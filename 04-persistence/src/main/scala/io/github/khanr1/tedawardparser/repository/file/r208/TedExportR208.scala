package io.github.khanr1.tedawardparser
package repository
package file
package r208

import cats.Monad
import cats.syntax.all.*
import scala.xml.Elem
import squants.Money
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import io.github.khanr1.tedawardparser.repository.file.XMLPathUtils.*
import io.github.khanr1.tedawardparser.repository.file.r208.R208Path.*
import cats.data.EitherT

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
