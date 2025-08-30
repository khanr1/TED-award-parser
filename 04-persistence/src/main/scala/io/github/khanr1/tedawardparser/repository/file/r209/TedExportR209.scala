package io.github.khanr1.tedawardparser
package repository
package file
package r209

import cats.data.EitherT
import cats.Monad
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.file.Matching.attrValue
import io.github.khanr1.tedawardparser.repository.file.r209.R209Path.*
import io.github.khanr1.tedawardparser.repository.file.XMLPathUtils.*
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import scala.util.Try
import scala.xml.Elem
import squants.market.*
import squants.market.defaultMoneyContext.*

class TedExportR209[F[_]: Monad] extends XMLParser[F] {

  // Reusable helpers (put next to your XMLPath utilities)

  private inline def pathsLabel(paths: List[XMLPath]): String =
    paths.map(_.show).mkString("|")

  private def childrenAtAll(elem: Elem, paths: List[XMLPath]): List[Elem] =
    paths.flatMap(elem.childrenAt(_))

  /** Extract multiple values under some sections. If at least one Right is
    * found in primary sections, return those results (Rights + Lefts).
    * Otherwise, try fallback sections (and return those results). This
    * preserves partial success while giving a clean fallback story.
    */
  def extractManyWithFallback[A](
      elem: Elem,
      primary: List[XMLPath],
      fallback: List[XMLPath],
      itemTag: XMLPath,
      fallbackItemTag: XMLPath,
      field: String
  )(decode: String => A): List[Either[ParserError, A]] = {

    val primaryChildren = childrenAtAll(elem, primary)

    val primaryResults: List[Either[ParserError, A]] =
      primaryChildren.map { e =>
        e.textAt(itemTag)
          .toRight(ParserError.MissingField(field, Some(pathsLabel(primary))))
          .map(decode)
      }

    val hasAnyRight = primaryResults.exists(_.isRight)
    if (hasAnyRight || fallback.isEmpty) primaryResults
    else {
      val fallbackChildren = childrenAtAll(elem, fallback)
      fallbackChildren.map { e =>
        e.textAt(fallbackItemTag)
          .toRight(
            ParserError.MissingField(
              field,
              Some(pathsLabel(primary ++ fallback))
            )
          )
          .map(decode)
      }
    }
  }

  /** Extract multiple values under some sections. If at least one Right is
    * found in primary sections, return those results (Rights + Lefts).
    * Otherwise, try fallback sections (and return those results). This
    * preserves partial success while giving a clean fallback story.
    */
  def extractManyAttrWithFallback[A](
      elem: Elem,
      primary: List[XMLPath],
      fallback: List[XMLPath],
      itemTag: XMLPath,
      fallbackItemTag: XMLPath,
      attr: String,
      fallbackAttr: String,
      field: String
  )(decode: String => A): List[Either[ParserError, A]] = {

    val primaryChildren = childrenAtAll(elem, primary)

    val primaryResults: List[Either[ParserError, A]] =
      primaryChildren.map { e =>
        e.attrAt(itemTag attr attr)
          .toRight(ParserError.MissingField(field, Some(pathsLabel(primary))))
          .map(decode)
      }

    val hasAnyRight = primaryResults.exists(_.isRight)
    if (hasAnyRight || fallback.isEmpty) primaryResults
    else {
      val fallbackChildren = childrenAtAll(elem, fallback)
      fallbackChildren.map { e =>
        e.attrAt(fallbackItemTag attr fallbackAttr)
          .toRight(
            ParserError.MissingField(
              field,
              Some(pathsLabel(primary ++ fallback))
            )
          )
          .map(decode)
      }
    }
  }
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
      DirectPurchaseContractingAuthority,
      VeatPurchaseContractingAuthority
    ).map(p => p / "OFFICIALNAME")

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
      val lot = e.textAt(XMLPath("LOT_NO")).map(ContractID(_))
      val contract = e.textAt(XMLPath("CONTRACT_NO")).map(ContractID(_))
      val item =
        elem.firstAttr(validPath.map(p => p.attr("ITEM"))).map(ContractID(_))
      contract.orElse(lot) orElse (item)
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
  ): F[List[Either[ParserError, Title]]] = extractManyWithFallback(
    elem,
    List(AwardOfContract, VeatAwardOfContact),
    List(ContractAwardInfo, VeatAwardInfo),
    XMLPath("TITLE"),
    XMLPath("TITLE"),
    "Title"
  )(Title.apply).pure[F]

  override def parseTenderLotDescription(
      elem: Elem
  ): F[List[Either[ParserError, Description]]] = {
    val validPath =
      List(ContractAwardInfo, VeatAwardInfo).map(p => p / "OBJECT_DESCR")
    val description = elem
      .allTextAt(validPath, XMLPath("SHORT_DESCR"))
      .map(maybeDescr =>
        maybeDescr
          .toRight(
            ParserError.MissingField(
              "Description",
              Some(validPath.map(x => x.show).mkString("|"))
            )
          )
          .map(x => Description(x))
      )
    description.pure[F]
  }

  override def parseTenderLotValue(
      elem: Elem
  ): F[List[Either[ParserError, Money]]] = {
    val values = extractManyWithFallback(
      elem,
      List(AwardOfContract, VeatAwardOfContact),
      List(ContractAwardInfo, VeatAwardInfo),
      XMLPath(
        "AWARDED_CONTRACT",
        "VALUES",
        "VAL_TOTAL"
      ),
      XMLPath("VAL_TOTAL"),
      "Amount"
    )((x: String) => x)
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
                Some(
                  (List(AwardOfContract, VeatAwardOfContact) ++ List(
                    ContractAwardInfo,
                    VeatAwardInfo
                  )).map(_.show).mkString("|")
                )
              )
            )
        )
      )

    val currencies = extractManyAttrWithFallback(
      elem,
      List(AwardOfContract, VeatAwardOfContact),
      List(ContractAwardInfo, VeatAwardInfo),
      XMLPath(
        "AWARDED_CONTRACT",
        "VALUES",
        "VAL_TOTAL"
      ),
      XMLPath("VAL_TOTAL"),
      "CURRENCY",
      "CURRENCY",
      "Currency"
    )((x: String) => x).map(either =>
      either.flatMap(s =>
        Currency(s.replace(" ", "").replace(",", "."))(
          defaultMoneyContext
        ).toEither.leftMap(t =>
          ParserError.InvalidFormat(
            "Currency",
            "Number",
            s,
            Some(
              (List(AwardOfContract, VeatAwardOfContact) ++ List(
                ContractAwardInfo,
                VeatAwardInfo
              )).map(_.show).mkString("|")
            )
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
    val primaryPath = List(AwardOfContract, VeatAwardOfContact).map(p =>
      p / "AWARDED_CONTRACT" /
        "CONTRACTORS" /
        "CONTRACTOR" /
        "ADDRESS_CONTRACTOR"
    )
    val fallback = List(AwardOfContract, VeatAwardOfContact).map(p =>
      p / "AWARDED_CONTRACT" / "CONTRACTOR" /
        "ADDRESS_CONTRACTOR"
    )
    val names = extractManyWithFallback(
      elem,
      primary = primaryPath,
      fallback = fallback,
      XMLPath("OFFICIALNAME"),
      XMLPath("OFFICIALNAME"),
      "Awarded Supplier Name"
    )(s => AwardedSupplierName(s))

    names.pure[F]
  }

  override def parseTenderLotAwardedSupplierCountry(
      elem: Elem
  ): F[List[Either[ParserError, Country]]] = {
    val primaryPath = List(AwardOfContract, VeatAwardOfContact).map(p =>
      p / "AWARDED_CONTRACT" /
        "CONTRACTORS" /
        "CONTRACTOR" /
        "ADDRESS_CONTRACTOR"
    )
    val fallback = List(AwardOfContract, VeatAwardOfContact).map(p =>
      p / "AWARDED_CONTRACT" / "CONTRACTOR" /
        "ADDRESS_CONTRACTOR"
    )
    val countries = extractManyAttrWithFallback(
      elem,
      primary = primaryPath,
      fallback = fallback,
      XMLPath("COUNTRY"),
      XMLPath("COUNTRY"),
      "VALUE",
      "VALUE",
      "Awarded Supplier Country"
    )(s => Country.toDomain(s))

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
    val validPath = List(ContractAwardProcedure, VeatAwardProcedure)
    val tagelem = XMLPath(
      "DIRECTIVE_2014_24_EU",
      "PT_NEGOTIATED_WITHOUT_PUBLICATION",
      "D_JUSTIFICATION"
    )
    val justification = e
      .allTextAt(
        validPath,
        tagelem
      )
      .map(maybeString =>
        maybeString
          .toRight(
            ParserError
              .MissingField(
                "Justification",
                Some((validPath.map(x => x ++ tagelem)).show.mkString("|"))
              )
          )
          .map(s => Justification(s))
      )
    justification.pure[F]
  }

}
