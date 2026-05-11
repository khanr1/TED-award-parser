package io.github.khanr1.tedawardparser.ublExport

import io.github.khanr1.tedawardparser.ublExport.formSection.{ContractAwardSection, LotAward, Organization}
import io.github.khanr1.tedawardparser.ublExport.publicationSection.PublicationSection
import io.github.khanr1.tedawardparser.ublExport.types.NoticeTypeCode

/** Root aggregate for a UBL ContractAwardNotice (eForms SDK format).
  * Symmetric to Notice in the TED export domain.
  *
  * All organization references (ORG-0001, etc.) are fully resolved before
  * this object is constructed — no raw XML IDs are exposed at the domain level.
  */
final case class UblNotice(
    noticeTypeCode: NoticeTypeCode,
    publicationSection: PublicationSection,
    formSection: ContractAwardSection
):
  def toCsvRows: List[List[String]] =
    val base = UblNotice.baseFields(this)
    formSection.lotAwards match
      case Nil   => List((base ++ UblNotice.emptyAwardFields).map(UblNotice.escape))
      case awards => awards.map(a => (base ++ UblNotice.awardFields(a)).map(UblNotice.escape))

object UblNotice:

  val csvHeader: List[String] = List(
    // Publication / header
    "Notice Type Code",
    "Notice Sub Type Code",
    "Notice Publication ID",
    "Gazette ID",
    "Publication Date",
    "Notice UUID",
    "Contract Folder ID",
    "Issue Date",
    "Regulatory Domain",
    "Notice Language",
    // Contracting party
    "Buyer Name",
    "Buyer National ID",
    "Buyer Street",
    "Buyer Additional Street",
    "Buyer City",
    "Buyer Postal Code",
    "Buyer Country",
    "Buyer NUTS",
    "Buyer Phone",
    "Buyer Email",
    "Buyer Legal Type",
    "Buyer Activity Type",
    // Top-level project
    "Project Name",
    "Project Description",
    "Contract Nature",
    "Estimated Amount",
    "Estimated Currency",
    "Main CPV",
    "Additional CPVs",
    "Delivery Country",
    "Delivery NUTS",
    // Per-award fields
    "Contract ID",
    "Lot ID",
    "Tender Result Code",
    "Award Date",
    "Contract Title",
    "Contract Internal Ref",
    "Tenders Received",
    "Contract Value",
    "Contract Currency",
    // Contractor
    "Contractor Name",
    "Contractor National ID",
    "Contractor Street",
    "Contractor City",
    "Contractor Postal Code",
    "Contractor Country",
    "Contractor NUTS",
    "Contractor Phone",
    "Contractor Email"
  )

  private[ublExport] def escape(s: String): String =
    if s.contains(",") || s.contains("\"") || s.contains("\n") then
      "\"" + s.replace("\"", "\"\"") + "\""
    else s

  private def fmt(amount: BigDecimal): String =
    if amount.remainder(1) == BigDecimal(0) then amount.toBigInt.toString
    else amount.bigDecimal.stripTrailingZeros.toPlainString

  private def baseFields(n: UblNotice): List[String] =
    val pub  = n.publicationSection
    val hdr  = pub.noticeHeader
    val ref  = pub.publicationReference
    val fs   = n.formSection
    val cp   = fs.contractingParty
    val org  = cp.organization
    val proj = fs.procurementProject

    List(
      n.noticeTypeCode.value,
      hdr.noticeSubTypeCode.value,
      ref.noticePublicationID.value,
      ref.gazetteID.value,
      ref.publicationDate.value,
      hdr.noticeUUID.value,
      hdr.contractFolderID.value,
      hdr.issueDate.value,
      hdr.regulatoryDomain.value,
      hdr.noticeLanguageCode.value,
      // Buyer
      org.name.value,
      org.nationalID.value,
      org.streetName.fold("")(_.value),
      org.additionalStreetName.fold("")(_.value),
      org.cityName.value,
      org.postalZone.value,
      org.country.toString,
      org.nutsCode.fold("")(_.value),
      org.phone.fold("")(_.value),
      org.email.fold("")(_.value),
      cp.buyerLegalType.value,
      cp.activityType.value,
      // Project
      proj.name.value,
      proj.description.value,
      proj.contractNature.value,
      proj.estimatedAmount.fold("")(m => fmt(m.amount)),
      proj.estimatedAmount.fold("")(_.currency.code),
      proj.mainCPV.value,
      proj.additionalCPVs.map(_.value).mkString("; "),
      proj.deliveryCountry.fold("")(_.toString),
      proj.deliveryNutsCode.fold("")(_.value)
    )

  // eForms codes where no winner was selected and the procedure is closed.
  private val noWinnerCodes: Set[String] = Set("clos-nw", "open-nw")

  private def orgFields(org: Option[Organization], noWinnerMsg: Option[String] = None): List[String] =
    org.fold {
      noWinnerMsg match
        case Some(msg) => msg :: List.fill(8)("")
        case None      => List.fill(9)("")
    } { o =>
      List(
        o.name.value,
        o.nationalID.value,
        o.streetName.fold("")(_.value),
        o.cityName.value,
        o.postalZone.value,
        o.country.toString,
        o.nutsCode.fold("")(_.value),
        o.phone.fold("")(_.value),
        o.email.fold("")(_.value)
      )
    }

  private def awardFields(a: LotAward): List[String] =
    val (value, currency) = a.contractValue match
      case Some(m) => (fmt(m.amount), m.currency.code)
      case None    => ("", "")

    val noWinnerMsg =
      if noWinnerCodes.contains(a.tenderResultCode.value) then
        Some("No winner was chosen and the competition is closed")
      else None

    List(
      a.contractID.value,
      a.lotID.value,
      a.tenderResultCode.value,
      a.awardDate.fold("")(_.value),
      a.contractTitle.fold("")(_.value),
      a.contractInternalRef.fold("")(_.value),
      a.tendersReceived.fold("")(_.toString),
      value,
      currency
    ) ++ orgFields(a.contractor, noWinnerMsg)

  private val emptyAwardFields: List[String] = List.fill(9 + 9)("")
