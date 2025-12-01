package io.github.khanr1.tedawardparser.repository
package xml
package decoders.codedDataSection

import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*
import cats.data.ValidatedNel
import cats.syntax.all.*
import cats.syntax.ior

case class OfficialJournalReferenceRaw(
    val journalSerie: Either[ParserError, String],
    val journalNumber: Either[ParserError, String],
    val date: Either[ParserError, String]
) extends Raw

object OfficialJournalReferenceRaw:
  given XMLDecoder[OfficialJournalReferenceRaw] =
    new XMLDecoder[OfficialJournalReferenceRaw] {

      override def decode(
          e: Elem
      ): OfficialJournalReferenceRaw = {
        val journalSeries = e.textAtOrError(
          xpath.CodedDataSectionPath.OfficialJournalReferencePath.journalSeriesPath,
          "Journal Serie"
        )
        val journalNumber = e.textAtOrError(
          xpath.CodedDataSectionPath.OfficialJournalReferencePath.journalNumberPath,
          "Journal Number"
        )

        val date = e.textAtOrError(
          xpath.CodedDataSectionPath.OfficialJournalReferencePath.publicationDatePath,
          "Publication Date"
        )

        OfficialJournalReferenceRaw(
          journalSeries,
          journalNumber,
          date
        )

      }

    }
