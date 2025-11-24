package io.github.khanr1.tedawardparser
package repository
package parsers
package tedexport
package codedDataSection

import io.github.khanr1.tedawardparser.repository.parsers.{
  ParserError,
  XMLDecoder
}
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.common.Date
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.textAtOrError
import io.github.khanr1.tedawardparser.tedexport.codedDataSection.*
import scala.xml.Elem
//import io.github.khanr1.tedawardparser.repository.parsers.r208.R208Path.PublicationDate

object PartialOfficialJournalReferenceDecoder:
  val path: XMLPath = XMLPath("CODED_DATA_SECTION") / "REF_OJS"
  given XMLDecoder[PartialOfficialJournalReference[ParserError]] =
    new XMLDecoder[PartialOfficialJournalReference[ParserError]] {

      override def decode(
          e: Elem
      ): Either[ParserError, PartialOfficialJournalReference[ParserError]] =
        val journalSeries = e
          .textAtOrError(path / "COLL_OJ", "Journal Serie")
          .map(s => JournalSeries(s))
        val journalNumber = e
          .textAtOrError(path / "NO_OJ", "Journal Number")
          .map(s => JournalNumber(s))
        val publicationDatePath = path / "DATE_PUB"
        val publicationDate = e
          .textAtOrError(publicationDatePath, "Publication Date")
          .flatMap(s =>
            Date(s).leftMap(_ =>
              ParserError.InvalidFormat(
                "Publication Date",
                "yyyyMMdd",
                s,
                Some(publicationDatePath.show)
              )
            )
          )
        Right(
          PartialOfficialJournalReference[ParserError](
            journalSeries,
            journalNumber,
            publicationDate
          )
        )

    }
