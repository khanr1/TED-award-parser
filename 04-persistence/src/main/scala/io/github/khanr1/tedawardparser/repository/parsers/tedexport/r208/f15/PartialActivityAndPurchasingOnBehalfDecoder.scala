package io.github.khanr1.tedawardparser
package repository
package parsers
package tedexport
package r208
package f15

import io.github.khanr1.tedawardparser.common.*
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.*
import io.github.khanr1.tedawardparser.tedexport.r208.f03.PartialActivityAndPurchasingOnBehalf
import io.github.khanr1.tedawardparser.tedexport.r208.f03.PartialContractingAuthority
import scala.xml.Elem

import cats.syntax.all.*

object PartialActivityAndPurchasingOnBehalfDecoder {

  val path =
    PartialContractingAuthorityInformationDecoder.root / "TYPE_AND_ACTIVITIES_OR_CONTRACTING_ENTITY_AND_PURCHASING_ON_BEHALF"
  val contractingAuthorityTypePath =
    path / "TYPE_AND_ACTIVITIES" / "TYPE_OF_CONTRACTING_AUTHORITY" attr ("VALUE")
  val ContractingAuthorityActivityPath =
    path / "TYPE_AND_ACTIVITIES" / "TYPE_OF_ACTIVITY" attr ("VALUE")
  val purhasingOnBehalfPath = path / "PURCHASING_ON_BEHALF"

  given decoder: XMLDecoder[PartialActivityAndPurchasingOnBehalf[ParserError]] =
    new XMLDecoder[PartialActivityAndPurchasingOnBehalf[ParserError]] {

      override def decode(
          e: Elem
      ): Either[ParserError, PartialActivityAndPurchasingOnBehalf[
        ParserError
      ]] = {
        val purchasingOnBehalf
            : Option[PartialContractingAuthority[ParserError]] =
          if (
            e
              .childrenAt(purhasingOnBehalfPath)
              .map(_.label)
              .headOption == Some("PURCHASING_ON_BEHALF_YES")
          )
          then {
            val pathToInfo =
              purhasingOnBehalfPath / "PURCHASING_ON_BEHALF_YES" / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY"
            val officialNamePath = pathToInfo / "ORGANISATION" / "OFFICIALNAME"
            val nationalIDPath = pathToInfo / "ORGANISATION" / "NATIONALID"
            val addressPath = pathToInfo / "ADDRESS"
            val townPath = pathToInfo / "TOWN"
            val postalCodePath = pathToInfo / "POSTAL_CODE"
            val countryPath = pathToInfo / "COUNTRY" attr ("VALUE")
            val pointOfContactPath = pathToInfo / "CONTACT_POINT"
            val phonePath = pathToInfo / "PHONE"
            val emailPath = pathToInfo / "E_MAILS" / "E_MAIL"
            Some(
              PartialContractingAuthority[ParserError](
                name = e.textAtOrError(officialNamePath, "Name").map(Name(_)),
                NationalID = e
                  .textAtOrError(nationalIDPath, "National ID")
                  .map(NationalID(_)),
                adress =
                  e.textAtOrError(addressPath, "Address").map(Address(_)),
                town = e.textAtOrError(townPath, "Town").map(Town(_)),
                postalCode = e
                  .textAtOrError(postalCodePath, "Postal Code")
                  .map(PostalCode(_)),
                country = e
                  .attrAt(countryPath)
                  .toRight(ParserError.MissingField("Country"))
                  .map(Country.toDomain(_)),
                pointOfContact = e
                  .textAtOrError(pointOfContactPath, "Point Of Contact")
                  .map(PointOfContact(_)),
                phone = e.textAtOrError(phonePath, "Phone").map(Phone(_)),
                email = e
                  .textAtOrError(emailPath, "Emails")
                  .map(s => s.split(";").map(Email(_)).toList)
              )
            )
          } else None

        val contractingAuthorityType =
          e.attrAt(contractingAuthorityTypePath)
            .map(ContractingAuthorityType(_))
            .toRight(
              ParserError.MissingField(
                "Contracting Authority Type",
                Some(contractingAuthorityTypePath.show)
              )
            )
        val contractingAuthorityActivity
            : Either[ParserError, ContractingAuthorityActivity] =
          e.attrAt(ContractingAuthorityActivityPath)
            .map(x => ContractingAuthorityActivity(x))
            .toRight(
              ParserError.MissingField(
                "Contracting Authority Type",
                Some(contractingAuthorityTypePath.show)
              )
            )

        Right(
          PartialActivityAndPurchasingOnBehalf[ParserError](
            contractingAuthorityType,
            contractingAuthorityActivity,
            purchasingOnBehalf
          )
        )
      }
    }

}
