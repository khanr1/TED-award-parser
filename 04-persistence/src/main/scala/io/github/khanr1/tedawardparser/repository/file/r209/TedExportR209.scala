package io.github.khanr1.tedawardparser
package repository
package file
package r209

import cats.data.EitherT
import cats.Monad
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.file.Matching.attrValue
import io.github.khanr1.tedawardparser.repository.file.r209.*
import io.github.khanr1.tedawardparser.repository.file.XMLPathUtils.*
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import scala.util.Try
import scala.xml.Elem
import squants.market.*
import squants.market.defaultMoneyContext.*
import io.github.khanr1.tedawardparser.repository.file.r209.R209Path.ContractValue

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
    .textAtOrError(R209Path.OjsID, "OjsID")
    .map(s => OJSNoticeID(s))
    .pure[F]

  override def parsePublicationDate(
      elem: Elem
  ): F[Either[ParserError, LocalDate]] = elem
    .textAtOrError(R209Path.PublicationDate, "Publication Date")
    .flatMap { s =>
      Either
        .catchNonFatal(LocalDate.parse(s, dateFormatter))
        .leftMap(e =>
          ParserError.InvalidFormat(
            "Publication Date",
            "yyyyMMdd",
            s,
            Some(R209Path.PublicationDate.show)
          )
        )
    }
    .pure[F]

  override def parseContractingAuthorityName(
      elem: Elem
  ): F[Either[ParserError, ContractingAuthorityName]] = {
    val validPath = List(
      R209Path.DirectPurchaseContractingAuthority,
      R209Path.VeatPurchaseContractingAuthority
    ).map(p => p ++ R209Path.EconomicOperator.OfficialNameLeaf)

    elem
      .firstTextOrError(validPath, "Contracting Authority Name")
      .map(s => ContractingAuthorityName(s))
      .pure[F]
  }

  override def parseContractingAuthorityCountry(
      elem: Elem
  ): F[Either[ParserError, Country]] = {
    val validPath = List(
      R209Path.DirectPurchaseContractingAuthority,
      R209Path.VeatPurchaseContractingAuthority
    ).map(p => p ++ R209Path.EconomicOperator.CountryValue)

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
    val validPath = List(R209Path.AwardOfContract, R209Path.VeatAwardOfContact)
    val children = validPath.flatMap(p => elem.childrenAt(p))
    val paths = List(R209Path.LotNo, R209Path.ContractNo)
    val ids: List[Option[ContractID]] = children.map { e =>
      val lot = e.textAt(R209Path.LotNo).map(ContractID.apply)
      val contract = e.textAt(R209Path.ContractNo).map(ContractID.apply)
      // ITEM is on the award containers (roots), not on each child:
      val item = elem
        .firstAttr(
          List(
            R209Path.AwardOfContract attr "ITEM",
            R209Path.VeatAwardOfContact attr "ITEM"
          )
        )
        .map(ContractID.apply)
      contract.orElse(lot).orElse(item)
    }
    ids
      .map(
        _.toRight(
          ParserError
            .MissingField("Contract/Lot ID", Some(validPath.showAltPath()))
        )
      )
      .pure[F]
  }

  override def parseTenderLotTitle(
      elem: Elem
  ): F[List[Either[ParserError, Title]]] = elem
    .extractManyWithFallback(
      List(R209Path.AwardOfContract, R209Path.VeatAwardOfContact),
      List(R209Path.ContractAwardInfo, R209Path.VeatAwardInfo),
      R209Path.Title,
      R209Path.Title,
      "Title"
    )
    .map(e => e.map(Title(_)))
    .pure[F]

  override def parseTenderLotDescription(
      elem: Elem
  ): F[List[Either[ParserError, Description]]] = {
    val validPath =
      List(R209Path.ContractAwardInfo, R209Path.VeatAwardInfo).map(p =>
        p ++ R209Path.ObjectDescr
      )
    val description = elem
      .allTextAtOrError(validPath, R209Path.ShortDescr, "Description")
      .map(either => either.map(s => Description(s)))

    description.pure[F]
  }

  override def parseTenderLotValue(
      elem: Elem
  ): F[List[Either[ParserError, Money]]] = {
    val values = elem
      .extractManyWithFallback(
        List(R209Path.AwardOfContract, R209Path.VeatAwardOfContact),
        List(R209Path.ContractAwardInfo, R209Path.VeatAwardInfo),
        R209Path.ContractValue.PrimaryValueTotal,
        R209Path.ContractValue.FallbackValueTotal,
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
                Some(
                  (List(
                    R209Path.AwardOfContract,
                    R209Path.VeatAwardOfContact
                  ) ++ List(
                    R209Path.ContractAwardInfo,
                    R209Path.VeatAwardInfo
                  )).showAltPath()
                )
              )
            )
        )
      )

    val currencies = elem
      .extractManyAttrWithFallback(
        List(R209Path.AwardOfContract, R209Path.VeatAwardOfContact),
        List(R209Path.ContractAwardInfo, R209Path.VeatAwardInfo),
        R209Path.ContractValue.PrimaryCurrencyAttr,
        ContractValue.FallbackCurrencyAttr,
        "CURRENCY",
        "Currency"
      )
      .map(either =>
        either.flatMap(s =>
          Currency(s.replace(" ", "").replace(",", "."))(
            defaultMoneyContext
          ).toEither.leftMap(t =>
            ParserError.InvalidFormat(
              "Currency",
              "Number",
              s,
              Some(
                (List(
                  R209Path.AwardOfContract,
                  R209Path.VeatAwardOfContact
                ) ++ List(
                  R209Path.ContractAwardInfo,
                  R209Path.VeatAwardInfo
                )).showAltPath()
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
  // TODO
  override def parseTenderLotAwardedSupplierName(
      elem: Elem
  ): F[List[Either[ParserError, AwardedSupplierName]]] = {
    val primaryPath =
      List(
        R209Path.AwardOfContract ++ R209Path.FormSection.ContractAward.EconomicOperator.PrimaryBase,
        R209Path.VeatAwardOfContact ++ R209Path.FormSection.Veat.EconomicOperator.PrimaryBase
      )
    val fallback =
      List(
        R209Path.AwardOfContract ++ R209Path.FormSection.ContractAward.EconomicOperator.FallbackBase,
        R209Path.VeatAwardOfContact ++ R209Path.FormSection.Veat.EconomicOperator.FallbackBase
      )
    val names = elem
      .extractManyWithFallback(
        primaryPath,
        fallback,
        R209Path.EconomicOperator.OfficialNameLeaf,
        R209Path.EconomicOperator.OfficialNameLeaf,
        "Awarded Supplier Name"
      )
      .map(either => either.map(s => AwardedSupplierName(s)))

    names.pure[F]
  }

  override def parseTenderLotAwardedSupplierCountry(
      elem: Elem
  ): F[List[Either[ParserError, Country]]] = {
    val primaryPath =
      List(
        R209Path.AwardOfContract ++ R209Path.FormSection.ContractAward.EconomicOperator.PrimaryBase,
        R209Path.VeatAwardOfContact ++ R209Path.FormSection.Veat.EconomicOperator.PrimaryBase
      )
    val fallback =
      List(
        R209Path.AwardOfContract ++ R209Path.FormSection.ContractAward.EconomicOperator.FallbackBase,
        R209Path.VeatAwardOfContact ++ R209Path.FormSection.Veat.EconomicOperator.FallbackBase
      )
    val countries = elem
      .extractManyAttrWithFallback(
        primaryPath,
        fallback,
        R209Path.EconomicOperator.CountryValue,
        R209Path.EconomicOperator.CountryValue,
        "VALUE",
        "Awarded Supplier Country"
      )
      .map(either => either.map(s => Country.toDomain(s)))

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
    val validPath =
      List(R209Path.ContractAwardProcedure, R209Path.VeatAwardProcedure)
    val tagelem = R209Path.ProcedureDirective.JustificationLeaf
    val justification = e
      .allTextAtOrError(
        validPath,
        tagelem,
        "Justification"
      )
      .map(either => either.map(s => Justification(s)))

    justification.pure[F]
  }

}
