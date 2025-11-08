package io.github.khanr1.tedawardparser
package repository
package parsers
package r208

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
import io.github.khanr1.tedawardparser.repository.parsers.r208.R208Path.FormSection.ContractAward.LotTitle

class TedExportR208[F[_]: Monad] extends XMLParser[F] {

  override def dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

  override def parseOJSNoticeID(
      elem: Elem
  ): F[Either[ParserError, OJSNoticeID]] = elem
    .textAtOrError(OjsID, "OjsID")
    .map(s => OJSNoticeID(s))
    .pure[F]

  override def parsePublicationDate(
      elem: Elem
  ): F[Either[ParserError, LocalDate]] = elem
    .textAtOrError(PublicationDate, "Publication Date")
    .flatMap { s =>
      Either
        .catchNonFatal(LocalDate.parse(s, dateFormatter))
        .leftMap(e =>
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
  ): F[Either[ParserError, ContractingAuthorityName]] = {
    val validPath = List(
      DelegatedPurchaseContractingAuthority,
      DirectPurchaseContractingAuthority,
      VeatPurchaseContractingAuthority
    ).map(p => p ++ OrgOfficialName)

    elem
      .firstTextOrError(validPath, "Contracting Authority Name")
      .map(s => ContractingAuthorityName(s))
      .pure[F]
  }

  override def parseContractingAuthorityCountry(
      elem: Elem
  ): F[Either[ParserError, Country]] = {
    val validPath = List(
      DelegatedPurchaseContractingAuthority,
      DirectPurchaseContractingAuthority,
      VeatPurchaseContractingAuthority
    ).map(p => p ++ CountryValue)

    elem
      .firstAttrOrError(validPath, "Contracting Authority Country")
      .map(s => Country.toDomain(s))
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
  ): F[List[Either[ParserError, ContractID]]] = {

    val validPath = List(AwardOfContract, VeatAwardOfContact)
    val children = elem.childrenAtAll(validPath)
    val paths = List(ContractNumber, LotNumber)

    val contractID = children
      .map(e =>
        e.firstTextOrError(List(ContractNumber), "Contract/Lot ID")
          .map(s => ContractID(s))
      )
    val lotID = children
      .map(e =>
        e.firstTextOrError(List(LotNumber), "Contract/Lot ID")
          .map(s => ContractID(s))
      )
    contractID
      .zip(lotID)
      .map(e =>
        e match
          case (Right(cID), Right(lID)) => Right(ContractID(s"$cID-lot:$lID"))
          case (Left(cID), Right(lID))  => Right(ContractID(s"$lID"))
          case (Right(cID), Left(lID))  => Right(ContractID(s"$cID"))
          case (Left(cID), Left(lID)) =>
            Left(ParserError.MissingField("Contract and LotID"))
      )
      .pure[F]
  }

  override def parseTenderLotTitle(
      elem: Elem
  ): F[List[Either[ParserError, Title]]] = {

    val lotTitle = elem
      .childrenAt(AwardOfContract)
      .map(x => x.textAtOrError(LotTitle, "Title").map(s => Title(s)))
    val validPath = List(ContractAwardInfo, VeatAwardInfo)
    val children = elem.childrenAtAll(validPath)
    val item = TitleContract

    if (lotTitle.length > 1 && lotTitle.forall(x => x.isRight)) then
      lotTitle.pure[F]
    else
      children
        .getTextsAt(validPath, item, "Title")
        .map(e => e.map(s => Title(s)))
        .pure[F]

  }

  override def parseTenderLotDescription(
      elem: Elem
  ): F[List[Either[ParserError, Description]]] = {

    val validPath = List(ContractAwardInfo, VeatAwardInfo)
    val children = elem.childrenAtAll(validPath)
    val item = ShortContractDescription

    children
      .getTextsAt(validPath, item, "Description")
      .map(e => e.map(s => Description(s)))
      .pure[F]

  }

  // TODO HERE
  override def parseTenderLotValue(
      elem: Elem
  ): F[List[Either[ParserError, Money]]] = {
    val validPath = List(AwardOfContract, VeatAwardOfContact)
    val children = elem.childrenAtAll(validPath)

    val values = children
      .getTextsAt(
        validPath,
        XMLPath(
          "CONTRACT_VALUE_INFORMATION",
          "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE",
          "VALUE_COST"
        ),
        "Amount"
      )
      .map(either =>
        either.flatMap(s =>
          Try(
            BigDecimal(s.replace(" ", "").replace(" ", "").replace(",", "."))
          ).toEither
            .leftMap(t =>
              ParserError.InvalidFormat(
                "Amount",
                "number",
                s,
                Some(validPath.showAltPath())
              )
            )
        )
      )
    val currencies = children
      .getAttrsAt(
        validPath,
        XMLPath(
          "CONTRACT_VALUE_INFORMATION",
          "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE"
        ) attr "CURRENCY",
        "Currency"
      )
      .map(either =>
        either.flatMap(s =>
          (Currency(s.replace(" ", "").replace(",", "."))(
            defaultMoneyContext
          ).toEither)
            .leftMap(t =>
              ParserError.InvalidFormat(
                "Currency",
                "Number",
                s,
                Some(validPath.map(_.show).mkString("|"))
              )
            )
        )
      )

    values
      .zip(currencies)
      .map((v, c) =>
        (v, c) match
          case (Right(x), Right(y)) => Right(Money(x, y))
          case (Left(e), _)         => Left(e)
          case (Right(x), Left(e))  => Left(e)
      )
      .pure[F]

  }

  override def parseTenderLotAwardedSupplierName(
      elem: Elem
  ): F[List[Either[ParserError, AwardedSupplierName]]] = {
    val validPath = List(AwardOfContract, VeatAwardOfContact)
    val children = elem.childrenAtAll(validPath)

    children
      .getTextsAt(
        validPath,
        EconomicOperator.OrganisationOfficialName,
        "Awarded Supplier Name"
      )
      .map(either => either.map(s => AwardedSupplierName(s)))
      .pure[F]

  }

  override def parseTenderLotAwardedSupplierCountry(
      elem: Elem
  ): F[List[Either[ParserError, Country]]] = {
    val validPath = List(AwardOfContract, VeatAwardOfContact)
    val children = elem.childrenAtAll(validPath)
    children
      .getAttrsAt(
        validPath,
        EconomicOperator.CountryValue,
        "Awarded Supplier Country"
      )
      .map(either => either.map(s => Country.toDomain(s)))
      .pure[F]

  }

  override def parseTenderLotAwardedSupplier(
      elem: Elem
  ): F[List[Either[ParserError, AwardedSupplier]]] = {
    val awardedSupplierName = parseTenderLotAwardedSupplierName(elem)
    val country = parseTenderLotAwardedSupplierCountry(elem)

    (awardedSupplierName, country)
      .mapN((l1, l2) =>
        l1.zip(l2)
          .map((maybeName, maybeCountry) =>
            (maybeName, maybeCountry) match
              case (Right(name), Right(country)) =>
                Right(AwardedSupplier(name, country))
              case (Left(e), _) => Left(e)
              case (_, Left(e)) => Left(e)
          )
      )

  }

  override def parseTenderLotJustification(
      e: Elem
  ): F[List[Either[ParserError, Justification]]] = {
    val validPath = List(ContractAwardJustification, VeatAwardJustification)
    val children = e.childrenAtAll(validPath)

    children
      .getTextsAt(validPath, JustificationReason, "Justification")
      .map(either => either.map(s => Justification(s)))
      .pure[F]
  }

}
