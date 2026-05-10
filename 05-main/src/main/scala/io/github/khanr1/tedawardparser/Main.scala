package io.github.khanr1
package tedawardparser

import cats.effect.{IO, IOApp}
import fs2.io.file.{Files, Path}
import fs2.text
import io.github.khanr1.tedawardparser.repository.file.XMLFileRepository
import io.github.khanr1.tedawardparser.repository.csv.NoticeToCSV
import io.github.khanr1.tedawardparser.repository.xml.NoticeMapper

object Main extends IOApp.Simple:

  val dir = Path(
    "/Users/raphaelkhan/Developer/ted-award-parser/04-persistence/src/main/resources/TED_08-01-2026"
  )

  val output = Path("result.csv")

  val run: IO[Unit] =
    XMLFileRepository
      .stream[IO](dir)
      .map(NoticeMapper.toDomain)
      .through(NoticeToCSV.toCSV)
      .intersperse("\n")
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(output))
      .compile
      .drain
