package io.github.khanr1.tedawardparser
package repository
package file
package r208

import cats.data.EitherT
import cats.Monad
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.file.Matching.attrValue
import io.github.khanr1.tedawardparser.repository.file.r208.R208Path.*
import io.github.khanr1.tedawardparser.repository.file.XMLPathUtils.*
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import scala.util.Try
import scala.xml.Elem
import squants.market.*
import squants.market.defaultMoneyContext.*

class TedExportR208[F[_]: Monad] extends XMLParser[F] {

  override def dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

  override def parseOJSNoticeID(
      elem: Elem
  ): F[Either[ParserError, OJSNoticeID]] = elem
    .textAt(OjsID)
    .toRight(ParserError.MissingField("OjsID", Some(OjsID.show)))
    .map(s => OJSNoticeID(s))
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
    ).map(p => p / "ORGANISATION" / "OFFICIALNAME")

    elem
      .firstText(validPath)
      .toRight(
        ParserError.MissingField(
          "Contracting Authority Name",
          Some(validPath.map(_.show).mkString("|"))
        )
      )
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
    ).map(p => p / "COUNTRY").map(p => p attr ("VALUE"))

    elem
      .firstAttr(validPath)
      .toRight(
        ParserError.MissingField(
          "Contracting Authority Country",
          Some(validPath.map(_.show).mkString("|"))
        )
      )
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
    val children = validPath.flatMap(p => elem.childrenAt(p))
    val ids: List[Option[ContractID]] = children.map(e => {
      val lot = e.textAt(XMLPath("LOT_NUMBER")).map(ContractID(_))
      val contract = e.textAt(XMLPath("CONTRACT_NUMBER")).map(ContractID(_))
      contract.orElse(lot)
    })
    ids
      .map(x =>
        x.toRight(
          ParserError.MissingField(
            "Contract/Lot ID",
            Some(validPath.map(_.show).mkString("|"))
          )
        )
      )
      .pure[F]

  }

  override def parseTenderLotTitle(
      elem: Elem
  ): F[List[Either[ParserError, Title]]] = {
    val validPath = List(ContractAwardInfo, VeatAwardInfo)
    val children = validPath.flatMap(p => elem.childrenAt(p))
    val titles = children.map { e =>
      e
        .textAt(XMLPath("TITLE_CONTRACT"))
        .toRight(
          ParserError
            .MissingField("Title", Some(validPath.map(_.show).mkString("|")))
        )
        .map(s => Title(s))
    }
    titles.pure[F]

  }

  override def parseTenderLotDescription(
      elem: Elem
  ): F[List[Either[ParserError, Description]]] = {
    val validPath = List(ContractAwardInfo, VeatAwardInfo)
    val children = validPath.flatMap(p => elem.childrenAt(p))
    val descriptions = children
      .map(e =>
        e.textAt(XMLPath("SHORT_CONTRACT_DESCRIPTION"))
          .toRight(
            ParserError.MissingField(
              "Description",
              Some(validPath.map(_.show).mkString("|"))
            )
          )
          .map(s => Description(s))
      )

    descriptions.pure[F]

  }

  override def parseTenderLotValue(
      elem: Elem
  ): F[List[Either[ParserError, Money]]] = {
    val validPath = List(AwardOfContract, VeatAwardOfContact)
    val children = validPath.flatMap(p => elem.childrenAt(p))

    val values = children.map(e =>
      e.textAt(
        XMLPath(
          "CONTRACT_VALUE_INFORMATION",
          "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE",
          "VALUE_COST"
        )
      ).toRight(
        ParserError
          .MissingField(
            "Amount",
            None /*Some(validPath.map(_.show).mkString("|"))*/
          )
      ).flatMap(s =>
        Try(
          BigDecimal(s.replace(" ", "").replace(" ", "").replace(",", "."))
        ).toEither
          .leftMap(t =>
            ParserError.InvalidFormat(
              "Amount",
              "number",
              s,
              Some(validPath.map(_.show).mkString("|"))
            )
          )
      )
    )
    val currencies = children.map(e =>
      e.attrAt(
        XMLPath(
          "CONTRACT_VALUE_INFORMATION",
          "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE"
        ) attr "CURRENCY"
      ).toRight(
        ParserError
          .MissingField("Currency", Some(validPath.map(_.show).mkString("|")))
      ).flatMap(s =>
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
    val children = validPath.flatMap(p => elem.childrenAt(p))

    val names = children.map(e =>
      e.textAt(
        XMLPath(
          "ECONOMIC_OPERATOR_NAME_ADDRESS",
          "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME",
          "ORGANISATION",
          "OFFICIALNAME"
        )
      ).toRight(
        ParserError.MissingField(
          "Awarded Supplier Name",
          Some(validPath.map(_.show).mkString("|"))
        )
      ).map(x => AwardedSupplierName(x))
    )

    names.pure[F]
  }

  override def parseTenderLotAwardedSupplierCountry(
      elem: Elem
  ): F[List[Either[ParserError, Country]]] = {
    val validPath = List(AwardOfContract, VeatAwardOfContact)
    val children = validPath.flatMap(p => elem.childrenAt(p))

    val countries = children.map { e =>
      e.attrAt(
        XMLPath(
          "ECONOMIC_OPERATOR_NAME_ADDRESS",
          "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME",
          "COUNTRY"
        ) attr "VALUE"
      ).toRight(
        ParserError.MissingField(
          "Awarded Supplier Country",
          Some(validPath.map(_.show).mkString("|"))
        )
      ).map(s => Country.toDomain(s))
    }
    countries.pure[F]
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
    val children = validPath.flatMap(p => e.childrenAt(p))

    val justifications = children.map(e =>
      e.textAt(XMLPath("REASON_CONTRACT_LAWFUL"))
        .toRight(
          ParserError.MissingField(
            "Justification",
            Some(validPath.map(_.show).mkString("|"))
          )
        )
        .map(s => Justification(s))
    )
    justifications.pure[F]
  }

}
