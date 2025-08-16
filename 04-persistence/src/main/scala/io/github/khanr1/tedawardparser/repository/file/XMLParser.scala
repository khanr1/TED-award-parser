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
import io.github.khanr1.tedawardparser.repository.file.XMLPathUtils.textAt

trait XMLParser[F[_]]:

  def dateFormatter: DateTimeFormatter
  def parseOJSNoticeID(elem: Elem): F[Either[ParserError, OJSNoticeID]]
  def parsePublicationDate(elem: Elem): F[Either[ParserError, LocalDate]]
  def parseContractingAuthorityName(
      elem: Elem
  ): F[Either[ParserError, ContractingAuthorityName]]
  def parseContractingAuthorityCountry(
      elem: Elem
  ): F[Either[ParserError, Country]]
  def parseContractingAuthority(
      elem: Elem
  ): F[Either[ParserError, ContractingAuthority]]
  def parseContractID(elem: Elem): F[List[Either[ParserError, ContractID]]]
  def parseTenderLotTitle(elem: Elem): F[List[Either[ParserError, Title]]]
  def parseTenderLotDescription(
      elem: Elem
  ): F[List[Either[ParserError, Description]]]
  def parseTenderLotValue(
      elem: Elem
  ): F[List[Either[ParserError, squants.Money]]]
  def parseTenderLotAwardedSupplierName(
      elem: Elem
  ): F[List[Either[ParserError, AwardedSupplierName]]]
  def parseTenderLotAwardedSupplierCountry(
      elem: Elem
  ): F[List[Either[ParserError, Country]]]
  def parseTenderLotAwardedSupplier(
      elem: Elem
  ): F[List[Either[ParserError, AwardedSupplier]]]
  def parseTenderLotJustification(
      e: Elem
  ): F[List[Either[ParserError, Justification]]]

class TedExportR208[F[_]: Monad] extends XMLParser[F] {

  override def parseOJSNoticeID(
      elem: Elem
  ): F[Either[ParserError, OJSNoticeID]] = elem
    .textAt(OjsID)
    .toRight(ParserError.MissingField("OjsID", Some(OjsID.show)))
    .map(OJSNoticeID(_))
    .pure[F]

  override def parsePublicationDate(
      elem: Elem
  ): F[Either[ParserError, LocalDate]] = elem
    .textAt(PublicationDate)
    .toRight(
      ParserError.MissingField("Publication Date", Some(PublicationDate.show))
    )
    .flatMap { s =>
      Either
        .catchNonFatal(LocalDate.parse(s, dateFormatter))
        .leftMap(t =>
          ParserError.InvalidFormat(
            "Publication Date",
            "yyyyMMdd",
            s,
            Some(PublicationDate.show)
          )
        )
    }
    .pure[F]

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
  ): F[List[Either[ParserError, ContractID]]] =
    ???

  override def parseTenderLotTitle(
      elem: Elem
  ): F[List[Either[ParserError, Title]]] =
    ???

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

  //////////////////////////////////////////////////////////
  /////// PATH. /////////////////////////////////////////////
  private val OjsID: XMLPath =
    XMLPath("CODED_DATA_SECTION", "NOTICE_DATA", "NO_DOC_OJS")
  private val PublicationDate: XMLPath =
    XMLPath("CODED_DATA_SECTION", "REF_OJS", "DATE_PUB")

  private val Form: XMLPath = XMLPath("FORM_SECTION")

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
  private val awardContractjustification = List(
    "FORM_SECTION",
    "CONTRACT_AWARD",
    "FD_CONTRACT_AWARD",
    "PROCEDURE_DEFINITION_CONTRACT_AWARD_NOTICE",
    "TYPE_OF_PROCEDURE_DEF",
    "F03_AWARD_WITHOUT_PRIOR_PUBLICATION",
    "ANNEX_D"
  )
  private val veatAwardContractjustification = List(
    "FORM_SECTION",
    "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE",
    "FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE",
    "PROCEDURE_DEFINITION_VEAT",
    "TYPE_OF_PROCEDURE_DEF_F15",
    "F15_PT_NEGOTIATED_WITHOUT_COMPETITION",
    "ANNEX_D_F15",
    "ANNEX_D1"
  )

  override def dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

  // override def parsePublicationDate(
  //     elem: Elem
  // ): F[Either[ParserError, LocalDate]] =
  //   elem
  //     .getText("CODED_DATA_SECTION", "REF_OJS", "DATE_PUB")
  //     .toRight(ParserError.NoPublicationDate)
  //     .map(LocalDate.parse(_, dateFormatter))
  //     .pure[F]

  // override def parseContractingAuthorityName(
  //     elem: Elem
  // ): F[Either[ParserError, ContractingAuthorityName]] = {
  //   val namePaths = List(
  //     delegatedPurchaseBasicPath,
  //     directPurchaseBasicPath,
  //     veatPurchaseBasicPath
  //   ).map(_ :+ "ORGANISATION" :+ "OFFICIALNAME")

  //   val maybeName = namePaths.iterator.map(p => elem.getText(p*)).collectFirst {
  //     case Some(value) => value
  //   }

  //   maybeName
  //     .toRight(ParserError.NoContractingAuthorityName)
  //     .map(ContractingAuthorityName(_))
  //     .pure[F]

  // }

  // override def parseContractingAuthorityCountry(
  //     elem: Elem
  // ): F[Either[ParserError, Country]] = {
  //   val coutryPaths = List(
  //     delegatedPurchaseBasicPath,
  //     directPurchaseBasicPath,
  //     veatPurchaseBasicPath
  //   ).map(x => x :+ "COUNTRY")

  //   val maybeCountry =
  //     coutryPaths.iterator.map(p => elem.getAttr("VALUE", p*)).collectFirst {
  //       case Some(value) => value
  //     }

  //   maybeCountry
  //     .toRight(ParserError.NoContractingAuthorityCountry)
  //     .map(Country.toDomain)
  //     .pure[F]

  // }

  // override def parseContractingAuthority(
  //     elem: Elem
  // ): F[Either[ParserError, ContractingAuthority]] = {
  //   val nameT = EitherT(parseContractingAuthorityName(elem))
  //   val countryT = EitherT(parseContractingAuthorityCountry(elem))

  //   (nameT, countryT).mapN(ContractingAuthority.apply).value

  // }

  // override def parseContractID(
  //     elem: Elem
  // ): F[List[Either[ParserError, ContractID]]] = {
  //   val validPath = List(awardContractBasicPath, veatAwardContractBasicPath)
  //   val resolvedElem =
  //     validPath.flatMap(path => elem.resolvePath(path*)).collect {
  //       case e: Elem => e
  //     }
  //   val ids: List[Option[ContractID]] = resolvedElem.map(e => {
  //     val lot = e.getText("LOT_NUMBER").map(ContractID(_))
  //     val contract = e.getText("CONTRACT_NUMBER").map(ContractID(_))

  //     contract.orElse(lot)

  //   })

  //   ids.map(x => x.toRight(ParserError.NoContractID)).pure[F]

  // }

  // override def parseTenderLotTitle(
  //     elem: Elem
  // ): F[List[Either[ParserError, Title]]] = {
  //   val validPath = List(awardContractInfoBasicPath, veatAwardContractInfoPath)
  //   val resolvedElem =
  //     validPath.flatMap(path => elem.resolvePath(path*)).collect {
  //       case e: Elem => e
  //     }
  //   val titles = resolvedElem.map(e => {
  //     e.getText("TITLE_CONTRACT").map(Title(_))
  //   })

  //   titles.map(x => x.toRight(ParserError.NoTitle)).pure[F]
  // }

  // override def parseTenderLotDescription(
  //     elem: Elem
  // ): F[List[Either[ParserError, Description]]] = {
  //   val validPath = List(awardContractInfoBasicPath, veatAwardContractInfoPath)
  //   val resolvedElem =
  //     validPath.flatMap(path => elem.resolvePath(path*)).collect {
  //       case e: Elem => e
  //     }
  //   val descriptions = resolvedElem.map(e => {
  //     e.getText("SHORT_CONTRACT_DESCRIPTION").map(Description(_))
  //   })

  //   descriptions.map(x => x.toRight(ParserError.NoDescription)).pure[F]
  // }

  // override def parseTenderLotValue(
  //     elem: Elem
  // ): F[List[Either[ParserError, Money]]] =
  //   val validPath = List(awardContractBasicPath, veatAwardContractBasicPath)
  //   val resolve =
  //     validPath.flatMap(p => elem.resolvePath(p*)).collect { case e: Elem => e }

  //   val values = resolve
  //     .map(e =>
  //       e.getText(
  //         "CONTRACT_VALUE_INFORMATION",
  //         "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE",
  //         "VALUE_COST"
  //       )
  //     )
  //     .map(maybeAmount =>
  //       maybeAmount.flatMap(s =>
  //         Try(BigDecimal(s.replace(" ", "").replace(",", "."))).toOption
  //       )
  //     )

  //   val currency = resolve
  //     .map(e =>
  //       e.getAttr(
  //         "CURRENCY",
  //         "CONTRACT_VALUE_INFORMATION",
  //         "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE"
  //       )
  //     )
  //     .map(maybeCurrent =>
  //       maybeCurrent.flatMap(s => Currency(s)(defaultMoneyContext).toOption)
  //     )

  //   values
  //     .zip(currency)
  //     .map((a, b) =>
  //       (a, b) match
  //         case (Some(x), Some(y)) => Some(Money(x, y))
  //         case _                  => None
  //     )
  //     .map(x => x.toRight(ParserError.NoValue))
  //     .pure[F]

  // override def parseTenderLotAwardedSupplierName(
  //     elem: Elem
  // ): F[List[Either[ParserError, AwardedSupplierName]]] = {
  //   val validPath = List(awardContractBasicPath, veatAwardContractBasicPath)
  //   val resolvedPath = validPath.flatMap(p => elem.resolvePath(p*)).collect {
  //     case x: Elem => x
  //   }
  //   val pathNameSupplieContract = List(
  //     "ECONOMIC_OPERATOR_NAME_ADDRESS",
  //     "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME",
  //     "ORGANISATION",
  //     "OFFICIALNAME"
  //   )
  //   val names = resolvedPath.map(e =>
  //     e.getText(pathNameSupplieContract*).map(s => AwardedSupplierName(s))
  //   )

  //   names
  //     .map(maybeName => maybeName.toRight(ParserError.NoAwardedSupplier))
  //     .pure[F]

  // }

  // override def parseTenderLotAwardedSupplierCountry(
  //     elem: Elem
  // ): F[List[Either[ParserError, Country]]] = {
  //   val pathNameSupplieCountry = List(
  //     "ECONOMIC_OPERATOR_NAME_ADDRESS",
  //     "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME"
  //   )
  //   val validPath = List(awardContractBasicPath, veatAwardContractBasicPath)
  //   val resolvedPath =
  //     validPath.flatMap(p => elem.resolvePath(p*)).collect { case x: Elem => x }
  //   val coutries =
  //     resolvedPath.map(e =>
  //       e.getAttr("VALUE", "COUNTRY").map(Country.toDomain(_))
  //     )

  //   coutries.map(l => l.toRight(ParserError.NoAwardedSupplierCountry)).pure[F]
  // }

  // override def parseTenderLotAwardedSupplier(
  //     elem: Elem
  // ): F[List[Either[ParserError, AwardedSupplier]]] = {
  //   val fName = parseTenderLotAwardedSupplierName(elem)
  //   val fCountry = parseTenderLotAwardedSupplierCountry(elem)

  //   (fName, fCountry).mapN((l1, l2) =>
  //     l1.zip(l2)
  //       .map((maybeName, maybeCountry) =>
  //         (maybeName, maybeCountry) match
  //           case (Right(name), Right(country)) =>
  //             Right(AwardedSupplier(name, country))
  //           case _ => Left(ParserError.NoAwardedSupplier)
  //       )
  //   )

  // }

  // override def parseTenderLotJustification(
  //     elem: Elem
  // ): F[List[Either[ParserError, Justification]]] = {
  //   val validPath =
  //     List(awardContractjustification, veatAwardContractjustification)
  //   val resolvedPath =
  //     validPath.flatMap(p => elem.resolvePath(p*)).collect { case x: Elem => x }

  //   val justificaton =
  //     resolvedPath.map(e =>
  //       e.getText("REASON_CONTRACT_LAWFUL").map(Justification(_))
  //     )

  //   justificaton.map(e => e.toRight(ParserError.NoJustification)).pure[F]
  // }

}
