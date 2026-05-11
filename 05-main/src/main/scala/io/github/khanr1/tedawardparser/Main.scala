package io.github.khanr1
package tedawardparser

import cats.effect.{IO, IOApp}
import fs2.io.file.{Files, Path}
import fs2.text
import io.github.khanr1.tedawardparser.repository.file.{UblXMLFileRepository, XMLFileRepository}
import io.github.khanr1.tedawardparser.repository.csv.{NoticeToCSV, UblNoticeToCSV}
import io.github.khanr1.tedawardparser.repository.xml.NoticeMapper

object Main extends IOApp.Simple:

  val tedDir = Path(
    "/Users/raphaelkhan/Developer/ted-award-parser/04-persistence/src/main/resources/TED_08-01-2026"
  )

  val ublDir = Path(
    "/Users/raphaelkhan/Developer/ted-award-parser/04-persistence/src/test/resources/ubl"
  )

  val tedOutput = Path("result.csv")
  val ublOutput = Path("ubl.csv")

  val tedPipeline: IO[Unit] =
    XMLFileRepository
      .stream[IO](tedDir)
      .map(NoticeMapper.toDomain)
      .through(NoticeToCSV.toCSV)
      .intersperse("\n")
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(tedOutput))
      .compile
      .drain

  val ublPipeline: IO[Unit] =
    UblXMLFileRepository
      .stream[IO](ublDir)
      .through(UblNoticeToCSV.toCSV)
      .intersperse("\n")
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(ublOutput))
      .compile
      .drain

  val run: IO[Unit] =
    tedPipeline >> ublPipeline
