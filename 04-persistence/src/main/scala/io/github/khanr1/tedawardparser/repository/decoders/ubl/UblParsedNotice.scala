package io.github.khanr1.tedawardparser
package repository
package decoders
package ubl

import io.github.khanr1.tedawardparser.repository.xml.Raw

// ── Per-organization raw data ─────────────────────────────────────────────────

final case class UblOrganizationRaw(
    orgID: String,
    name: String,
    nationalID: String,
    streetName: Option[String],
    additionalStreetName: Option[String],
    cityName: String,
    postalZone: String,
    nutsCode: Option[String],
    countryCode: String,
    websiteURI: Option[String],
    contactName: Option[String],
    phone: Option[String],
    email: Option[String]
) extends Raw

// ── Per-criterion raw data ────────────────────────────────────────────────────

final case class UblAwardCriterionRaw(
    criterionType: Option[String],
    description: Option[String],
    weightPercent: Option[Int]
) extends Raw

// ── Per-lot raw data ──────────────────────────────────────────────────────────

final case class UblLotRaw(
    lotID: String,
    lotProjectName: String,
    lotProjectDescription: String,
    lotProjectContractNature: Option[String],
    lotProjectEstimatedAmount: Option[String],
    lotProjectEstimatedCurrency: Option[String],
    lotProjectMainCPV: Option[String],
    lotProjectAdditionalCPVs: List[String],
    lotProjectDeliveryCountry: Option[String],
    lotProjectDeliveryNuts: Option[String],
    lotProjectDuration: Option[Int],
    lotProjectNote: Option[String],
    fundingProgramCode: Option[String],
    awardCriteria: List[UblAwardCriterionRaw],
    appealDeadlineDesc: Option[String],
    appealBodyOrgID: Option[String],
    appealReceiverOrgID: Option[String]
) extends Raw

// ── Award result raw data ─────────────────────────────────────────────────────

final case class UblLotResultRaw(
    resultID: String,
    tenderResultCode: Option[String],
    lotID: String,
    contractID: String,
    tenderID: String,
    tendersReceivedCount: Option[Int]
) extends Raw

final case class UblSettledContractRaw(
    contractID: String,
    awardDate: Option[String],
    issueDate: Option[String],
    contractTitle: Option[String],
    contractInternalRef: Option[String],
    tenderID: Option[String]
) extends Raw

final case class UblLotTenderRaw(
    tenderID: String,
    payableAmount: Option[String],
    payableCurrency: Option[String],
    tenderingPartyID: Option[String]
) extends Raw

final case class UblTenderingPartyRaw(
    tenderingPartyID: String,
    contractorOrgID: String
) extends Raw

// ── Aggregated parsed notice ──────────────────────────────────────────────────

final case class UblParsedNotice(
    // Publication / header
    noticeTypeCode: String,
    noticeUUID: String,
    contractFolderID: String,
    issueDate: String,
    ublVersionID: String,
    customizationID: String,
    versionID: String,
    regulatoryDomain: String,
    noticeLanguageCode: String,
    noticePublicationID: String,
    gazetteID: String,
    publicationDate: String,
    noticeSubTypeCode: String,
    // Contracting party (buyer) reference
    buyerOrgID: String,
    buyerLegalType: Option[String],
    activityType: Option[String],
    buyerProfileURI: Option[String],
    // All organizations (registry)
    organizations: List[UblOrganizationRaw],
    // Top-level procurement project
    projectName: String,
    projectDescription: String,
    projectContractNature: Option[String],
    projectEstimatedAmount: Option[String],
    projectEstimatedCurrency: Option[String],
    projectMainCPV: Option[String],
    projectAdditionalCPVs: List[String],
    projectDeliveryCountry: Option[String],
    projectDeliveryNuts: Option[String],
    projectNote: Option[String],
    // Lots
    lots: List[UblLotRaw],
    // Award results
    totalAmount: Option[String],
    totalAmountCurrency: Option[String],
    lotResults: List[UblLotResultRaw],
    settledContracts: List[UblSettledContractRaw],
    lotTenders: List[UblLotTenderRaw],
    tenderingParties: List[UblTenderingPartyRaw]
)
