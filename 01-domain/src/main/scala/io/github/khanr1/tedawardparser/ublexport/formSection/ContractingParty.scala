package io.github.khanr1.tedawardparser
package ublExport
package formSection

import io.github.khanr1.tedawardparser.tedExport.types.URL
import io.github.khanr1.tedawardparser.ublExport.types.{ActivityTypeCode, BuyerLegalType}

/** The contracting authority (buyer), with organization data resolved from the ORG-ID reference.
  * Equivalent to ContractingAuthority in the TED export domain.
  */
final case class ContractingParty(
    organization: Organization,        // resolved from cac:Party/cac:PartyIdentification/cbc:ID
    buyerLegalType: BuyerLegalType,    // cac:ContractingPartyType/cbc:PartyTypeCode e.g. "body-pl"
    activityType: ActivityTypeCode,    // cac:ContractingActivity/cbc:ActivityTypeCode e.g. "education"
    buyerProfileURI: Option[URL]       // cbc:BuyerProfileURI
)
