package io.github.khanr1.tedawardparser.repository.parsers.tedexport.r208.f15

import cats.syntax.all.*
import io.github.khanr1.tedawardparser.common.*

import io.github.khanr1.tedawardparser.repository.parsers.*

import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.*

import io.github.khanr1.tedawardparser.tedexport.r208.f03.PartialContractingAuthority
import scala.xml.Elem

object PartialContractingAuthorityDecoder {

  val path =
    PartialContractingAuthorityInformationDecoder.root / "NAME_ADDRESSES_CONTACT_VEAT" / "CA_CE_CONCESSIONAIRE_PROFILE"
  val officialNamePath = path / "ORGANISATION" / "OFFICIALNAME"
  val nationalIDPath = path / "ORGANISATION" / "NATIONALID"
  val addressPath = path / "ADDRESS"
  val townPath = path / "TOWN"
  val postalCodePath = path / "POSTAL_CODE"
  val countryPath = path / "COUNTRY" attr ("VALUE")
  val pointOfContactPath = path / "CONTACT_POINT"
  val phonePath = path / "PHONE"
  val emailPath = path / "E_MAILS" / "E_MAIL"
  given decoder: XMLDecoder[PartialContractingAuthority[ParserError]] =
    new XMLDecoder[PartialContractingAuthority[ParserError]] {

      override def decode(
          e: Elem
      ): Either[ParserError, PartialContractingAuthority[
        ParserError
      ]] = {
        val officialName =
          e.textAtOrError(officialNamePath, "Official Name").map(Name(_))
        val nationalID =
          e.textAtOrError(nationalIDPath, "National ID").map(NationalID(_))
        val adress = e.textAtOrError(addressPath, "Address").map(Address(_))
        val town = e.textAtOrError(townPath, "Town").map(Town(_))
        val postalCode =
          e.textAtOrError(postalCodePath, "Postal Code").map(PostalCode(_))
        val country = e
          .attrAt(countryPath)
          .toRight(ParserError.MissingField("Country", Some(countryPath.show)))
          .map(Country.toDomain(_))
        val pointOfContact = e
          .textAtOrError(pointOfContactPath, "Point of Contact")
          .map(PointOfContact(_))
        val phone = e.textAtOrError(phonePath, "Phone Number").map(Phone(_))
        val emails = e
          .textAtOrError(emailPath, "Emails")
          .map(s => s.split(";").map(Email(_)).toList)

        Right(
          PartialContractingAuthority(
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
        )
      }

    }
}
