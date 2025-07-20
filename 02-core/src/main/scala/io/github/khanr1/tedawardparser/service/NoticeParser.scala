package io.github.khanr1.tedawardparser
package service

import scala.xml.Elem
import fs2.Pipe
import io.github.khanr1.tedawardparser.NoticeType.toDomain

trait NoticeParser[F[_], A] {
  def parseStream: Pipe[F, A, Notice]
}
object NoticeParser:

  def TedExportParser[F[_]]: NoticeParser[F, Elem] =

    def parseNoticeNumber(xml: Elem): NoticeNumber =
      NoticeNumber(
        (xml \ "CODED_DATA_SECTION" \ "NOTICE_DATA" \ "NO_DOC_OJS").text
      )
    def parseNoticePublicationDate(xml: Elem): PublicationDate =
      PublicationDate(
        (xml \ "CODED_DATA_SECTION" \ "REF_OJS" \ "DATE_PUB").text
      )
    def parseNoticetype(xml: Elem): NoticeType =
      toDomain(
        (xml \ "CODED_DATA_SECTION" \ "CODIF_DATA" \ "TD_DOCUMENT_TYPE").text
      )

    new NoticeParser[F, Elem] {

      override def parseStream: Pipe[F, Elem, Notice] = s =>
        s.map(elem =>
          Notice(
            parseNoticeNumber(elem),
            parseNoticePublicationDate(elem),
            parseNoticetype(elem)
          )
        )

    }
