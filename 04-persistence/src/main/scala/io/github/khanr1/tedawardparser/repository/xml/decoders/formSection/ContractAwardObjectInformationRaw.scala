package io.github.khanr1.tedawardparser
package repository
package xml
package decoders
package formSection

import scala.xml.Elem
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*
import io.github.khanr1.tedawardparser.repository.xpath.{
  FormSectionPathR208,
  FormSectionPathR209
}

final case class ContractAwardObjectInformationRaw(
    titleOfContract: Either[ParserError, String],
    contractDescription: Either[ParserError, String],
    totalValue: Either[ParserError, String]
) extends Raw

object ContractAwardObjectInformationDecoder208:
  given XMLDecoder[ContractAwardObjectInformationRaw] =
    new XMLDecoder[ContractAwardObjectInformationRaw] {

      override def decode(e: Elem): ContractAwardObjectInformationRaw = {
        val path = e
          .nodesAt(FormSectionPathR208.F03.root)
          .headOption
          .map(x => x.label) match
          case Some("FD_CONTRACT_AWARD") =>
            io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F03.ContractAwardObjectInformationPath
          case _ =>
            io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F15.ContractAwardObjectInformationPath

        import path.*

        val titleOfContract = e.textAtOrError(
          path.titlePath,
          "Contract Title"
        )
        val contractDescription =
          e.textAtOrError(
            path.descriptionPath,
            "Description"
          )
        val amount = e
          .textAtOrError(
            path.valuePath,
            "Contract Value"
          )
          .map(s => s.replace(" ", "").replace(",", "."))

        val currency: Either[ParserError, String] = e
          .attrAt(path.currencyPath)
          .toRight(
            ParserError.MissingField(
              "Currency",
              Some(
                (path.currencyPath).show
              )
            )
          )
          .flatMap(s =>
            Right(
              s.replace(" ", "")
                .replace(",", ".")
            )
          )
        val totalValue = (amount, currency) match
          case (Right(v), Right(c)) => Right(v + " " + c)
          case (Right(v), Left(c))  => Left(c)
          case (Left(c), _)         => Left(c)

        ContractAwardObjectInformationRaw(
          titleOfContract,
          contractDescription,
          totalValue
        )

      }

    }

object ContractAwardObjectInformationDecoder209:
  given XMLDecoder[ContractAwardObjectInformationRaw] =
    new XMLDecoder[ContractAwardObjectInformationRaw] {

      override def decode(e: Elem): ContractAwardObjectInformationRaw = {
        val path = e
          .nodesAt(FormSectionPathR209.F03.root)
          .headOption
          .map(x => x.label) match
          case Some("F03_2014") =>
            io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR209.F03.ContractAwardObjectInformationPath
          case _ =>
            io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR209.F15.ContractAwardObjectInformationPath

        import path.*

        val titleOfContract = e.textAtOrError(
          path.titlePath,
          "Contract Title"
        )
        val contractDescription =
          e.textAtOrError(
            path.descriptionPath,
            "Description"
          )
        val amount = e
          .textAtOrError(
            path.valuePath,
            "Contract Value"
          )
          .map(s => s.replace(" ", "").replace(",", "."))

        val currency: Either[ParserError, String] = e
          .attrAt(path.currencyPath)
          .toRight(
            ParserError.MissingField(
              "Currency",
              Some(
                (path.currencyPath).show
              )
            )
          )
          .flatMap(s =>
            Right(
              s.replace(" ", "")
                .replace(",", ".")
            )
          )
        val totalValue = (amount, currency) match
          case (Right(v), Right(c)) => Right(v + " " + c)
          case (Right(v), Left(c))  => Left(c)
          case (Left(c), _)         => Left(c)

        ContractAwardObjectInformationRaw(
          titleOfContract,
          contractDescription,
          totalValue
        )

      }

    }
