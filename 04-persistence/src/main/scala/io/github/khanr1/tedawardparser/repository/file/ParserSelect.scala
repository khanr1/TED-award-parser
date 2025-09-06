package io.github.khanr1.tedawardparser
package repository
package file

import io.github.khanr1.tedawardparser.repository.parsers.*
import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.attrAt
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.rootAttr
import cats.Monad
import squants.Money
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ParserSelect {

  private val ted: String = "TED_EXPORT"
  private val version: XMLPath = XMLPath("TED_EXPORT").attr("VERSION")

  def detect[F[_]: Monad](elem: Elem): XMLParser[F] = {
    val isTedExport = elem.label == ted

    if isTedExport then
      elem.rootAttr("VERSION") match
        case Some(value) => new r209.TedExportR209[F]
        case None        => new r208.TedExportR208[F]
    else
      new XMLParser[F] {

        override def dateFormatter: DateTimeFormatter = ???

        override def parseOJSNoticeID(
            elem: Elem
        ): F[Either[ParserError, OJSNoticeID]] = ???

        override def parsePublicationDate(
            elem: Elem
        ): F[Either[ParserError, LocalDate]] = ???

        override def parseContractingAuthorityName(
            elem: Elem
        ): F[Either[ParserError, ContractingAuthorityName]] = ???

        override def parseContractingAuthorityCountry(
            elem: Elem
        ): F[Either[ParserError, Country]] = ???

        override def parseContractingAuthority(
            elem: Elem
        ): F[Either[ParserError, ContractingAuthority]] = ???

        override def parseContractID(
            elem: Elem
        ): F[List[Either[ParserError, ContractID]]] = ???

        override def parseTenderLotTitle(
            elem: Elem
        ): F[List[Either[ParserError, Title]]] = ???

        override def parseTenderLotDescription(
            elem: Elem
        ): F[List[Either[ParserError, Description]]] = ???

        override def parseTenderLotValue(
            elem: Elem
        ): F[List[Either[ParserError, Money]]] = ???

        override def parseTenderLotAwardedSupplierName(
            elem: Elem
        ): F[List[Either[ParserError, AwardedSupplierName]]] = ???

        override def parseTenderLotAwardedSupplierCountry(
            elem: Elem
        ): F[List[Either[ParserError, Country]]] = ???

        override def parseTenderLotAwardedSupplier(
            elem: Elem
        ): F[List[Either[ParserError, AwardedSupplier]]] = ???

        override def parseTenderLotJustification(
            e: Elem
        ): F[List[Either[ParserError, Justification]]] = ???

      }

  }

}
