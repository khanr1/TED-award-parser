package io.github.khanr1.tedawardparser.repository.parsers.tedexport.r208.f03

import io.github.khanr1.tedawardparser.repository.parsers.XMLDecoder
import io.github.khanr1.tedawardparser.repository.parsers.ParserError
import io.github.khanr1.tedawardparser.tedexport.r208.f03.PartialContractingAuthority
import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.parsers.XMLPath
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.textAtOrError
import io.github.khanr1.tedawardparser.common.Name
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.textAt
import io.github.khanr1.tedawardparser.common.NationalID
import io.github.khanr1.tedawardparser.common.Address
import io.github.khanr1.tedawardparser.common.Town
import io.github.khanr1.tedawardparser.common.PostalCode
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.attrAt
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.common.Country
import io.github.khanr1.tedawardparser.common.PointOfContact
import io.github.khanr1.tedawardparser.common.Phone
import io.github.khanr1.tedawardparser.common.Email

object PartialContractingAuthorityDecoder {

  val path =
    PartialContractingAuthorityInformationDecoder.root / "NAME_ADDRESSES_CONTACT_CONTRACT_AWARD" / "CA_CE_CONCESSIONAIRE_PROFILE"
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
        val partialActivityAndPurchasingOnBehalfDecoder =
          PartialActivityAndPurchasingOnBehalfDecoder.decoder

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
