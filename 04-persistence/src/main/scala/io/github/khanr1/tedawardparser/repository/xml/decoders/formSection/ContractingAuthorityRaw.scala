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
    NationalID: Either[ParserError, String],
    adress: Either[ParserError, String],
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

object ContractingAuthorityDecoderR208F02:
  given XMLDecoder[ContractingAuthorityRaw] =
    new XMLDecoder[ContractingAuthorityRaw]:
      override def decode(e: Elem): ContractingAuthorityRaw =
        val path = FormSectionPathR208.F02.ContractingAuthorityPath
        ContractingAuthorityRaw(
          e.textAtOrError(path.officialNamePath, "Official Name"),
          e.textAtOrError(path.nationalIDPath, "National ID"),
          e.textAtOrError(path.addressPath, "Address"),
          e.textAtOrError(path.townPath, "Town"),
          e.textAtOrError(path.postalCodePath, "Postal Code"),
          e.attrAt(path.countryPath).toRight(ParserError.MissingField("Country", Some(path.countryPath.show))),
          e.textAtOrError(path.pointOfContactPath, "Point of Contact"),
          e.textAtOrError(path.phonePath, "Phone Number"),
          e.textAtOrError(path.emailPath, "Emails")
        )

object ContractingAuthorityDecoderR209F02:
  given XMLDecoder[ContractingAuthorityRaw] =
    new XMLDecoder[ContractingAuthorityRaw]:
      override def decode(e: Elem): ContractingAuthorityRaw =
        val path = FormSectionPathR209.F02.ContractingAuthorityPath
        ContractingAuthorityRaw(
          e.textAtOrError(path.officialNamePath, "Official Name"),
          e.textAtOrError(path.nationalIDPath, "National ID"),
          e.textAtOrError(path.addressPath, "Address"),
          e.textAtOrError(path.townPath, "Town"),
          e.textAtOrError(path.postalCodePath, "Postal Code"),
          e.attrAt(path.countryPath).toRight(ParserError.MissingField("Country", Some(path.countryPath.show))),
          e.textAtOrError(path.pointOfContactPath, "Point of Contact"),
          e.textAtOrError(path.phonePath, "Phone Number"),
          e.textAtOrError(path.emailPath, "Emails")
        )
