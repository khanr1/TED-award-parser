package io.github.khanr1.tedawardparser
package repository
package parsers
package tedexport
package codedDataSection

import scala.xml.Elem
import common.*
import io.github.khanr1.tedawardparser.tedexport.codedDataSection.*
import io.github.khanr1.tedawardparser.repository.parsers.tedexport.codedDataSection.PartialOfficialJournalReferenceDecoder.given
import io.github.khanr1.tedawardparser.repository.parsers.tedexport.codedDataSection.PartialNoticeDataDecoder.given
import io.github.khanr1.tedawardparser.repository.parsers.tedexport.codedDataSection.PartialCodeIFDataDecoder.given

import io.github.khanr1.tedawardparser.tedexport.codedDataSection.PartialOfficialJournalReference
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
