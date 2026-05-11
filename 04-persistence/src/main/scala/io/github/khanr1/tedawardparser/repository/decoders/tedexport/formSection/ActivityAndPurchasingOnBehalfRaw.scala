package io.github.khanr1.tedawardparser.repository.decoders
package tedexport.formSection

import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.xml.{ParserError, Raw, XMLDecoder}
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*
import io.github.khanr1.tedawardparser.repository.xpath.tedexport.FormSectionPathR208.F03
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.xpath.tedexport.FormSectionPathR209
import io.github.khanr1.tedawardparser.repository.xpath.tedexport.FormSectionPathR208

final case class ActivityAndPurchasingOnBehalfRaw(
    contractingAuthorityType: Either[ParserError, String],
    contractingAuthorityActivity: Either[ParserError, String],
    purchasingOnBehalf: Option[ContractingAuthorityRaw]
) extends Raw

object ActivityAndPurchasingOnBehalfR208Decoder {
  given XMLDecoder[ActivityAndPurchasingOnBehalfRaw] =
    new XMLDecoder[ActivityAndPurchasingOnBehalfRaw] {

      override def decode(e: Elem): ActivityAndPurchasingOnBehalfRaw = {
        val path = e
          .nodesAt(FormSectionPathR208.F03.root)
          .headOption
          .map(x => x.label) match
          case Some("FD_CONTRACT_AWARD") =>
            FormSectionPathR208.F03.ContractingAuthorityPath.ActivityAndPurchasingOnBehalfPath
          case _ =>
            FormSectionPathR208.F15.ContractingAuthorityPath.ActivityAndPurchasingOnBehalfPath

        import path.*

        val contractingAuthorityType =
          e.attrAt(path.contractingAuthorityTypePath)
            .orElse(e.attrAt(path.contractingAuthorityTypePath2))
            .toRight(
              ParserError.MissingField(
                "Contracting Authority Type",
                Some(path.contractingAuthorityTypePath.show)
              )
            )
        val contractingAuthorityActivity =
          e.attrAt(path.contractingAuthorityActivityPath)
            .orElse(e.attrAt(path.contractingAuthorityActivityPath2))
            .toRight(
              ParserError.MissingField(
                "Contracting Authority Activity",
                Some(contractingAuthorityTypePath.show)
              )
            )

        val purchasingOnBehalf =
          if (
            e
              .nodesAt(pathToInfo)
              .map(_.label)
              .headOption == Some("PURCHASING_ON_BEHALF_YES")
          )
          then
            Some(
              ContractingAuthorityRaw(
                name = e.textAtOrError(path.officialNamePath, "Name"),
                nationalID =
                  e.textAtOrError(path.nationalIDPath, "National ID"),
                address = e.textAtOrError(path.addressPath, "Address"),
                town = e.textAtOrError(path.townPath, "Town"),
                postalCode =
                  e.textAtOrError(path.postalCodePath, "Postal Code"),
                country = e
                  .attrAt(path.countryPath)
                  .toRight(ParserError.MissingField("Country")),
                pointOfContact =
                  e.textAtOrError(path.pointOfContactPath, "Point Of Contact"),
                phone = e.textAtOrError(path.phonePath, "Phone"),
                email = e.textAtOrError(path.emailPath, "Emails")
              )
            )
          else None

        ActivityAndPurchasingOnBehalfRaw(
          contractingAuthorityType,
          contractingAuthorityActivity,
          purchasingOnBehalf
        )

      }

    }

}

object ActivityAndPurchasingOnBehalfR209Decoder {
  given XMLDecoder[ActivityAndPurchasingOnBehalfRaw] =
    new XMLDecoder[ActivityAndPurchasingOnBehalfRaw] {

      override def decode(e: Elem): ActivityAndPurchasingOnBehalfRaw = {
        val path = e
          .nodesAt(FormSectionPathR209.F03.root)
          .headOption
          .map(x => x.label) match
          case Some("F03_2014") =>
            FormSectionPathR209.F03.ContractingAuthorityPath.ActivityAndPurchasingOnBehalfPath
          case _ =>
            FormSectionPathR209.F15.ContractingAuthorityPath.ActivityAndPurchasingOnBehalfPath

        import path.*

        val contractingAuthorityType =
          e.attrAt(path.contractingAuthorityTypePath)
            .orElse(e.attrAt(path.contractingAuthorityTypePath2))
            .toRight(
              ParserError.MissingField(
                "Contracting Authority Type",
                Some(path.contractingAuthorityTypePath.show)
              )
            )
        val contractingAuthorityActivity =
          e.attrAt(path.contractingAuthorityActivityPath)
            .orElse(e.attrAt(path.contractingAuthorityActivityPath2))
            .toRight(
              ParserError.MissingField(
                "Contracting Authority Activity",
                Some(contractingAuthorityTypePath.show)
              )
            )

        val purchasingOnBehalf =
          if (
            e
              .nodesAt(pathToInfo)
              .map(_.label)
              .headOption == Some("PURCHASING_ON_BEHALF_YES")
          )
          then
            Some(
              ContractingAuthorityRaw(
                name = e.textAtOrError(path.officialNamePath, "Name"),
                nationalID =
                  e.textAtOrError(path.nationalIDPath, "National ID"),
                address = e.textAtOrError(path.addressPath, "Address"),
                town = e.textAtOrError(path.townPath, "Town"),
                postalCode =
                  e.textAtOrError(path.postalCodePath, "Postal Code"),
                country = e
                  .attrAt(path.countryPath)
                  .toRight(ParserError.MissingField("Country")),
                pointOfContact =
                  e.textAtOrError(path.pointOfContactPath, "Point Of Contact"),
                phone = e.textAtOrError(path.phonePath, "Phone"),
                email = e.textAtOrError(path.emailPath, "Emails")
              )
            )
          else None

        ActivityAndPurchasingOnBehalfRaw(
          contractingAuthorityType,
          contractingAuthorityActivity,
          purchasingOnBehalf
        )

      }

    }

}
