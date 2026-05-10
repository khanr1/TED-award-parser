package io.github.khanr1.tedawardparser.tedExport

import io.github.khanr1.tedawardparser.tedExport.codedDataSection.CodedDataSection
import io.github.khanr1.tedawardparser.tedExport.formSection.{AwardContract, Contractor, FormSection}

final case class Notice(
    formType: FormType,
    codedDataSection: CodedDataSection,
    formSection: FormSection
):
  def toCsvRows: List[List[String]] =
    val ca   = formSection match { case FormSection.ContractSection(ca) => ca }
    val base = Notice.baseFields(this)

    ca.awardContracts match
      case Nil  => List((base ++ Notice.emptyAwardFields).map(Notice.escape))
      case lots => lots.map(lot => (base ++ Notice.awardFields(lot)).map(Notice.escape))

object Notice:

  // ── Header ────────────────────────────────────────────────────────────────

  val csvHeader: List[String] = List(
    // OfficialJournalReference
    "Journal Series",
    "Journal Number",
    "Publication Date",
    // NoticeData
    "Notice ID",
    "Document URIs",
    "Languages",
    "Buyer Country",
    "Buyer URL",
    "CPV",
    "Reference Notice",
    // CodeIF
    "Dispatch Date",
    "Authority Type",
    "Document Type",
    "Contract Nature",
    "Procedure Type",
    "Regulation Scope",
    "Bid Type",
    "Award Criterion",
    "Main Activity",
    "Heading Code",
    "Initiator Code",
    "Directive",
    // Form
    "Form Type",
    // ContractingAuthority
    "CA Name",
    "CA National ID",
    "CA Address",
    "CA Town",
    "CA Postal Code",
    "CA Country",
    "CA Point of Contact",
    "CA Phone",
    "CA Email",
    // ObjectContract
    "Object Title",
    "Object Description",
    "Object Total Value",
    "Object Total Currency",
    // ComplementaryInformation
    "Additional Information",
    "Relates to EU Project",
    "Notice Dispatch Date",
    "Appeal Body Name",
    "Justification",
    // AwardContract
    "Contract Number",
    "Lot Number",
    "Contract Title",
    "Award Date",
    "Contractor Name",
    "Contractor National ID",
    "Contractor Address",
    "Contractor Town",
    "Contractor Postal Code",
    "Contractor Country",
    "Contractor Point of Contact",
    "Contractor Phone",
    "Contractor Email",
    "Contract Value",
    "Contract Currency"
  )

  // ── Helpers ───────────────────────────────────────────────────────────────

  private[tedExport] def escape(s: String): String =
    if s.contains(",") || s.contains("\"") || s.contains("\n") then
      "\"" + s.replace("\"", "\"\"") + "\""
    else s

  private def fmt(amount: BigDecimal): String =
    if amount.remainder(1) == BigDecimal(0) then amount.toBigInt.toString
    else amount.bigDecimal.stripTrailingZeros.toPlainString

  // ── Field extractors ───────────────────────────────────────────────────────

  private def baseFields(n: Notice): List[String] =
    val ojr  = n.codedDataSection.officialJournalReference
    val nd   = n.codedDataSection.noticeData
    val cif  = n.codedDataSection.codeIF
    val ca   = n.formSection match { case FormSection.ContractSection(ca) => ca }
    val obj  = ca.objectOfContract
    val comp = ca.complementaryInformartion

    List(
      // OfficialJournalReference
      ojr.journalSeries.toString,
      ojr.journalNumber.value.toString,
      ojr.publicationDate.value,
      // NoticeData
      nd.ojsNumber.value,
      nd.documentURIs.map(_.value).mkString("; "),
      nd.langages.map(_.value).mkString("; "),
      nd.buyerCountry.toString,
      nd.buyerURL.value,
      nd.CPV.value,
      nd.referenceNotice.value,
      // CodeIF
      cif.dispatchDate.value,
      cif.authorityType.value,
      cif.documentType.value,
      cif.contractNature.value,
      cif.procedureType.value,
      cif.regulationScope.value,
      cif.bidType.value,
      cif.awardCriterion.value,
      cif.mainActivity.value,
      cif.headingCode.value,
      cif.initiatorCode.value,
      cif.directive.value,
      // Form Type
      n.formType.toString,
      // ContractingAuthority
      ca.contractingAuthority.name.value,
      ca.contractingAuthority.NationalID.value,
      ca.contractingAuthority.adress.value,
      ca.contractingAuthority.town.value,
      ca.contractingAuthority.postalCode.value,
      ca.contractingAuthority.country.toString,
      ca.contractingAuthority.pointOfContact.value,
      ca.contractingAuthority.phone.value,
      ca.contractingAuthority.email.value,
      // ObjectContract
      obj.titleOfContract.fold("")(_.value),
      obj.contractDescription.fold("")(_.value),
      obj.totalValue.fold("")(m => fmt(m.amount)),
      obj.totalValue.fold("")(_.currency.code),
      // ComplementaryInformation
      comp.additionalInformation.fold("")(_.value),
      comp.relatesToEuProject.fold("")(_.value),
      comp.noticeDispatchDate.fold("")(_.value),
      comp.appealBodyName.fold("")(_.value),
      comp.justification.fold("")(_.value)
    )

  private def contractorFields(c: Option[Contractor]): List[String] =
    c.fold(List.fill(9)("")) { ct =>
      List(
        ct.name.value,
        ct.NationalID.value,
        ct.adress.value,
        ct.town.value,
        ct.postalCode.value,
        ct.country.toString,
        ct.pointOfContact.value,
        ct.phone.value,
        ct.email.value
      )
    }

  private def awardFields(lot: AwardContract): List[String] =
    val (value, currency) = lot.contractValue match
      case Some(m) => (fmt(m.amount), m.currency.code)
      case None    => ("", "")

    List(
      lot.contractNumber.fold("")(_.value),
      lot.lotNumber.fold("")(_.value),
      lot.contractTitle.fold("")(_.value),
      lot.awardDate.fold("")(_.value)
    ) ++ contractorFields(lot.contractor) ++ List(value, currency)

  private val emptyAwardFields: List[String] =
    List.fill(4 + 9 + 2)("")
