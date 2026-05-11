package io.github.khanr1.tedawardparser
package repository
package xml

import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*
import io.github.khanr1.tedawardparser.repository.decoders.ubl.*
import io.github.khanr1.tedawardparser.repository.xpath.ubl.UblNoticePathImpl

/** Decodes a UBL ContractAwardNotice XML element into a [[UblParsedNotice]].
  *
  * Uses [[UblNoticePathImpl]] for all path constants and namespace bindings.
  * Returns `None` for documents that do not look like UBL notices (no
  * `cbc:UBLVersionID` element present at the root).
  */
object UblNoticeAssembler:

  private val P = UblNoticePathImpl
  private val ns = P.namespaces

  def decode(e: Elem): Option[UblParsedNotice] =
    if e.textAt(P.PublicationSectionPath.ublVersionIDPath, ns).isEmpty then None
    else Some(build(e))

  // ── Top-level build ─────────────────────────────────────────────────────────

  private def build(e: Elem): UblParsedNotice =
    val pub = P.PublicationSectionPath
    val cp = P.ContractingPartyPath
    val orgP = P.OrganizationPath
    val proj = P.ProcurementProjectPath
    val lotP = P.ProcurementLotPath
    val result = P.NoticeResultPath

    UblParsedNotice(
      // Publication / header
      noticeTypeCode = e.textAt(pub.noticeTypeCodePath, ns).getOrElse(""),
      noticeUUID = e.textAt(pub.noticeUUIDPath, ns).getOrElse(""),
      contractFolderID = e.textAt(pub.contractFolderIDPath, ns).getOrElse(""),
      issueDate = e.textAt(pub.issueDatePath, ns).getOrElse(""),
      ublVersionID = e.textAt(pub.ublVersionIDPath, ns).getOrElse(""),
      customizationID = e.textAt(pub.customizationIDPath, ns).getOrElse(""),
      versionID = e.textAt(pub.versionIDPath, ns).getOrElse(""),
      regulatoryDomain = e.textAt(pub.regulatoryDomainPath, ns).getOrElse(""),
      noticeLanguageCode =
        e.textAt(pub.noticeLanguageCodePath, ns).getOrElse(""),
      noticePublicationID =
        e.textAt(pub.noticePublicationIDPath, ns).getOrElse(""),
      gazetteID = e.textAt(pub.gazetteIDPath, ns).getOrElse(""),
      publicationDate = e.textAt(pub.publicationDatePath, ns).getOrElse(""),
      noticeSubTypeCode = e.textAt(pub.noticeSubTypeCodePath, ns).getOrElse(""),
      // Contracting party reference
      buyerOrgID = e.textAt(cp.buyerOrgIDPath, ns).getOrElse(""),
      buyerLegalType = e.textAt(cp.buyerLegalTypePath, ns),
      activityType = e.textAt(cp.activityTypePath, ns),
      buyerProfileURI = e.textAt(cp.buyerProfileURIPath, ns),
      // Organizations
      organizations = e.childrenAt(orgP.containerPath, ns).map(decodeOrg),
      // Top-level project
      projectName = e.textAt(proj.namePath, ns).getOrElse(""),
      projectDescription = e.textAt(proj.descriptionPath, ns).getOrElse(""),
      projectContractNature = e.textAt(proj.contractNaturePath, ns),
      projectEstimatedAmount = e.textAt(proj.estimatedAmountPath, ns),
      projectEstimatedCurrency = e.attrAt(proj.estimatedAmountCurrencyPath, ns),
      projectMainCPV = e.textAt(proj.mainCPVPath, ns),
      projectAdditionalCPVs = texts(e.nodesAt(proj.additionalCPVsPath, ns)),
      projectDeliveryCountry = e.textAt(proj.deliveryCountryPath, ns),
      projectDeliveryNuts = e.textAt(proj.deliveryNutsCodePath, ns),
      projectNote = e.textAt(proj.notePath, ns),
      // Lots (schemeName="Lot" only — LotsGroup excluded by containerPath)
      lots = e.childrenAt(lotP.containerPath, ns).map(decodeLot),
      // Award results
      totalAmount = e.textAt(result.totalAmountPath, ns),
      totalAmountCurrency = e.attrAt(result.totalAmountCurrencyPath, ns),
      lotResults =
        e.childrenAt(result.lotResultContainerPath, ns).map(decodeLotResult),
      settledContracts = e
        .childrenAt(result.settledContractContainerPath, ns)
        .map(decodeSettledContract),
      lotTenders =
        e.childrenAt(result.lotTenderContainerPath, ns).map(decodeLotTender),
      tenderingParties = e
        .childrenAt(result.tenderingPartyContainerPath, ns)
        .map(decodeTenderingParty)
    )

  // ── Sub-element decoders ────────────────────────────────────────────────────

  private def decodeOrg(e: Elem): UblOrganizationRaw =
    val p = P.OrganizationPath
    UblOrganizationRaw(
      orgID = e.textAt(p.orgIDPath, ns).getOrElse(""),
      name = e.textAt(p.namePath, ns).getOrElse(""),
      nationalID = e.textAt(p.nationalIDPath, ns).getOrElse(""),
      streetName = e.textAt(p.streetNamePath, ns),
      additionalStreetName = e.textAt(p.additionalStreetNamePath, ns),
      cityName = e.textAt(p.cityNamePath, ns).getOrElse(""),
      postalZone = e.textAt(p.postalZonePath, ns).getOrElse(""),
      nutsCode = e.textAt(p.nutsCodePath, ns),
      countryCode = e.textAt(p.countryCodePath, ns).getOrElse(""),
      websiteURI = e.textAt(p.websiteURIPath, ns),
      contactName = e.textAt(p.contactNamePath, ns),
      phone = e.textAt(p.phonePath, ns),
      email = e.textAt(p.emailPath, ns)
    )

  private def decodeLot(e: Elem): UblLotRaw =
    val p = P.ProcurementLotPath
    val criteria =
      e.childrenAt(p.awardCriteriaContainerPath, ns).map(decodeCriterion)
    UblLotRaw(
      lotID = e.textAt(p.lotIDPath, ns).getOrElse(""),
      lotProjectName = e.textAt(p.lotProjectNamePath, ns).getOrElse(""),
      lotProjectDescription =
        e.textAt(p.lotProjectDescriptionPath, ns).getOrElse(""),
      lotProjectContractNature = e.textAt(p.lotProjectContractNaturePath, ns),
      lotProjectEstimatedAmount = e.textAt(p.lotProjectEstimatedAmountPath, ns),
      lotProjectEstimatedCurrency =
        e.attrAt(p.lotProjectEstimatedCurrencyPath, ns),
      lotProjectMainCPV = e.textAt(p.lotProjectMainCPVPath, ns),
      lotProjectAdditionalCPVs =
        texts(e.nodesAt(p.lotProjectAdditionalCPVsPath, ns)),
      lotProjectDeliveryCountry = e.textAt(p.lotProjectDeliveryCountryPath, ns),
      lotProjectDeliveryNuts = e.textAt(p.lotProjectDeliveryNutsPath, ns),
      lotProjectDuration =
        e.textAt(p.lotProjectDurationPath, ns).flatMap(_.trim.toIntOption),
      lotProjectNote = e.textAt(p.lotProjectNotePath, ns),
      fundingProgramCode = e.textAt(p.fundingProgramCodePath, ns),
      awardCriteria = criteria,
      appealDeadlineDesc = e.textAt(p.appealDeadlineDescPath, ns),
      appealBodyOrgID = e.textAt(p.appealBodyOrgIDPath, ns),
      appealReceiverOrgID = e.textAt(p.appealReceiverOrgIDPath, ns)
    )

  private def decodeCriterion(e: Elem): UblAwardCriterionRaw =
    val p = P.ProcurementLotPath
    UblAwardCriterionRaw(
      criterionType = e.textAt(p.criterionTypePath, ns),
      description = e.textAt(p.criterionDescriptionPath, ns),
      weightPercent =
        e.textAt(p.criterionWeightPath, ns).flatMap(_.trim.toIntOption)
    )

  private def decodeLotResult(e: Elem): UblLotResultRaw =
    val p = P.NoticeResultPath
    UblLotResultRaw(
      resultID = e.textAt(p.lotResultIDPath, ns).getOrElse(""),
      tenderResultCode = e.textAt(p.tenderResultCodePath, ns),
      lotID = e.textAt(p.lotResultLotIDPath, ns).getOrElse(""),
      contractID = e.textAt(p.lotResultContractIDPath, ns).getOrElse(""),
      tenderID = e.textAt(p.lotResultTenderIDPath, ns).getOrElse(""),
      tendersReceivedCount =
        e.textAt(p.statisticsCountPath, ns).flatMap(_.trim.toIntOption)
    )

  private def decodeSettledContract(e: Elem): UblSettledContractRaw =
    val p = P.NoticeResultPath
    UblSettledContractRaw(
      contractID = e.textAt(p.contractIDPath, ns).getOrElse(""),
      awardDate = e.textAt(p.awardDatePath, ns),
      issueDate = e.textAt(p.contractIssueDatePath, ns),
      contractTitle = e.textAt(p.contractTitlePath, ns),
      contractInternalRef = e.textAt(p.contractInternalRefPath, ns),
      tenderID = e.textAt(p.contractTenderIDPath, ns)
    )

  private def decodeLotTender(e: Elem): UblLotTenderRaw =
    val p = P.NoticeResultPath
    UblLotTenderRaw(
      tenderID = e.textAt(p.tenderIDPath, ns).getOrElse(""),
      payableAmount = e.textAt(p.tenderPayableAmountPath, ns),
      payableCurrency = e.attrAt(p.tenderPayableCurrencyPath, ns),
      tenderingPartyID = e.textAt(p.tenderPartyIDPath, ns)
    )

  private def decodeTenderingParty(e: Elem): UblTenderingPartyRaw =
    val p = P.NoticeResultPath
    UblTenderingPartyRaw(
      tenderingPartyID = e.textAt(p.tenderingPartyIDPath, ns).getOrElse(""),
      contractorOrgID = e.textAt(p.tenderingPartyOrgIDPath, ns).getOrElse("")
    )

  private def texts(nodes: scala.xml.NodeSeq): List[String] =
    nodes.toList.map(_.text.replaceAll("\\s+", " ").trim).filter(_.nonEmpty)
