package io.github.khanr1.tedawardparser
package repository
package parsers
package ubl

import cats.data.EitherT
import cats.Monad
import cats.syntax.all.*
import cats.syntax.validated
import io.github.khanr1.tedawardparser.repository.parsers.Matching.attrValue
import io.github.khanr1.tedawardparser.repository.parsers.r208.R208Path.*
import io.github.khanr1.tedawardparser.repository.parsers.ubl.UBLPath.*
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.*
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import scala.util.matching.Regex
import scala.util.Try
import scala.xml.Elem
import squants.market.*
import squants.market.defaultMoneyContext.*

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
    val orgID = elem.textAt(pContractingOrgId, ns)
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
    val orgID = elem.textAt(pContractingOrgId, ns)
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
  ): F[Either[ParserError, ContractingAuthority]] =
    val name = EitherT(parseContractingAuthorityName(elem))
    val country = EitherT(parseContractingAuthorityCountry(elem))

    (name, country).mapN((n, c) => ContractingAuthority(n, c)).value

  override def parseContractID(
      elem: Elem
  ): F[List[Either[ParserError, ContractID]]] =
    elem
      .allTextAtOrError(List(pTenderLot), pTenderLotID, "Contract/Lot ID", ns)
      .map(e => e.map(s => ContractID(s)))
      .pure[F]

  override def parseTenderLotTitle(
      elem: Elem
  ): F[List[Either[ParserError, Title]]] =
    elem
      .allTextAtOrError(List(pTenderLot), pTenderLotName, "Title", ns)
      .map(e => e.map(s => Title(s)))
      .pure[F]

  override def parseTenderLotDescription(
      elem: Elem
  ): F[List[Either[ParserError, Description]]] =
    elem
      .allTextAtOrError(
        List(pTenderLot),
        pTenderLotDescription,
        "Desciption",
        ns
      )
      .map(e => e.map(s => Description(s)))
      .pure[F]

  override def parseTenderLotValue(
      elem: Elem
  ): F[List[Either[ParserError, squants.Money]]] = {
    val amounts = elem
      .childrenAt(UBLPath.pNoticeResult ++ UBLPath.pLotTender, ns)
      .map(e => e.textAtOrError(UBLPath.pLotTenderValue, "Amount value", ns))
      .map(either =>
        either.flatMap(s =>
          Try(
            BigDecimal(s.replace(" ", "").replace(" ", "").replace(",", "."))
          ).toEither.leftMap(error =>
            ParserError
              .InvalidFormat("Amount Value", " expected a number", s, None)
          )
        )
      )
    val totalAmount = elem
      .textAtOrError(
        UBLPath.pNoticeResult ++ UBLPath.pTotalValue,
        "Total Amount value",
        ns
      )
      .flatMap(s =>
        Try(
          BigDecimal(s.replace(" ", "").replace(" ", "").replace(",", "."))
        ).toEither.leftMap(error =>
          ParserError
            .InvalidFormat("Amount Value", " expected a number", s, None)
        )
      )
    val numberOfAwardedTender = elem
      .childrenAt(UBLPath.pNoticeResult ++ UBLPath.pLotTender, ns)
      .flatMap(e => e.textAt(UBLPath.pLotTenderID, ns))
      .length

    val currencies = elem
      .childrenAt(UBLPath.pNoticeResult ++ UBLPath.pLotTender, ns)
      .map(e =>
        e.attrAt(UBLPath.pLotTenderValue.attr("currencyID"), ns)
          .toRight(ParserError.MissingField("Currency", None))
      )
      .map(either =>
        either.flatMap(s =>
          Currency(s.replace(" ", "").replace(",", "."))(
            defaultMoneyContext
          ).toEither
            .leftMap(t => ParserError.InvalidFormat("Currency", "Number", s))
        )
      )
    val currency = elem
      .attrAt(
        UBLPath.pNoticeResult ++ UBLPath.pTotalValue.attr("currencyID"),
        ns
      )
      .toRight(ParserError.MissingField("Currency", None))
      .flatMap(s =>
        Currency(s.replace(" ", "").replace(",", "."))(
          defaultMoneyContext
        ).toEither.leftMap(t =>
          ParserError.InvalidFormat("Currency", "Number", s)
        )
      )

    if (amounts.nonEmpty && !amounts.forall(e => e.isLeft)) then
      amounts
        .zip(currencies)
        .map((v, c) =>
          (v, c) match
            case (Right(x), Right(y)) => Right(Money(x, y))
            case (Left(e), _)         => Left(e)
            case (Right(x), Left(e))  => Left(e)
        )
        .pure[F]
    else
      val tmpAmounts = List.fill(numberOfAwardedTender)(
        totalAmount.map(x => x / numberOfAwardedTender)
      )

      val tmpCurrencies = List.fill(numberOfAwardedTender)(currency)

      tmpAmounts
        .zip(tmpCurrencies)
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
