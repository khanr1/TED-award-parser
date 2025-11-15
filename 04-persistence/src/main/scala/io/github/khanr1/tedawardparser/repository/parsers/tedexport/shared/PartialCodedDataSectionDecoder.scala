package io.github.khanr1.tedawardparser
package repository
package parsers
package tedexport
package shared

import scala.xml.Elem
import io.github.khanr1.tedawardparser.tedexport.shared.*
import io.github.khanr1.tedawardparser.repository.parsers.tedexport.shared.PartialOfficialJournalReferenceDecoder.given
import io.github.khanr1.tedawardparser.repository.parsers.tedexport.shared.PartialNoticeDataDecoder.given
import io.github.khanr1.tedawardparser.repository.parsers.tedexport.shared.PartialCodeIFDataDecoder.given

import io.github.khanr1.tedawardparser.tedexport.shared.PartialOfficialJournalReference
import io.github.khanr1.tedawardparser.models.PartialNotice

object PartialCodedDataSectionDecoder {
  val path = XMLPath("CODED_DATA_SECTION")

  given XMLDecoder[PartialCodedDataSection[ParserError]] =
    new XMLDecoder[PartialCodedDataSection[ParserError]] {

      override def decode(
          e: Elem
      ): Either[ParserError, PartialCodedDataSection[ParserError]] = {
        val ojsRef =
          summon[XMLDecoder[PartialOfficialJournalReference[ParserError]]]
            .decode(e)
        val noticeData = summon[XMLDecoder[PartialNoticeData[ParserError]]]
          .decode(e)
        val codeIf = summon[XMLDecoder[PartialCodeIF[ParserError]]]
          .decode(e)

        for
          journal <- ojsRef
          notice <- noticeData
          code <- codeIf
        yield PartialCodedDataSection(journal, notice, code)

      }

    }
}
