package io.github.khanr1.tedawardparser
package repository
package decoders
package tedexport.codedDataSection

import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.xml.{Raw, XMLDecoder}

final case class CodedDataSectionRaw(
    officialJournalReference: OfficialJournalReferenceRaw,
    noticeData: NoticeDataRaw,
    codeIF: CodeIFRaw
) extends Raw

object CodedDataSectionRaw:
  given XMLDecoder[CodedDataSectionRaw] = new XMLDecoder[CodedDataSectionRaw] {

    override def decode(e: Elem): CodedDataSectionRaw =
      CodedDataSectionRaw(
        summon[XMLDecoder[OfficialJournalReferenceRaw]].decode(e),
        summon[XMLDecoder[NoticeDataRaw]].decode(e),
        summon[XMLDecoder[CodeIFRaw]].decode(e)
      )

  }
