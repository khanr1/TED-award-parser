package io.github.khanr1.tedawardparser
package repository
package parsers
package ubl

import cats.data.EitherT
import cats.Monad
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.parsers.Matching.attrValue
import io.github.khanr1.tedawardparser.repository.parsers.r208.R208Path.*
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.*
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import scala.util.Try
import scala.xml.Elem
import squants.market.*
import squants.market.defaultMoneyContext.*
import cats.syntax.validated
import io.github.khanr1.tedawardparser.repository.parsers.ubl.UBLPath.pOjsNoticeId
import io.github.khanr1.tedawardparser.repository.parsers.ubl.UBLPath.pOjsNumber
import scala.util.matching.Regex
import io.github.khanr1.tedawardparser.repository.parsers.ubl.UBLPath.pPublicationDate
import io.github.khanr1.tedawardparser.repository.parsers.ubl.UBLPath.pOrgId
import io.github.khanr1.tedawardparser.repository.parsers.ubl.UBLPath.pOrganizations
import io.github.khanr1.tedawardparser.repository.parsers.ubl.UBLPath.pOrgIdInRegistry
import io.github.khanr1.tedawardparser.repository.parsers.ubl.UBLPath.pOrgNameInRegistry
import io.github.khanr1.tedawardparser.repository.parsers.ubl.UBLPath.pOrgCountryInRegistry

class UBLParser[F[_]: Monad] extends XMLParser[F] {

  private val ns = UBLPath.ns
  override def dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-ddXXXXX")
  override def parseOJSNoticeID(
      elem: Elem
  ): F[Either[ParserError, OJSNoticeID]] = {
    val noticeID = elem.textAtOrError(pOjsNoticeId, "OjsID", ns)
    val ojsID = elem.textAtOrError(pOjsNumber, "OjsID", ns)

    val zipped = for
      ojs <- ojsID
      number <- noticeID
    yield (ojs, number)

    zipped
      .flatMap(x =>
        UBLhelpers
          .formatE(x._1, x._2)
          .leftMap(s => ParserError.InvalidFormat("OjsID", "XX/YYYY", s, None))
      )
      .map(s => OJSNoticeID(s))
      .pure[F]

  }

  override def parsePublicationDate(
      elem: Elem
  ): F[Either[ParserError, LocalDate]] = elem
    .textAtOrError(pPublicationDate, "Publication date", ns)
    .map(s => LocalDate.parse(s, dateFormatter))
    .pure[F]

  override def parseContractingAuthorityName(
      elem: Elem
  ): F[Either[ParserError, ContractingAuthorityName]] = {
    val orgID = elem.textAt(pOrgId, ns)
    val orgs = elem.childrenAt(pOrganizations, ns)
    val name =
      for
        id <- orgID
        org <- orgs.find(o => o.textAt(pOrgIdInRegistry, ns).contains(id))
        name <- org.textAt(pOrgNameInRegistry, ns)
      yield name

    name
      .map(ContractingAuthorityName(_))
      .toRight(ParserError.MissingField("Contracting Authority Name"))
      .pure[F]
  }

  override def parseContractingAuthorityCountry(
      elem: Elem
  ): F[Either[ParserError, Country]] = {
    val orgID = elem.textAt(pOrgId, ns)
    val orgs = elem.childrenAt(pOrganizations, ns)
    val country =
      for
        id <- orgID
        org <- orgs.find(o => o.textAt(pOrgIdInRegistry, ns).contains(id))
        country <- org.textAt(pOrgCountryInRegistry, ns)
      yield country

    country
      .map(Country.toDomain(_))
      .toRight(ParserError.MissingField("Contracting Authority Name"))
      .pure[F]
  }

  override def parseContractingAuthority(
      elem: Elem
  ): F[Either[ParserError, ContractingAuthority]] = {
    val name = EitherT(parseContractingAuthorityName(elem))
    val country = EitherT(parseContractingAuthorityCountry(elem))

    (name, country).mapN((n, c) => ContractingAuthority(n, c)).value
  }

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
  ): F[List[Either[ParserError, squants.Money]]] = ???

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
