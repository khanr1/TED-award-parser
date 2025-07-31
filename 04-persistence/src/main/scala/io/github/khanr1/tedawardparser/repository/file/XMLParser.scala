package io.github.khanr1.tedawardparser
package repository
package file

import scala.xml.*
import java.time.LocalDate
import squants.Money
import cats.Applicative
import cats.syntax.all.*
import cats.MonadError
import java.time.format.DateTimeFormatter
import java.sql.Date

trait XMLParser[F[_]]:

  def dateFormatter: DateTimeFormatter
  def parseOJSNoticeID(elem: Elem): F[Either[ParsingError, OJSNoticeID]]
  def parsePublicationDate(elem: Elem): F[Either[ParsingError, LocalDate]]
  def parseContractingAuthorityName(
      elem: Elem
  ): F[Either[ParsingError, ContractingAuthorityName]]
  def parseCountry(elem: Elem): F[Either[ParsingError, Country]]
  def parseContractingAuthority(
      elem: Elem
  ): F[Either[ParsingError, ContractingAuthority]]
  def parseTenderLotTitle(elem: Elem): F[Either[ParsingError, Title]]
  def parseTenderLotDescription(
      elem: Elem
  ): F[Either[ParsingError, Description]]
  def parseTenderLotValue(elem: Elem): F[Either[ParsingError, squants.Money]]
  def parseTenderLotAwardedSupplierName(
      elem: Elem
  ): F[Either[ParsingError, AwardedSupplierName]]
  def parseTenderLotAwardedSupplier(
      elem: Elem
  ): F[Either[ParsingError, AwardedSupplier]]
  def parseTenderLotJustification: F[Either[ParsingError, Justification]]

class TedExportR208[F[_]: Applicative] extends XMLParser[F] {

  val directPurchaseBasicPath =
    List(
      "FORM_SECTION",
      "CONTRACT_AWARD",
      "FD_CONTRACT_AWARD",
      "CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD",
      "NAME_ADDRESSES_CONTACT_CONTRACT_AWARD",
      "CA_CE_CONCESSIONAIRE_PROFILE"
    )
  val delegatedPurchaseBasicPath = List(
    "FORM_SECTION",
    "CONTRACT_AWARD",
    "FD_CONTRACT_AWARD",
    "CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD",
    "TYPE_AND_ACTIVITIES_AND_PURCHASING_ON_BEHALF",
    "PURCHASING_ON_BEHALF",
    "PURCHASING_ON_BEHALF_YES",
    "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY"
  )
  val veatPurchaseBasicPath = List(
    "FORM_SECTION",
    "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE",
    "FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE",
    "CONTRACTING_AUTHORITY_VEAT",
    "NAME_ADDRESSES_CONTACT_VEAT",
    "CA_CE_CONCESSIONAIRE_PROFILE"
  )
  override def dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")
  override def parseOJSNoticeID(
      elem: Elem
  ): F[Either[ParsingError, OJSNoticeID]] =
    elem
      .getText("CODED_DATA_SECTION", "NOTICE_DATA", "NO_DOC_OJS")
      .toRight(ParsingError.NoOJSID)
      .map(OJSNoticeID(_))
      .pure[F]

  override def parsePublicationDate(
      elem: Elem
  ): F[Either[ParsingError, LocalDate]] =
    elem
      .getText("CODED_DATA_SECTION", "REF_OJS", "DATE_PUB")
      .toRight(ParsingError.NoPublicationDate)
      .map(LocalDate.parse(_, dateFormatter))
      .pure[F]

  override def parseContractingAuthorityName(
      elem: Elem
  ): F[Either[ParsingError, ContractingAuthorityName]] = {
    val namePaths = List(
      delegatedPurchaseBasicPath,
      directPurchaseBasicPath,
      veatPurchaseBasicPath
    ).map(_ :+ "ORGANISATION" :+ "OFFICIALNAME")

    val maybeName = namePaths.iterator.map(p => elem.getText(p*)).collectFirst {
      case Some(value) => value
    }

    maybeName
      .toRight(ParsingError.NoContractingAuthorityName)
      .map(ContractingAuthorityName(_))
      .pure[F]

  }

  override def parseCountry(elem: Elem): F[Either[ParsingError, Country]] = {
    val coutryPaths = List(
      delegatedPurchaseBasicPath,
      directPurchaseBasicPath,
      veatPurchaseBasicPath
    ).map(x => x :+ "COUNTRY")

    val maybeCountry =
      coutryPaths.iterator.map(p => elem.getAttr("VALUE", p*)).collectFirst {
        case Some(value) => value
      }

    maybeCountry
      .toRight(ParsingError.NoContractingAuthorityCountry)
      .map(Country.toDomain)
      .pure[F]

  }

  override def parseContractingAuthority(
      elem: Elem
  ): F[Either[ParsingError, ContractingAuthority]] = ???

  override def parseTenderLotTitle(elem: Elem): F[Either[ParsingError, Title]] =
    ???

  override def parseTenderLotDescription(
      elem: Elem
  ): F[Either[ParsingError, Description]] = ???

  override def parseTenderLotValue(elem: Elem): F[Either[ParsingError, Money]] =
    ???

  override def parseTenderLotAwardedSupplierName(
      elem: Elem
  ): F[Either[ParsingError, AwardedSupplierName]] = ???

  override def parseTenderLotAwardedSupplier(
      elem: Elem
  ): F[Either[ParsingError, AwardedSupplier]] = ???

  override def parseTenderLotJustification
      : F[Either[ParsingError, Justification]] = ???

}
