package io.github.khanr1.tedawardparser.repository
package decoders.tedexport.codedDataSection

import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.xml.{ParserError, Raw, XMLDecoder}
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*
import io.github.khanr1.tedawardparser.repository.xpath.tedexport.CodedDataSectionPath
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
          CodedDataSectionPath.OfficialJournalReferencePath.journalSeriesPath,
          "Journal Serie"
        )
        val journalNumber = e.textAtOrError(
          CodedDataSectionPath.OfficialJournalReferencePath.journalNumberPath,
          "Journal Number"
        )

        val date = e.textAtOrError(
          CodedDataSectionPath.OfficialJournalReferencePath.publicationDatePath,
          "Publication Date"
        )

        OfficialJournalReferenceRaw(
          journalSeries,
          journalNumber,
          date
        )

      }

    }
