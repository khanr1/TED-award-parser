package io.github.khanr1
package tedawardparser

import io.github.khanr1.tedawardparser.repository.file.XMLFileRepository
import io.github.khanr1.tedawardparser.models.NoticeToCSV.toCSV

import cats.effect.{IO, IOApp}
import fs2.io.file.Path
import fs2.Pipe
import scala.xml.Elem
import fs2.io.file.Files
import scala.xml.XML

import fs2.text

object Main extends IOApp.Simple {

  val dir = Path(
    "/Users/raphaelkhan/Developer/ted-award-parser/04-persistence/src/main/resources/TED_08-11-2025"
  )
  val pathToElem: Pipe[IO, Path, Elem] =
    _.evalFilter(Files[IO].isRegularFile)
      .evalMap { path =>
        Files[IO]
          .readAll(path)
          .through(fs2.text.utf8.decode)
          .compile
          .string // read full file content
          .flatMap { content =>
            IO(XML.loadString(content)) // parse to scala.xml.Elem
          }
      }

  /** The main stream: list dir -> parse files -> produce Elems */
  val Elems: fs2.Stream[IO, Elem] =
    Files[IO]
      .list(dir) // Stream[IO, Path]
      .through(pathToElem)

  val repo = XMLFileRepository.make[IO](dir)

  val csv =
    repo.getAll.through(toCSV)

  val output = Path("data2.csv")

  val writeStream: fs2.Stream[IO, Unit] =
    csv // Elem → String
      .intersperse("\n") // optional: separate entries with newlines
      .through(text.utf8.encode) // String → bytes
      .through(Files[IO].writeAll(output))
  val run = writeStream.compile.drain

}
