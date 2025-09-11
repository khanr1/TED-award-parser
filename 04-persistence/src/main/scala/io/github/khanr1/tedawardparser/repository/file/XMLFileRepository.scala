package io.github.khanr1.tedawardparser
package repository
package file

import repository.NoticeRepository
import fs2.{text, Pipe}
import fs2.io.file.{Path, Files}
import scala.xml.{Elem, XML}
import cats.syntax.all.*
import scala.collection.View.Elems
import squants.Money
import java.time.LocalDate
import cats.data.EitherT
import io.github.khanr1.tedawardparser.models.*
import io.github.khanr1.tedawardparser.repository.file.ParserSelect.detect
import io.github.khanr1.tedawardparser.repository.parsers.ParserError

object XMLFileRepository {
  def make[F[_]: cats.effect.Concurrent: Files](
      path: Path
  ): NoticeRepository[F] = new NoticeRepository[F] {

    override def getAll: fs2.Stream[F, PartialNotice] = xmlElems.evalMap { e =>

      val parser = detect(e)

      extension [A](fEither: F[Either[ParserError, A]])
        def toDomainError(field: String): F[Either[DomainError, A]] =
          fEither.map(either =>
            either.leftMap(e =>
              e match
                case ParserError.MissingField(field, at) =>
                  DomainError.Missing(field)
                case ParserError.InvalidFormat(field, expected, found, at) =>
                  DomainError.Invalid(field, found)
                case ParserError.MultipleValues(field, at) =>
                  DomainError.Unexpected(field)
                case ParserError.UnexpectedNode(node, at) =>
                  DomainError.Unexpected(field)
                case ParserError.Unknown(why, at) =>
                  DomainError.Unexpected(why)
            )
          )

      extension [A](fListEither: F[List[Either[ParserError, A]]])
        def toDomainErrorL(field: String): F[List[Either[DomainError, A]]] =
          fListEither.map(leither =>
            leither.map(either =>
              either.leftMap(e =>
                e match
                  case ParserError.MissingField(field, at) =>
                    DomainError.Missing(field)
                  case ParserError.InvalidFormat(field, expected, found, at) =>
                    DomainError.Invalid(field, found)
                  case ParserError.MultipleValues(field, at) =>
                    DomainError.Unexpected(field)
                  case ParserError.UnexpectedNode(node, at) =>
                    DomainError.Unexpected(field)
                  case ParserError.Unknown(why, at) =>
                    DomainError.Unexpected(why)
              )
            )
          )

      val parseOJSNoticeID: F[Either[DomainError, OJSNoticeID]] =
        parser.parseOJSNoticeID(e).toDomainError("OjsNumber")
      val parsePublicationDate: F[Either[DomainError, LocalDate]] =
        parser.parsePublicationDate(e).toDomainError("Publication Date")
      val parseContractingAuthorityName
          : F[Either[DomainError, ContractingAuthorityName]] = parser
        .parseContractingAuthorityName(e)
        .toDomainError("Contracting Authority Name")
      val parseContractingAuthorityCountry: F[Either[DomainError, Country]] =
        parser
          .parseContractingAuthorityCountry(e)
          .toDomainError("Contracting Authority Country")
      val parseContractID: F[List[Either[DomainError, ContractID]]] =
        parser.parseContractID(e).toDomainErrorL("Contract ID")
      val parseTenderLotTitle: F[List[Either[DomainError, Title]]] =
        parser.parseTenderLotTitle(e).toDomainErrorL("Title")

      val parseTenderLotDescription: F[List[Either[DomainError, Description]]] =
        parser.parseTenderLotDescription(e).toDomainErrorL("Description")
      val parseTenderLotValue: F[List[Either[DomainError, squants.Money]]] =
        parser.parseTenderLotValue(e).toDomainErrorL("Value")
      val parseTenderLotAwardedSupplierName
          : F[List[Either[DomainError, AwardedSupplierName]]] = parser
        .parseTenderLotAwardedSupplierName(e)
        .toDomainErrorL("Awarded Supplier Name")
      val parseTenderLotAwardedSupplierCountry
          : F[List[Either[DomainError, Country]]] = parser
        .parseTenderLotAwardedSupplierCountry(e)
        .toDomainErrorL("Awarded supplier country")
      val parseTenderLotJustification
          : F[List[Either[DomainError, Justification]]] =
        parser.parseTenderLotJustification(e).toDomainErrorL("Justification")

      val partialContractingAuthority: F[PartialContractingAuthority] =
        for
          name <- parseContractingAuthorityName
          country <- parseContractingAuthorityCountry
        yield PartialContractingAuthority(name, country)

      val partialAwardedSupplier: F[List[PartialAwardedSupplier]] = (
        parseTenderLotAwardedSupplierName,
        parseTenderLotAwardedSupplierCountry
      ).mapN((l1, l2) =>
        l1.zipAll(
          l2,
          Left(
            DomainError.Unexpected(
              "The list of awarded Supplier name is shorter than the list of country"
            )
          ),
          Left(
            DomainError.Unexpected(
              "The list of awarded Supplier country is shorter than the list of name"
            )
          )
        ).map((en, ec) => PartialAwardedSupplier(en, ec))
      )

      // Build lots by aligning each parsed field by index, using mapN + zipAll
      val partialLotTender: F[List[PartialTenderLot]] = (
        parseContractID,
        parseTenderLotTitle,
        parseTenderLotDescription,
        parseTenderLotValue,
        parseTenderLotJustification,
        partialAwardedSupplier
      ).mapN { (ids, titles, descs, values, justifs, suppliers) =>
        val idDef = Left(DomainError.Missing("Contract ID"))
        val titleDef = Left(DomainError.Missing("Title"))
        val descDef = Left(DomainError.Missing("Description"))
        val valueDef = Left(DomainError.Missing("Value"))
        val justDef = Left(DomainError.Missing("Justification"))
        val suppDef = PartialAwardedSupplier(
          Left(DomainError.Missing("Awarded Supplier Name")),
          Left(DomainError.Missing("Awarded supplier country"))
        )

        val t2 =
          ids.zipAll(titles, idDef, titleDef).map { case (a, b) => (a, b) }
        val t3 = t2.zipAll(descs, (idDef, titleDef), descDef).map {
          case ((a, b), c) => (a, b, c)
        }
        val t4 = t3.zipAll(values, (idDef, titleDef, descDef), valueDef).map {
          case ((a, b, c), d) => (a, b, c, d)
        }
        val t5 = t4
          .zipAll(justifs, (idDef, titleDef, descDef, valueDef), justDef)
          .map { case ((a, b, c, d), e) => (a, b, c, d, e) }
        val t6 = t5
          .zipAll(
            suppliers,
            (idDef, titleDef, descDef, valueDef, justDef),
            suppDef
          )
          .map { case ((a, b, c, d, e), s) => (a, b, c, d, e, s) }

        t6.map { case (idE, titleE, descE, valueE, justE, supplier) =>
          val supplierOpt: Option[PartialAwardedSupplier] =
            (supplier.name, supplier.countryCode) match
              case (
                    Left(DomainError.Missing(_)),
                    Left(DomainError.Missing(_))
                  ) =>
                None
              case _ => Some(supplier)

          PartialTenderLot(
            id = idE,
            title = titleE,
            description = descE,
            value = valueE,
            awardedSupplier = supplierOpt,
            justification = justE
          )
        }
      }

      for
        ojs <- parseOJSNoticeID
        date <- parsePublicationDate
        auth <- partialContractingAuthority
        lots <- partialLotTender
      yield PartialNotice(ojs, date, auth, lots)

    }

    private val xmlFile = (path: Path) =>
      Files[F].walk(path).filter(p => p.extName == ".xml")

    private def fileToElem: Pipe[F, Path, Elem] = s =>
      s.evalMap(path =>
        Files[F]
          .readAll(path)
          .through(text.utf8.decode)
          .compile
          .string
          .map(content => XML.loadString(content))
      )

    private val xmlElems: fs2.Stream[F, Elem] =
      xmlFile(path).through(fileToElem)

  }
}
