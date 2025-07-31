package io.github.khanr1.tedawardparser
package repository
package file

import weaver.SimpleIOSuite
import fs2.io.file.{Files, Path}

import scala.xml.XML
import cats.effect.IO
import fs2.text
import scala.xml.Elem

import io.github.khanr1.tedawardparser.repository.*
import fs2.Pipe
import java.time.LocalDate

object XMLParserSuite extends SimpleIOSuite {
  val path: Path = Path(
    "/Users/raphaelkhan/Developer/ted-award-parser/04-persistence/src/test/resources/r208"
  )
  val xmlFile = Files[IO].walk(path).filter(path => path.extName == ".xml")
  def fileToElem: Pipe[IO, Path, Elem] = s =>
    s.evalMap(path =>
      Files[IO]
        .readAll(path)
        .through(text.utf8.decode)
        .compile
        .string
        .map(content => XML.loadString(content))
    )
  val xmlElems = xmlFile.through(fileToElem)

  def assertParser[A](
      parser: Elem => IO[Either[ParsingError, A]],
      expectedIO: List[Either[ParsingError, A]]
  ) =
    xmlElems
      .evalMap(parser)
      .compile
      .toList
      .map(result => {
        // println(result)
        assert.same(result.sortBy(_.toString), expectedIO.sortBy(_.toString()))
      })

  val parse = new TedExportR208[IO]

  test("Parsing the OJSNumber") {
    val expectedIO =
      List(
        "2016/S 002-001503",
        "2016/S 011-015034",
        "2016/S 015-021991",
        "2017/S 032-059079",
        "2016/S 046-075906",
        "2016/S 050-082736",
        "2016/S 123-220974",
        "2018/S 143-328218",
        "2017/S 219-456124",
        "2017/S 046-085146"
      ).map(Right(_))

    assertParser(parse.parseOJSNoticeID, expectedIO)

  }
  test("Parsing the publication date") {
    val expectedIO = List(
      "20160105",
      "20160116",
      "20160122",
      "20170215",
      "20160305",
      "20160311",
      "20160629",
      "20180727",
      "20171115",
      "20170307"
    ).map(x => Right(LocalDate.parse(x, parse.dateFormatter)))

    assertParser(parse.parsePublicationDate, expectedIO)

  }
  test("Parsing Contracting Authority Name") {
    val expectedIO = List(
      "Kungliga Tekniska Högskolan",
      "Delft University of Technology",
      "CNRS Délégation Paris B",
      "Paul-Scherrer-Institut (PSI), Strategischer Einkauf / Purchasing Department",
      "Deutsche Forschungsgemeinschaft e. V., Zentrale Beschaffungsstelle",
      "Max-Planck-Gesellschaft zur Förderung der Wissenschaften e. V., Max-Planck-Institut für Kernphysik",
      "Science and Technology Facilities Council",
      "Universiteit Leiden",
      "Universität zu Köln, Der Kanzler",
      "Deutsches Elektronen- Synchrotron DESY in der Helmholtz Gemeinschaft"
    ).map(Right(_))

    assertParser(parse.parseContractingAuthorityName, expectedIO)

  }

  test("Parsing Countracting Authority Country") {
    val expectedIO = List(
      "SE",
      "NL",
      "FR",
      "CH",
      "DE",
      "DE",
      "UK",
      "NL",
      "DE",
      "DE"
    ).map(x => Right(Country.toDomain(x)))

    assertParser(parse.parseCountry, expectedIO)
  }
}
