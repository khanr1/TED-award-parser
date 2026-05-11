package io.github.khanr1.tedawardparser
package repository
package xpath.ubl

import io.github.khanr1.tedawardparser.repository.xml.{Ns, XMLPath}

/** Master trait for all UBL ContractAwardNotice path containers.
  * Symmetric to [[FormSectionPath]] in the TED export layer, but covering the
  * full notice structure (publication + form sections) in a single hierarchy.
  * There is only one UBL format, so only one implementation is expected.
  */
trait UblNoticePath:
  val namespaces: Ns
  def PublicationSectionPath: UblPublicationSectionPath
  def ContractingPartyPath: UblContractingPartyPath
  def OrganizationPath: UblOrganizationPath
  def ProcurementProjectPath: UblProcurementProjectPath
  def ProcurementLotPath: UblProcurementLotPath
  def NoticeResultPath: UblNoticeResultPath

/** Paths for the publication metadata sections of a UBL notice.
  * Covers both the root-level `cbc:*` header fields (NoticeHeader) and the
  * `efac:Publication` block inside the eForms extension (PublicationReference).
  * Equivalent to [[CodedDataSectionPath]] in the TED export layer.
  */
trait UblPublicationSectionPath:
  // NoticeHeader — direct children of the ContractAwardNotice root
  val noticeTypeCodePath: XMLPath      // cbc:NoticeTypeCode
  val noticeUUIDPath: XMLPath          // cbc:ID [schemeName="notice-id"]
  val contractFolderIDPath: XMLPath    // cbc:ContractFolderID
  val issueDatePath: XMLPath           // cbc:IssueDate
  val ublVersionIDPath: XMLPath        // cbc:UBLVersionID
  val customizationIDPath: XMLPath     // cbc:CustomizationID
  val versionIDPath: XMLPath           // cbc:VersionID
  val regulatoryDomainPath: XMLPath    // cbc:RegulatoryDomain
  val noticeLanguageCodePath: XMLPath  // cbc:NoticeLanguageCode
  // PublicationReference — inside efext:EformsExtension/efac:Publication
  val noticePublicationIDPath: XMLPath // efbc:NoticePublicationID
  val gazetteIDPath: XMLPath           // efbc:GazetteID
  val publicationDatePath: XMLPath     // efbc:PublicationDate
  val noticeSubTypeCodePath: XMLPath   // efac:NoticeSubType/cbc:SubTypeCode

/** Paths for the `cac:ContractingParty` block (buyer identification).
  * The party's full organization data is resolved via [[UblOrganizationPath]]
  * using the ORG-ID returned by [[buyerOrgIDPath]].
  */
trait UblContractingPartyPath:
  val root: XMLPath                // cac:ContractingParty
  val buyerLegalTypePath: XMLPath  // root/cac:ContractingPartyType/cbc:PartyTypeCode
  val activityTypePath: XMLPath    // root/cac:ContractingActivity/cbc:ActivityTypeCode
  val buyerOrgIDPath: XMLPath      // root/cac:Party/cac:PartyIdentification/cbc:ID
  val buyerProfileURIPath: XMLPath // root/cbc:BuyerProfileURI

/** Paths for resolving organizations from `efac:Organizations`.
  *
  * Two usage contexts:
  *   - `containerPath`: navigated from the document root to obtain the full
  *     list of `efac:Organization` elements.
  *   - All other paths: navigated from a single `efac:Organization` element
  *     (first-segment self-match), returning data for that one organization.
  */
trait UblOrganizationPath:
  val containerPath: XMLPath            // eformsExt/efac:Organizations/efac:Organization (doc root)
  val root: XMLPath                     // efac:Organization (self-match root for per-element nav)
  val orgIDPath: XMLPath                // root/efac:Company/cac:PartyIdentification/cbc:ID
  val namePath: XMLPath                 // root/efac:Company/cac:PartyName/cbc:Name
  val nationalIDPath: XMLPath           // root/efac:Company/cac:PartyLegalEntity/cbc:CompanyID
  val streetNamePath: XMLPath           // root/efac:Company/cac:PostalAddress/cbc:StreetName
  val additionalStreetNamePath: XMLPath // root/efac:Company/cac:PostalAddress/cbc:AdditionalStreetName
  val cityNamePath: XMLPath             // root/efac:Company/cac:PostalAddress/cbc:CityName
  val postalZonePath: XMLPath           // root/efac:Company/cac:PostalAddress/cbc:PostalZone
  val nutsCodePath: XMLPath             // root/efac:Company/cac:PostalAddress/cbc:CountrySubentityCode
  val countryCodePath: XMLPath          // root/efac:Company/cac:PostalAddress/cac:Country/cbc:IdentificationCode
  val websiteURIPath: XMLPath           // root/efac:Company/cbc:WebsiteURI
  val contactNamePath: XMLPath          // root/efac:Company/cac:Contact/cbc:Name
  val phonePath: XMLPath                // root/efac:Company/cac:Contact/cbc:Telephone
  val emailPath: XMLPath                // root/efac:Company/cac:Contact/cbc:ElectronicMail

/** Paths for the top-level `cac:ProcurementProject` block.
  * Equivalent to [[ContractAwardObjectInformationPath]] in the TED export layer
  * but substantially richer (CPV codes, delivery location, notes).
  */
trait UblProcurementProjectPath:
  val root: XMLPath                       // cac:ProcurementProject
  val internalIDPath: XMLPath             // root/cbc:ID
  val namePath: XMLPath                   // root/cbc:Name
  val descriptionPath: XMLPath            // root/cbc:Description
  val contractNaturePath: XMLPath         // root/cbc:ProcurementTypeCode
  val notePath: XMLPath                   // root/cbc:Note
  val estimatedAmountPath: XMLPath        // root/cac:RequestedTenderTotal/cbc:EstimatedOverallContractAmount
  val estimatedAmountCurrencyPath: XMLPath // estimatedAmountPath/@currencyID
  val mainCPVPath: XMLPath                // root/cac:MainCommodityClassification/cbc:ItemClassificationCode
  val additionalCPVsPath: XMLPath         // root/cac:AdditionalCommodityClassification/cbc:ItemClassificationCode
  val deliveryCountryPath: XMLPath        // root/cac:RealizedLocation/cac:Address/cac:Country/cbc:IdentificationCode
  val deliveryNutsCodePath: XMLPath       // root/cac:RealizedLocation/cac:Address/cbc:CountrySubentityCode

/** Paths for `cac:ProcurementProjectLot` elements.
  *
  * Two usage contexts:
  *   - `containerPath`: navigated from the document root; filtered to
  *     schemeName="Lot" (excludes LotsGroup elements).
  *   - `root` and all others: navigated from a single lot element (self-match).
  *
  * Includes:
  *   - Lot identification and funding
  *   - Award criteria (relative to `cac:SubordinateAwardingCriterion` elements)
  *   - Appeal terms
  *   - Lot-level `cac:ProcurementProject` (prefixed `lotProject*`)
  */
trait UblProcurementLotPath:
  val containerPath: XMLPath              // cac:ProcurementProjectLot [schemeName="Lot"] (doc root nav)
  val root: XMLPath                       // cac:ProcurementProjectLot (self-match root)
  val lotIDPath: XMLPath                  // root/cbc:ID
  val fundingProgramCodePath: XMLPath     // root/cac:TenderingTerms/cbc:FundingProgramCode
  // Award criteria — container path from lot root; criterion paths relative to SubordinateAwardingCriterion
  val awardCriteriaContainerPath: XMLPath  // root/…/cac:SubordinateAwardingCriterion
  val criterionTypePath: XMLPath           // cac:SubordinateAwardingCriterion/cbc:AwardingCriterionTypeCode
  val criterionDescriptionPath: XMLPath    // cac:SubordinateAwardingCriterion/cbc:Description
  val criterionWeightPath: XMLPath         // cac:SubordinateAwardingCriterion/ext:UBLExtensions/…/efbc:ParameterNumeric
  // Appeal terms — from lot root
  val appealDeadlineDescPath: XMLPath      // root/cac:TenderingTerms/cac:AppealTerms/cac:PresentationPeriod/cbc:Description
  val appealBodyOrgIDPath: XMLPath         // root/cac:TenderingTerms/cac:AppealTerms/cac:AppealInformationParty/cac:PartyIdentification/cbc:ID
  val appealReceiverOrgIDPath: XMLPath     // root/cac:TenderingTerms/cac:AppealTerms/cac:AppealReceiverParty/cac:PartyIdentification/cbc:ID
  // Lot-level procurement project — all relative to lot element
  val lotProjectInternalIDPath: XMLPath    // root/cac:ProcurementProject/cbc:ID
  val lotProjectNamePath: XMLPath          // root/cac:ProcurementProject/cbc:Name
  val lotProjectDescriptionPath: XMLPath   // root/cac:ProcurementProject/cbc:Description
  val lotProjectContractNaturePath: XMLPath // root/cac:ProcurementProject/cbc:ProcurementTypeCode
  val lotProjectNotePath: XMLPath          // root/cac:ProcurementProject/cbc:Note
  val lotProjectEstimatedAmountPath: XMLPath  // root/cac:ProcurementProject/cac:RequestedTenderTotal/cbc:EstimatedOverallContractAmount
  val lotProjectEstimatedCurrencyPath: XMLPath // lotProjectEstimatedAmountPath/@currencyID
  val lotProjectMainCPVPath: XMLPath       // root/cac:ProcurementProject/cac:MainCommodityClassification/cbc:ItemClassificationCode
  val lotProjectAdditionalCPVsPath: XMLPath // root/cac:ProcurementProject/cac:AdditionalCommodityClassification/cbc:ItemClassificationCode
  val lotProjectDeliveryCountryPath: XMLPath // root/cac:ProcurementProject/cac:RealizedLocation/cac:Address/cac:Country/cbc:IdentificationCode
  val lotProjectDeliveryNutsPath: XMLPath  // root/cac:ProcurementProject/cac:RealizedLocation/cac:Address/cbc:CountrySubentityCode
  val lotProjectDurationPath: XMLPath      // root/cac:ProcurementProject/cac:PlannedPeriod/cbc:DurationMeasure

/** Paths for the `efac:NoticeResult` block inside the eForms extension.
  *
  * `root` navigates from the document root to `efac:NoticeResult`. Container
  * paths (ending in `ContainerPath`) are navigated from the document root to
  * obtain element collections. Per-element paths (e.g. `lotResultIDPath`) use
  * first-segment self-match and are navigated on individual child elements.
  */
trait UblNoticeResultPath:
  val root: XMLPath                          // eformsExt/efac:NoticeResult
  val totalAmountPath: XMLPath               // root/cbc:TotalAmount
  val totalAmountCurrencyPath: XMLPath       // root/cbc:TotalAmount/@currencyID
  // LotResult — container + per-element paths (self-match on efac:LotResult)
  val lotResultContainerPath: XMLPath        // root/efac:LotResult
  val lotResultIDPath: XMLPath               // efac:LotResult/cbc:ID
  val tenderResultCodePath: XMLPath          // efac:LotResult/cbc:TenderResultCode
  val lotResultLotIDPath: XMLPath            // efac:LotResult/efac:TenderLot/cbc:ID
  val lotResultContractIDPath: XMLPath       // efac:LotResult/efac:SettledContract/cbc:ID
  val lotResultTenderIDPath: XMLPath         // efac:LotResult/efac:LotTender/cbc:ID
  val statisticsCodePath: XMLPath            // efac:LotResult/efac:ReceivedSubmissionsStatistics/efbc:StatisticsCode
  val statisticsCountPath: XMLPath           // efac:LotResult/efac:ReceivedSubmissionsStatistics/efbc:StatisticsNumeric
  // SettledContract — container + per-element paths (self-match on efac:SettledContract)
  val settledContractContainerPath: XMLPath  // root/efac:SettledContract
  val contractIDPath: XMLPath                // efac:SettledContract/cbc:ID
  val awardDatePath: XMLPath                 // efac:SettledContract/cbc:AwardDate
  val contractIssueDatePath: XMLPath         // efac:SettledContract/cbc:IssueDate
  val contractTitlePath: XMLPath             // efac:SettledContract/cbc:Title
  val contractInternalRefPath: XMLPath       // efac:SettledContract/efac:ContractReference/cbc:ID
  val contractTenderIDPath: XMLPath          // efac:SettledContract/efac:LotTender/cbc:ID
  // LotTender — container + per-element paths (self-match on efac:LotTender)
  val lotTenderContainerPath: XMLPath        // root/efac:LotTender
  val tenderIDPath: XMLPath                  // efac:LotTender/cbc:ID
  val tenderPayableAmountPath: XMLPath       // efac:LotTender/cac:LegalMonetaryTotal/cbc:PayableAmount
  val tenderPayableCurrencyPath: XMLPath     // efac:LotTender/cac:LegalMonetaryTotal/cbc:PayableAmount/@currencyID
  val tenderLotIDPath: XMLPath               // efac:LotTender/efac:TenderLot/cbc:ID
  val tenderPartyIDPath: XMLPath             // efac:LotTender/efac:TenderingParty/cbc:ID
  // TenderingParty — container + per-element paths (self-match on efac:TenderingParty)
  val tenderingPartyContainerPath: XMLPath   // root/efac:TenderingParty
  val tenderingPartyIDPath: XMLPath          // efac:TenderingParty/cbc:ID
  val tenderingPartyOrgIDPath: XMLPath       // efac:TenderingParty/efac:Tenderer/cbc:ID
