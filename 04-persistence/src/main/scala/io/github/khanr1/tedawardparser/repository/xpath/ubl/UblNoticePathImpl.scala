package io.github.khanr1.tedawardparser.repository
package xpath.ubl

import io.github.khanr1.tedawardparser.repository.xml.{Ns, QName, XMLPath}
import io.github.khanr1.tedawardparser.repository.xpath.ubl.{
  UblContractingPartyPath,
  UblNoticePath,
  UblNoticeResultPath,
  UblOrganizationPath,
  UblProcurementLotPath,
  UblProcurementProjectPath,
  UblPublicationSectionPath
}

/** Concrete XPath constants for UBL ContractAwardNotice (eForms SDK format).
  *
  * Implements [[UblNoticePath]] with paths derived from the six UBL namespaces.
  * Because UBL has a single XML schema (unlike TED's R208/R209 variants) this
  * is the sole implementation. Pass [[namespaces]] to every `XMLPathUtils` call
  * so that namespace-URI matching resolves correctly.
  *
  * Internal design
  *   - [[eformsExt]] is a shared private prefix for paths inside the eForms
  *     extension block.
  *   - Private intermediate `val`s within each nested object avoid repeating
  *     long path segments.
  *   - "Container" paths (e.g. [[OrganizationPath.containerPath]]) navigate
  *     from the document root to a collection of same-named elements.
  *   - "Per-element" paths (e.g. [[OrganizationPath.namePath]]) start with the
  *     element's own name so that the first-segment self-match in `nodesAt`
  *     works when the path is evaluated on that individual element.
  */
object UblNoticePathImpl extends UblNoticePath:

  /** Namespace URI bindings for all six UBL prefix families used in eForms XML.
    */
  val namespaces: Ns = Ns(
    Map(
      "cbc" -> "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2",
      "cac" -> "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2",
      "efac" -> "http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1",
      "efbc" -> "http://data.europa.eu/p27/eforms-ubl-extension-basic-components/1",
      "efext" -> "http://data.europa.eu/p27/eforms-ubl-extensions/1",
      "ext" -> "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2"
    )
  )

  /** Path prefix for the eForms extension block present in every UBL notice. */
  private val eformsExt: XMLPath = XMLPath.parse(
    "ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension"
  )

  // ── Publication section ─────────────────────────────────────────────────────

  object PublicationSectionPath extends UblPublicationSectionPath:
    // NoticeHeader: direct children of the ContractAwardNotice root element
    val noticeTypeCodePath: XMLPath = XMLPath.parse("cbc:NoticeTypeCode")
    val noticeUUIDPath: XMLPath =
      XMLPath.parse("cbc:ID").whereAttr("schemeName", "notice-id")
    val contractFolderIDPath: XMLPath = XMLPath.parse("cbc:ContractFolderID")
    val issueDatePath: XMLPath = XMLPath.parse("cbc:IssueDate")
    val ublVersionIDPath: XMLPath = XMLPath.parse("cbc:UBLVersionID")
    val customizationIDPath: XMLPath = XMLPath.parse("cbc:CustomizationID")
    val versionIDPath: XMLPath = XMLPath.parse("cbc:VersionID")
    val regulatoryDomainPath: XMLPath = XMLPath.parse("cbc:RegulatoryDomain")
    val noticeLanguageCodePath: XMLPath =
      XMLPath.parse("cbc:NoticeLanguageCode")
    // PublicationReference: inside efext:EformsExtension
    private val pubRoot: XMLPath = eformsExt / QName("efac", "Publication")
    val noticePublicationIDPath: XMLPath =
      pubRoot / QName("efbc", "NoticePublicationID")
    val gazetteIDPath: XMLPath = pubRoot / QName("efbc", "GazetteID")
    val publicationDatePath: XMLPath =
      pubRoot / QName("efbc", "PublicationDate")
    val noticeSubTypeCodePath: XMLPath =
      eformsExt / QName("efac", "NoticeSubType") / QName("cbc", "SubTypeCode")

  // ── Contracting party ───────────────────────────────────────────────────────

  object ContractingPartyPath extends UblContractingPartyPath:
    val root: XMLPath = XMLPath.parse("cac:ContractingParty")
    val buyerLegalTypePath: XMLPath =
      root / QName("cac", "ContractingPartyType") / QName(
        "cbc",
        "PartyTypeCode"
      )
    val activityTypePath: XMLPath =
      root / QName("cac", "ContractingActivity") / QName(
        "cbc",
        "ActivityTypeCode"
      )
    val buyerOrgIDPath: XMLPath =
      root / QName("cac", "Party") / QName(
        "cac",
        "PartyIdentification"
      ) / QName("cbc", "ID")
    val buyerProfileURIPath: XMLPath = root / QName("cbc", "BuyerProfileURI")

  // ── Organization registry ───────────────────────────────────────────────────

  object OrganizationPath extends UblOrganizationPath:
    val containerPath: XMLPath =
      eformsExt / QName("efac", "Organizations") / QName("efac", "Organization")
    val root: XMLPath = XMLPath.parse("efac:Organization")

    private val company: XMLPath = root / QName("efac", "Company")
    private val address: XMLPath = company / QName("cac", "PostalAddress")
    private val contact: XMLPath = company / QName("cac", "Contact")

    val orgIDPath: XMLPath =
      company / QName("cac", "PartyIdentification") / QName("cbc", "ID")
    val namePath: XMLPath =
      company / QName("cac", "PartyName") / QName("cbc", "Name")
    val nationalIDPath: XMLPath =
      company / QName("cac", "PartyLegalEntity") / QName("cbc", "CompanyID")
    val streetNamePath: XMLPath = address / QName("cbc", "StreetName")
    val additionalStreetNamePath: XMLPath =
      address / QName("cbc", "AdditionalStreetName")
    val cityNamePath: XMLPath = address / QName("cbc", "CityName")
    val postalZonePath: XMLPath = address / QName("cbc", "PostalZone")
    val nutsCodePath: XMLPath = address / QName("cbc", "CountrySubentityCode")
    val countryCodePath: XMLPath =
      address / QName("cac", "Country") / QName("cbc", "IdentificationCode")
    val websiteURIPath: XMLPath = company / QName("cbc", "WebsiteURI")
    val contactNamePath: XMLPath = contact / QName("cbc", "Name")
    val phonePath: XMLPath = contact / QName("cbc", "Telephone")
    val emailPath: XMLPath = contact / QName("cbc", "ElectronicMail")

  // ── Top-level procurement project ───────────────────────────────────────────

  object ProcurementProjectPath extends UblProcurementProjectPath:
    val root: XMLPath = XMLPath.parse("cac:ProcurementProject")

    private val tenderTotal: XMLPath =
      root / QName("cac", "RequestedTenderTotal")
    private val location: XMLPath =
      root / QName("cac", "RealizedLocation") / QName("cac", "Address")

    val internalIDPath: XMLPath = root / QName("cbc", "ID")
    val namePath: XMLPath = root / QName("cbc", "Name")
    val descriptionPath: XMLPath = root / QName("cbc", "Description")
    val contractNaturePath: XMLPath = root / QName("cbc", "ProcurementTypeCode")
    val notePath: XMLPath = root / QName("cbc", "Note")
    val estimatedAmountPath: XMLPath =
      tenderTotal / QName("cbc", "EstimatedOverallContractAmount")
    val estimatedAmountCurrencyPath: XMLPath =
      estimatedAmountPath attr "currencyID"
    val mainCPVPath: XMLPath =
      root / QName("cac", "MainCommodityClassification") / QName(
        "cbc",
        "ItemClassificationCode"
      )
    val additionalCPVsPath: XMLPath =
      root / QName("cac", "AdditionalCommodityClassification") / QName(
        "cbc",
        "ItemClassificationCode"
      )
    val deliveryCountryPath: XMLPath =
      location / QName("cac", "Country") / QName("cbc", "IdentificationCode")
    val deliveryNutsCodePath: XMLPath =
      location / QName("cbc", "CountrySubentityCode")

  // ── Procurement lots ────────────────────────────────────────────────────────

  object ProcurementLotPath extends UblProcurementLotPath:
    val containerPath: XMLPath =
      XMLPath.parse("cac:ProcurementProjectLot").whereAttr("schemeName", "Lot")
    val root: XMLPath = XMLPath.parse("cac:ProcurementProjectLot")

    val lotIDPath: XMLPath = root / QName("cbc", "ID")

    private val tenderingTerms: XMLPath = root / QName("cac", "TenderingTerms")
    val fundingProgramCodePath: XMLPath =
      tenderingTerms / QName("cbc", "FundingProgramCode")

    val awardCriteriaContainerPath: XMLPath =
      tenderingTerms /
        QName("cac", "AwardingTerms") /
        QName("cac", "AwardingCriterion") /
        QName("cac", "SubordinateAwardingCriterion")

    // Criterion paths — relative to a single cac:SubordinateAwardingCriterion element
    private val criterionRoot: XMLPath =
      XMLPath.parse("cac:SubordinateAwardingCriterion")
    val criterionTypePath: XMLPath =
      criterionRoot / QName("cbc", "AwardingCriterionTypeCode")
    val criterionDescriptionPath: XMLPath =
      criterionRoot / QName("cbc", "Description")
    val criterionWeightPath: XMLPath =
      criterionRoot /
        QName("ext", "UBLExtensions") /
        QName("ext", "UBLExtension") /
        QName("ext", "ExtensionContent") /
        QName("efext", "EformsExtension") /
        QName("efac", "AwardCriterionParameter") /
        QName("efbc", "ParameterNumeric")

    // Appeal terms — from lot root
    private val appealTerms: XMLPath =
      tenderingTerms / QName("cac", "AppealTerms")
    val appealDeadlineDescPath: XMLPath =
      appealTerms / QName("cac", "PresentationPeriod") / QName(
        "cbc",
        "Description"
      )
    val appealBodyOrgIDPath: XMLPath =
      appealTerms / QName("cac", "AppealInformationParty") / QName(
        "cac",
        "PartyIdentification"
      ) / QName("cbc", "ID")
    val appealReceiverOrgIDPath: XMLPath =
      appealTerms / QName("cac", "AppealReceiverParty") / QName(
        "cac",
        "PartyIdentification"
      ) / QName("cbc", "ID")

    // Lot-level procurement project — all relative to lot element
    private val lotProject: XMLPath = root / QName("cac", "ProcurementProject")
    private val lotTenderTotal: XMLPath =
      lotProject / QName("cac", "RequestedTenderTotal")
    private val lotLocation: XMLPath =
      lotProject / QName("cac", "RealizedLocation") / QName("cac", "Address")

    val lotProjectInternalIDPath: XMLPath = lotProject / QName("cbc", "ID")
    val lotProjectNamePath: XMLPath = lotProject / QName("cbc", "Name")
    val lotProjectDescriptionPath: XMLPath =
      lotProject / QName("cbc", "Description")
    val lotProjectContractNaturePath: XMLPath =
      lotProject / QName("cbc", "ProcurementTypeCode")
    val lotProjectNotePath: XMLPath = lotProject / QName("cbc", "Note")
    val lotProjectEstimatedAmountPath: XMLPath =
      lotTenderTotal / QName("cbc", "EstimatedOverallContractAmount")
    val lotProjectEstimatedCurrencyPath: XMLPath =
      lotProjectEstimatedAmountPath attr "currencyID"
    val lotProjectMainCPVPath: XMLPath =
      lotProject / QName("cac", "MainCommodityClassification") / QName(
        "cbc",
        "ItemClassificationCode"
      )
    val lotProjectAdditionalCPVsPath: XMLPath =
      lotProject / QName("cac", "AdditionalCommodityClassification") / QName(
        "cbc",
        "ItemClassificationCode"
      )
    val lotProjectDeliveryCountryPath: XMLPath =
      lotLocation / QName("cac", "Country") / QName("cbc", "IdentificationCode")
    val lotProjectDeliveryNutsPath: XMLPath =
      lotLocation / QName("cbc", "CountrySubentityCode")
    val lotProjectDurationPath: XMLPath =
      lotProject / QName("cac", "PlannedPeriod") / QName(
        "cbc",
        "DurationMeasure"
      )

  // ── Notice result (award data) ───────────────────────────────────────────────

  object NoticeResultPath extends UblNoticeResultPath:
    val root: XMLPath = eformsExt / QName("efac", "NoticeResult")

    val totalAmountPath: XMLPath = root / QName("cbc", "TotalAmount")
    val totalAmountCurrencyPath: XMLPath = totalAmountPath attr "currencyID"

    // LotResult — container from root; per-element paths self-match on efac:LotResult
    val lotResultContainerPath: XMLPath = root / QName("efac", "LotResult")
    private val lr: XMLPath = XMLPath.parse("efac:LotResult")
    val lotResultIDPath: XMLPath = lr / QName("cbc", "ID")
    val tenderResultCodePath: XMLPath = lr / QName("cbc", "TenderResultCode")
    val lotResultLotIDPath: XMLPath =
      lr / QName("efac", "TenderLot") / QName("cbc", "ID")
    val lotResultContractIDPath: XMLPath =
      lr / QName("efac", "SettledContract") / QName("cbc", "ID")
    val lotResultTenderIDPath: XMLPath =
      lr / QName("efac", "LotTender") / QName("cbc", "ID")
    private val stats: XMLPath =
      lr / QName("efac", "ReceivedSubmissionsStatistics")
    val statisticsCodePath: XMLPath = stats / QName("efbc", "StatisticsCode")
    val statisticsCountPath: XMLPath =
      stats / QName("efbc", "StatisticsNumeric")

    // SettledContract — container from root; per-element paths self-match on efac:SettledContract
    val settledContractContainerPath: XMLPath =
      root / QName("efac", "SettledContract")
    private val sc: XMLPath = XMLPath.parse("efac:SettledContract")
    val contractIDPath: XMLPath = sc / QName("cbc", "ID")
    val awardDatePath: XMLPath = sc / QName("cbc", "AwardDate")
    val contractIssueDatePath: XMLPath = sc / QName("cbc", "IssueDate")
    val contractTitlePath: XMLPath = sc / QName("cbc", "Title")
    val contractInternalRefPath: XMLPath =
      sc / QName("efac", "ContractReference") / QName("cbc", "ID")
    val contractTenderIDPath: XMLPath =
      sc / QName("efac", "LotTender") / QName("cbc", "ID")

    // LotTender — container from root; per-element paths self-match on efac:LotTender
    val lotTenderContainerPath: XMLPath = root / QName("efac", "LotTender")
    private val lt: XMLPath = XMLPath.parse("efac:LotTender")
    val tenderIDPath: XMLPath = lt / QName("cbc", "ID")
    private val monetaryTotal: XMLPath = lt / QName("cac", "LegalMonetaryTotal")
    val tenderPayableAmountPath: XMLPath =
      monetaryTotal / QName("cbc", "PayableAmount")
    val tenderPayableCurrencyPath: XMLPath =
      tenderPayableAmountPath attr "currencyID"
    val tenderLotIDPath: XMLPath =
      lt / QName("efac", "TenderLot") / QName("cbc", "ID")
    val tenderPartyIDPath: XMLPath =
      lt / QName("efac", "TenderingParty") / QName("cbc", "ID")

    // TenderingParty — container from root; per-element paths self-match on efac:TenderingParty
    val tenderingPartyContainerPath: XMLPath =
      root / QName("efac", "TenderingParty")
    private val tp: XMLPath = XMLPath.parse("efac:TenderingParty")
    val tenderingPartyIDPath: XMLPath = tp / QName("cbc", "ID")
    val tenderingPartyOrgIDPath: XMLPath =
      tp / QName("efac", "Tenderer") / QName("cbc", "ID")
