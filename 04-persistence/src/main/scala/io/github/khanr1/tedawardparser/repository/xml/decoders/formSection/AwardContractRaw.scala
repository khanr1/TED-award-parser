package io.github.khanr1.tedawardparser.repository.xml
package decoders
package formSection

import scala.xml.Elem
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPath
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR209
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F03.AwardContractPath.contractNumberPath
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F03.AwardContractPath.contractTitlePath
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F03.AwardContractPath.lotNumberPath
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F03.AwardContractPath.awardDatePath
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F03.AwardContractPath.contractorPath
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F03.AwardContractPath.contractValueCurrencyPath
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F03.AwardContractPath.contractValueAmountPath

final case class AwardContractRaw(
    contractNumber: Either[ParserError, String],
    lotNumber: Either[ParserError, String],
    contractTitle: Either[ParserError, String],
    awardDate: Either[ParserError, String],
    contractor: Either[ParserError, String],
    contractValue: Either[ParserError, String]
) extends Raw

object AwardContractDecoder208:
  given XMLDecoder[AwardContractRaw] =
    new XMLDecoder[AwardContractRaw] {

      override def decode(e: Elem): AwardContractRaw =
        val path = e
          .nodesAt(FormSectionPathR208.F03.root)
          .headOption
          .map(x => x.label) match
          case Some("FD_CONTRACT_AWARD") =>
            io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F03.ContractingAuthorityPath
          case _ =>
            io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F03.ContractingAuthorityPath

        import path.*

        val contractNumber =
          e.textAtOrError(contractNumberPath, "Contract Number")
        val lotNumber =
          e.textAtOrError(lotNumberPath, "Lot Number")
        val contractTitle =
          e.textAtOrError(contractTitlePath, "Contract Title")
        val awardDate = e.textAtOrError(awardDatePath, "Award Date")
        val contractor = e.textAtOrError(contractorPath, "Contractor")
        val currency =
          e.attrAt(contractValueCurrencyPath)
        val value = e
          .textAtOrError(contractValueAmountPath, "Amount")

        val contractValue = (currency, value) match
          case (Some(c), Right(v)) => Right(s"$v $c")
          case _ => Left(ParserError.Unknown("not proper currency", None))

        AwardContractRaw(
          contractNumber,
          lotNumber,
          contractTitle,
          awardDate,
          contractor,
          contractValue
        )
    }

object AwardContractDecoder209:
  given XMLDecoder[ContractingAuthorityRaw] =
    new XMLDecoder[ContractingAuthorityRaw] {

      override def decode(e: Elem): ContractingAuthorityRaw =
        val path = e
          .nodesAt(FormSectionPathR209.F03.root)
          .headOption
          .map(x => x.label) match
          case Some("F03_2014") =>
            io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR209.F03.ContractingAuthorityPath
          case _ =>
            io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR209.F15.ContractingAuthorityPath

        import path.*

        val officialName =
          e.textAtOrError(officialNamePath, "Official Name")
        val nationalID =
          e.textAtOrError(nationalIDPath, "National ID")
        val adress = e.textAtOrError(addressPath, "Address")
        val town = e.textAtOrError(townPath, "Town")
        val postalCode =
          e.textAtOrError(postalCodePath, "Postal Code")
        val country = e
          .attrAt(countryPath)
          .toRight(
            ParserError.MissingField("Country", Some(countryPath.show))
          )

        val pointOfContact = e
          .textAtOrError(pointOfContactPath, "Point of Contact")

        val phone = e.textAtOrError(phonePath, "Phone Number")
        val emails = e
          .textAtOrError(emailPath, "Emails")

        ContractingAuthorityRaw(
          officialName,
          nationalID,
          adress,
          town,
          postalCode,
          country,
          pointOfContact,
          phone,
          emails
        )
    }
