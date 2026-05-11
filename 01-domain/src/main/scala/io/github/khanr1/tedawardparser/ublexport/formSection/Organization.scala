package io.github.khanr1.tedawardparser
package ublExport
package formSection

import io.github.khanr1.tedawardparser.common.Country
import io.github.khanr1.tedawardparser.tedExport.types.{
  Address, Email, Name, NationalID, Phone, PointOfContact, PostalCode, Town, URL
}
import io.github.khanr1.tedawardparser.ublExport.types.NutsCode

/** A fully resolved organization from efac:Organizations.
  * Used as both the winning contractor and the contracting authority.
  * Equivalent to Contractor in the TED export domain.
  */
final case class Organization(
    name: Name,                            // cac:PartyName/cbc:Name
    nationalID: NationalID,                // cac:PartyLegalEntity/cbc:CompanyID (SIRET/VAT)
    streetName: Option[Address],           // cbc:StreetName
    additionalStreetName: Option[Address], // cbc:AdditionalStreetName
    cityName: Town,                        // cbc:CityName
    postalZone: PostalCode,                // cbc:PostalZone
    nutsCode: Option[NutsCode],            // cbc:CountrySubentityCode listName="nuts"
    country: Country,                      // cac:Country/cbc:IdentificationCode (3-letter ISO)
    websiteURI: Option[URL],               // cbc:WebsiteURI
    contactName: Option[PointOfContact],   // cac:Contact/cbc:Name
    phone: Option[Phone],                  // cbc:Telephone
    email: Option[Email]                   // cbc:ElectronicMail
)
