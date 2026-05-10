package io.github.khanr1.tedawardparser.repository.xml
package decoders
package formSection

import scala.xml.Elem
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPath
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR209

final case class ContractingAuthorityRaw(
    name: Either[ParserError, String],
    nationalID: Either[ParserError, String],
    address: Either[ParserError, String],
    town: Either[ParserError, String],
    postalCode: Either[ParserError, String],
    country: Either[ParserError, String],
    pointOfContact: Either[ParserError, String],
    phone: Either[ParserError, String],
    email: Either[ParserError, String]
) extends Raw

object ContractingAuthorityDecoder208:
  given XMLDecoder[ContractingAuthorityRaw] =
    new XMLDecoder[ContractingAuthorityRaw] {

      override def decode(e: Elem): ContractingAuthorityRaw =
        val path = e
          .nodesAt(FormSectionPathR208.F03.root)
          .headOption
          .map(x => x.label) match
          case Some("FD_CONTRACT_AWARD") =>
            io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F03.ContractingAuthorityPath
          case _ =>
            io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208.F15.ContractingAuthorityPath

        import path.*

        ContractingAuthorityRaw(
          name           = e.textAtOrError(officialNamePath, "Official Name"),
          nationalID     = e.textAtOrError(nationalIDPath, "National ID"),
          address        = e.textAtOrError(addressPath, "Address"),
          town           = e.textAtOrError(townPath, "Town"),
          postalCode     = e.textAtOrError(postalCodePath, "Postal Code"),
          country        = e.attrAt(countryPath)
                             .toRight(ParserError.MissingField("Country", Some(countryPath.show))),
          pointOfContact = e.textAtOrError(pointOfContactPath, "Point of Contact"),
          phone          = e.textAtOrError(phonePath, "Phone Number"),
          email          = e.textAtOrError(emailPath, "Emails")
        )
    }

object ContractingAuthorityDecoder209:
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

        ContractingAuthorityRaw(
          name           = e.textAtOrError(officialNamePath, "Official Name"),
          nationalID     = e.textAtOrError(nationalIDPath, "National ID"),
          address        = e.textAtOrError(addressPath, "Address"),
          town           = e.textAtOrError(townPath, "Town"),
          postalCode     = e.textAtOrError(postalCodePath, "Postal Code"),
          country        = e.attrAt(countryPath)
                             .toRight(ParserError.MissingField("Country", Some(countryPath.show))),
          pointOfContact = e.textAtOrError(pointOfContactPath, "Point of Contact"),
          phone          = e.textAtOrError(phonePath, "Phone Number"),
          email          = e.textAtOrError(emailPath, "Emails")
        )
    }

object ContractingAuthorityDecoderR208F02:
  given XMLDecoder[ContractingAuthorityRaw] =
    new XMLDecoder[ContractingAuthorityRaw]:
      override def decode(e: Elem): ContractingAuthorityRaw =
        val path = FormSectionPathR208.F02.ContractingAuthorityPath
        ContractingAuthorityRaw(
          name           = e.textAtOrError(path.officialNamePath, "Official Name"),
          nationalID     = e.textAtOrError(path.nationalIDPath, "National ID"),
          address        = e.textAtOrError(path.addressPath, "Address"),
          town           = e.textAtOrError(path.townPath, "Town"),
          postalCode     = e.textAtOrError(path.postalCodePath, "Postal Code"),
          country        = e.attrAt(path.countryPath)
                             .toRight(ParserError.MissingField("Country", Some(path.countryPath.show))),
          pointOfContact = e.textAtOrError(path.pointOfContactPath, "Point of Contact"),
          phone          = e.textAtOrError(path.phonePath, "Phone Number"),
          email          = e.textAtOrError(path.emailPath, "Emails")
        )

object ContractingAuthorityDecoderR209F02:
  given XMLDecoder[ContractingAuthorityRaw] =
    new XMLDecoder[ContractingAuthorityRaw]:
      override def decode(e: Elem): ContractingAuthorityRaw =
        val path = FormSectionPathR209.F02.ContractingAuthorityPath
        ContractingAuthorityRaw(
          name           = e.textAtOrError(path.officialNamePath, "Official Name"),
          nationalID     = e.textAtOrError(path.nationalIDPath, "National ID"),
          address        = e.textAtOrError(path.addressPath, "Address"),
          town           = e.textAtOrError(path.townPath, "Town"),
          postalCode     = e.textAtOrError(path.postalCodePath, "Postal Code"),
          country        = e.attrAt(path.countryPath)
                             .toRight(ParserError.MissingField("Country", Some(path.countryPath.show))),
          pointOfContact = e.textAtOrError(path.pointOfContactPath, "Point of Contact"),
          phone          = e.textAtOrError(path.phonePath, "Phone Number"),
          email          = e.textAtOrError(path.emailPath, "Emails")
        )
