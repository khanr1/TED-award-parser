package io.github.khanr1.tedawardparser
package repository
package file

import cats.effect.IO
import cats.syntax.all.*
import fs2.{Pipe, text}
import fs2.io.file.{Files, Path}
import scala.xml.{XML, Elem}

import weaver.Expectations.Helpers.{expect, failure}
import weaver.Expectations

// Ordering for comparing streams
given ordering[A]: Ordering[Either[ParserError, A]] = Ordering.by(e =>
  e match
    case Left(value)  => (0, value.message)
    case Right(value) => (1, value.toString())
)

// Function taking a path and returning a Stream[IO,Path]
val xmlFile = (path: Path) =>
  Files[IO].walk(path).filter(p => p.extName == ".xml")

// Pipe to transform a Stream[IO,Path] to a Stream[IO,Elem]
def fileToElem: Pipe[IO, Path, Elem] = s =>
  s.evalMap(path =>
    Files[IO]
      .readAll(path)
      .through(text.utf8.decode)
      .compile
      .string
      .map(content => XML.loadString(content))
  )
// Function that take a path and return a Stream[IO,Elem]
val xmlElems = (path: Path) => xmlFile(path).through(fileToElem)

// ------------- GENERIC HELPERS -----------------//

// Function to test if the parser give a proper result
def assertParser[A](
    parser: Elem => IO[Either[ParserError, A]],
    expectedIO: List[Either[ParserError, A]],
    xmlElems: fs2.Stream[IO, Elem]
): IO[Expectations] =
  xmlElems
    .evalMap(parser)
    .compile
    .toList
    .map(result => {
      expect.same(result.sorted, expectedIO.sorted)
    })
// Function to test is a parser return the proper list of parsed items
def assertListParser[A](
    parser: Elem => IO[List[Either[ParserError, A]]],
    expectedIO: List[List[Either[ParserError, A]]],
    xmlElems: fs2.Stream[IO, Elem]
): IO[Expectations] =
  xmlElems
    .evalMap(parser)
    .compile
    .toList
    .map(result => {

      expect.same(
        result.map(l => l.sorted).sorted,
        expectedIO.map(l => l.sorted).sorted
      )
    })

def expectMissingField[A](
    io: IO[Either[ParserError, A]],
    field: String,
    path: XMLPath,
    note: String = ""
): IO[Expectations] =
  val expectedPath = path.show
  io.map {
    case Left(ParserError.MissingField(f, at)) =>
      expect.same(f, field) &&
      expect(at.contains(expectedPath))
    case other =>
      failure(s"Expected MissingField$note, got: $other")
  }
def expectInvalidFormat[A](
    io: IO[Either[ParserError, A]],
    field: String,
    expected: String,
    found: String,
    epath: XMLPath
): IO[Expectations] =
  val expectedPath = epath.show
  io.map {
    case Left(ParserError.InvalidFormat(f, exp, got, at)) =>
      expect.same(f, field) &&
      expect.same(exp, expected) &&
      expect.same(got, found) &&
      expect(at.contains(expectedPath))
    case other =>
      failure(s"Expected InvalidFormat, got: $other")
  }
