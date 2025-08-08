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
import cats.Monad
import cats.data.EitherT
import squants.market.*
import squants.market.defaultMoneyContext.*
import scala.util.Try

trait XMLParser[F[_]]:

  def dateFormatter: DateTimeFormatter
  def parseOJSNoticeID(elem: Elem): F[Either[ParsingError, OJSNoticeID]]
  def parsePublicationDate(elem: Elem): F[Either[ParsingError, LocalDate]]
  def parseContractingAuthorityName(
      elem: Elem
  ): F[Either[ParsingError, ContractingAuthorityName]]
  def parseContractingAuthorityCountry(
      elem: Elem
  ): F[Either[ParsingError, Country]]
  def parseContractingAuthority(
      elem: Elem
  ): F[Either[ParsingError, ContractingAuthority]]
  def parseContractID(elem: Elem): F[List[Either[ParsingError, ContractID]]]
  def parseTenderLotTitle(elem: Elem): F[List[Either[ParsingError, Title]]]
  def parseTenderLotDescription(
      elem: Elem
  ): F[List[Either[ParsingError, Description]]]
  def parseTenderLotValue(
      elem: Elem
  ): F[List[Either[ParsingError, squants.Money]]]
  def parseTenderLotAwardedSupplierName(
      elem: Elem
  ): F[List[Either[ParsingError, AwardedSupplierName]]]
  def parseTenderLotAwardedSupplierCountry(
      elem: Elem
  ): F[List[Either[ParsingError, Country]]]
  def parseTenderLotAwardedSupplier(
      elem: Elem
  ): F[List[Either[ParsingError, AwardedSupplier]]]
  def parseTenderLotJustification: F[List[Either[ParsingError, Justification]]]

class TedExportR208[F[_]: Monad] extends XMLParser[F] {

  private val directPurchaseBasicPath =
    List(
      "FORM_SECTION",
      "CONTRACT_AWARD",
      "FD_CONTRACT_AWARD",
      "CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD",
      "NAME_ADDRESSES_CONTACT_CONTRACT_AWARD",
      "CA_CE_CONCESSIONAIRE_PROFILE"
    )
  private val delegatedPurchaseBasicPath = List(
    "FORM_SECTION",
    "CONTRACT_AWARD",
    "FD_CONTRACT_AWARD",
    "CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD",
    "TYPE_AND_ACTIVITIES_AND_PURCHASING_ON_BEHALF",
    "PURCHASING_ON_BEHALF",
    "PURCHASING_ON_BEHALF_YES",
    "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY"
  )
  private val veatPurchaseBasicPath = List(
    "FORM_SECTION",
    "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE",
    "FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE",
    "CONTRACTING_AUTHORITY_VEAT",
    "NAME_ADDRESSES_CONTACT_VEAT",
    "CA_CE_CONCESSIONAIRE_PROFILE"
  )
  private val awardContractInfoBasicPath = List(
    "FORM_SECTION",
    "CONTRACT_AWARD",
    "FD_CONTRACT_AWARD",
    "OBJECT_CONTRACT_INFORMATION_CONTRACT_AWARD_NOTICE",
    "DESCRIPTION_AWARD_NOTICE_INFORMATION"
  )
  private val veatAwardContractInfoPath = List(
    "FORM_SECTION",
    "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE",
    "FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE",
    "OBJECT_VEAT",
    "DESCRIPTION_VEAT"
  )
  private val awardContractBasicPath = List(
    "FORM_SECTION",
    "CONTRACT_AWARD",
    "FD_CONTRACT_AWARD",
    "AWARD_OF_CONTRACT"
  )
  private val veatAwardContractBasicPath = List(
    "FORM_SECTION",
    "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE",
    "FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE",
    "AWARD_OF_CONTRACT_DEFENCE"
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

  override def parseContractingAuthorityCountry(
      elem: Elem
  ): F[Either[ParsingError, Country]] = {
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
  ): F[Either[ParsingError, ContractingAuthority]] = {
    val nameT = EitherT(parseContractingAuthorityName(elem))
    val countryT = EitherT(parseContractingAuthorityCountry(elem))

    (nameT, countryT).mapN(ContractingAuthority.apply).value

  }

  override def parseContractID(
      elem: Elem
  ): F[List[Either[ParsingError, ContractID]]] = {
    val validPath = List(awardContractBasicPath, veatAwardContractBasicPath)
    val resolvedElem =
      validPath.flatMap(path => elem.resolvePath(path*)).collect {
        case e: Elem => e
      }
    val ids: List[Option[ContractID]] = resolvedElem.map(e => {
      val lot = e.getText("LOT_NUMBER").map(ContractID(_))
      val contract = e.getText("CONTRACT_NUMBER").map(ContractID(_))

      contract.orElse(lot)

    })

    ids.map(x => x.toRight(ParsingError.NoContractID)).pure[F]

  }

  override def parseTenderLotTitle(
      elem: Elem
  ): F[List[Either[ParsingError, Title]]] = {
    val validPath = List(awardContractInfoBasicPath, veatAwardContractInfoPath)
    val resolvedElem =
      validPath.flatMap(path => elem.resolvePath(path*)).collect {
        case e: Elem => e
      }
    val titles = resolvedElem.map(e => {
      e.getText("TITLE_CONTRACT").map(Title(_))
    })

    titles.map(x => x.toRight(ParsingError.NoTitle)).pure[F]
  }

  override def parseTenderLotDescription(
      elem: Elem
  ): F[List[Either[ParsingError, Description]]] = {
    val validPath = List(awardContractInfoBasicPath, veatAwardContractInfoPath)
    val resolvedElem =
      validPath.flatMap(path => elem.resolvePath(path*)).collect {
        case e: Elem => e
      }
    val descriptions = resolvedElem.map(e => {
      e.getText("SHORT_CONTRACT_DESCRIPTION").map(Description(_))
    })

    descriptions.map(x => x.toRight(ParsingError.NoDescription)).pure[F]
  }

  override def parseTenderLotValue(
      elem: Elem
  ): F[List[Either[ParsingError, Money]]] =
    val validPath = List(awardContractBasicPath, veatAwardContractBasicPath)
    val resolve =
      validPath.flatMap(p => elem.resolvePath(p*)).collect { case e: Elem => e }

    val values = resolve
      .map(e =>
        e.getText(
          "CONTRACT_VALUE_INFORMATION",
          "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE",
          "VALUE_COST"
        )
      )
      .map(maybeAmount =>
        maybeAmount.flatMap(s =>
          Try(BigDecimal(s.replace(" ", "").replace(",", "."))).toOption
        )
      )

    val currency = resolve
      .map(e =>
        e.getAttr(
          "CURRENCY",
          "CONTRACT_VALUE_INFORMATION",
          "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE"
        )
      )
      .map(maybeCurrent =>
        maybeCurrent.flatMap(s => Currency(s)(defaultMoneyContext).toOption)
      )

    values
      .zip(currency)
      .map((a, b) =>
        (a, b) match
          case (Some(x), Some(y)) => Some(Money(x, y))
          case _                  => None
      )
      .map(x => x.toRight(ParsingError.NoValue))
      .pure[F]

  override def parseTenderLotAwardedSupplierName(
      elem: Elem
  ): F[List[Either[ParsingError, AwardedSupplierName]]] = {
    val validPath = List(awardContractBasicPath, veatAwardContractBasicPath)
    val resolvedPath = validPath.flatMap(p => elem.resolvePath(p*)).collect {
      case x: Elem => x
    }
    val pathNameSupplieContract = List(
      "ECONOMIC_OPERATOR_NAME_ADDRESS",
      "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME",
      "ORGANISATION",
      "OFFICIALNAME"
    )
    val names = resolvedPath.map(e =>
      e.getText(pathNameSupplieContract*).map(s => AwardedSupplierName(s))
    )

    names
      .map(maybeName => maybeName.toRight(ParsingError.NoAwardedSupplier))
      .pure[F]

  }

  override def parseTenderLotAwardedSupplierCountry(
      elem: Elem
  ): F[List[Either[ParsingError, Country]]] = ???

  override def parseTenderLotAwardedSupplier(
      elem: Elem
  ): F[List[Either[ParsingError, AwardedSupplier]]] = ???

  override def parseTenderLotJustification
      : F[List[Either[ParsingError, Justification]]] = ???

}
