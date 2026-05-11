package io.github.khanr1.tedawardparser
package repository
package xml

import scala.util.Try
import squants.market.{Money, defaultCurrencySet}
import io.github.khanr1.tedawardparser.common.Country
import io.github.khanr1.tedawardparser.tedExport.types.{
  Address,
  CPV,
  ContractNature,
  Date,
  Description,
  Email,
  Language,
  Name,
  NationalID,
  Phone,
  PointOfContact,
  PostalCode,
  Title,
  Town,
  URL
}
import io.github.khanr1.tedawardparser.ublExport.*
import io.github.khanr1.tedawardparser.ublExport.formSection.*
import io.github.khanr1.tedawardparser.ublExport.publicationSection.*
import io.github.khanr1.tedawardparser.ublExport.types.*
import io.github.khanr1.tedawardparser.repository.decoders.ubl.*

/** Maps a [[UblParsedNotice]] (raw strings from XML) to a typed [[UblNotice]].
  *
  * Organization references (ORG-ID strings) are resolved here by building a
  * lookup map from the organizations list before constructing other domain
  * objects.
  */
object UblNoticeMapper:

  def toDomain(p: UblParsedNotice): UblNotice =
    val orgMap: Map[String, UblOrganizationRaw] =
      p.organizations.map(o => o.orgID -> o).toMap

    UblNotice(
      noticeTypeCode = NoticeTypeCode(p.noticeTypeCode),
      publicationSection = mapPublication(p),
      formSection = mapContractAward(p, orgMap)
    )

  // ── Helpers ─────────────────────────────────────────────────────────────────

  private def opt(s: String): Option[String] = Option(s).filter(_.nonEmpty)
  private def optS(s: Option[String]): Option[String] = s.filter(_.nonEmpty)

  private def parseMoney(amount: String, currency: String): Option[Money] =
    for
      amt <- Try(amount.trim.replace(",", ".").toDouble).toOption
      curr <- defaultCurrencySet.find(_.code == currency.trim.toUpperCase)
    yield Money(amt, curr)

  private def parseMoneyCombined(raw: String): Option[Money] =
    val parts = raw.trim.split(" ", 2)
    if parts.length == 2 then parseMoney(parts(0), parts(1)) else None

  private def parseDate(s: String): Option[Date] = Date(s.trim).toOption

  // ── Publication section ──────────────────────────────────────────────────────

  private def mapPublication(p: UblParsedNotice): PublicationSection =
    PublicationSection(
      noticeHeader = NoticeHeader(
        noticeUUID = NoticeUUID(p.noticeUUID),
        contractFolderID = ContractFolderID(p.contractFolderID),
        issueDate = parseDate(p.issueDate).getOrElse(
          Date("19700101").getOrElse(throw new RuntimeException("impossible"))
        ),
        ublVersionID = UBLVersionID(p.ublVersionID),
        customizationID = CustomizationID(p.customizationID),
        versionID = VersionID(p.versionID),
        regulatoryDomain = RegulatoryDomain(p.regulatoryDomain),
        noticeLanguageCode = Language(p.noticeLanguageCode),
        noticeSubTypeCode = NoticeSubTypeCode(p.noticeSubTypeCode)
      ),
      publicationReference = PublicationReference(
        noticePublicationID = NoticePublicationID(p.noticePublicationID),
        gazetteID = GazetteID(p.gazetteID),
        publicationDate = parseDate(p.publicationDate).getOrElse(
          Date("19700101").getOrElse(throw new RuntimeException("impossible"))
        )
      )
    )

  // ── Main form section ────────────────────────────────────────────────────────

  private def mapContractAward(
      p: UblParsedNotice,
      orgMap: Map[String, UblOrganizationRaw]
  ): ContractAwardSection =
    val buyer = orgMap.get(p.buyerOrgID)
    val awards = buildLotAwards(p, orgMap)

    ContractAwardSection(
      contractingParty = mapContractingParty(p, buyer),
      procurementProject = mapProject(p),
      lots = p.lots.map(l => mapLot(l, orgMap)),
      lotAwards = awards,
      totalAmount = for
        amt <- p.totalAmount
        curr <- p.totalAmountCurrency
        m <- parseMoney(amt, curr)
      yield m
    )

  // ── ContractingParty ─────────────────────────────────────────────────────────

  private def mapContractingParty(
      p: UblParsedNotice,
      buyer: Option[UblOrganizationRaw]
  ): ContractingParty =
    ContractingParty(
      organization = buyer.map(mapOrg).getOrElse(emptyOrg),
      buyerLegalType = BuyerLegalType(p.buyerLegalType.getOrElse("")),
      activityType = ActivityTypeCode(p.activityType.getOrElse("")),
      buyerProfileURI = p.buyerProfileURI.filter(_.nonEmpty).map(URL.apply)
    )

  private val emptyOrg: Organization = Organization(
    name = Name(""),
    nationalID = NationalID(""),
    streetName = None,
    additionalStreetName = None,
    cityName = Town(""),
    postalZone = PostalCode(""),
    nutsCode = None,
    country = Country.Unknown,
    websiteURI = None,
    contactName = None,
    phone = None,
    email = None
  )

  private def mapOrg(raw: UblOrganizationRaw): Organization =
    Organization(
      name = Name(raw.name),
      nationalID = NationalID(raw.nationalID),
      streetName = optS(raw.streetName).map(Address.apply),
      additionalStreetName = optS(raw.additionalStreetName).map(Address.apply),
      cityName = Town(raw.cityName),
      postalZone = PostalCode(raw.postalZone),
      nutsCode = optS(raw.nutsCode).map(NutsCode.apply),
      country = Country.toDomain(raw.countryCode),
      websiteURI = optS(raw.websiteURI).map(URL.apply),
      contactName = optS(raw.contactName).map(PointOfContact.apply),
      phone = optS(raw.phone).map(Phone.apply),
      email = optS(raw.email).map(Email.apply)
    )

  // ── ProcurementProject ───────────────────────────────────────────────────────

  private def mapProject(p: UblParsedNotice): ProcurementProject =
    ProcurementProject(
      internalID = None,
      name = Title(p.projectName),
      description = Description(p.projectDescription),
      contractNature = ContractNature(p.projectContractNature.getOrElse("")),
      estimatedAmount = for
        amt <- p.projectEstimatedAmount
        curr <- p.projectEstimatedCurrency
        m <- parseMoney(amt, curr)
      yield m,
      mainCPV = CPV(p.projectMainCPV.getOrElse("")),
      additionalCPVs = p.projectAdditionalCPVs.map(CPV.apply),
      deliveryCountry = p.projectDeliveryCountry.map(Country.toDomain),
      deliveryNutsCode =
        p.projectDeliveryNuts.filter(_.nonEmpty).map(NutsCode.apply),
      duration = None,
      notes = p.projectNote.filter(_.nonEmpty).map(Description.apply)
    )

  private def mapLotProject(l: UblLotRaw): ProcurementProject =
    ProcurementProject(
      internalID = None,
      name = Title(l.lotProjectName),
      description = Description(l.lotProjectDescription),
      contractNature = ContractNature(l.lotProjectContractNature.getOrElse("")),
      estimatedAmount = for
        amt <- l.lotProjectEstimatedAmount
        curr <- l.lotProjectEstimatedCurrency
        m <- parseMoney(amt, curr)
      yield m,
      mainCPV = CPV(l.lotProjectMainCPV.getOrElse("")),
      additionalCPVs = l.lotProjectAdditionalCPVs.map(CPV.apply),
      deliveryCountry = l.lotProjectDeliveryCountry.map(Country.toDomain),
      deliveryNutsCode =
        l.lotProjectDeliveryNuts.filter(_.nonEmpty).map(NutsCode.apply),
      duration = l.lotProjectDuration.map(Duration.apply),
      notes = l.lotProjectNote.filter(_.nonEmpty).map(Description.apply)
    )

  // ── ProcurementLot ───────────────────────────────────────────────────────────

  private def mapLot(
      l: UblLotRaw,
      orgMap: Map[String, UblOrganizationRaw]
  ): ProcurementLot =
    val appealInfo =
      if l.appealDeadlineDesc.isDefined || l.appealBodyOrgID.isDefined then
        Some(
          AppealInformation(
            deadlineDescription =
              l.appealDeadlineDesc.filter(_.nonEmpty).map(Description.apply),
            appealBodyName =
              l.appealBodyOrgID.flatMap(orgMap.get).map(o => Name(o.name)),
            appealReceiverName =
              l.appealReceiverOrgID.flatMap(orgMap.get).map(o => Name(o.name))
          )
        )
      else None

    ProcurementLot(
      lotID = LotID(l.lotID),
      procurementProject = mapLotProject(l),
      fundingProgramCode =
        l.fundingProgramCode.filter(_.nonEmpty).map(FundingProgramCode.apply),
      awardCriteria = l.awardCriteria.map(mapCriterion),
      appealInformation = appealInfo
    )

  private def mapCriterion(c: UblAwardCriterionRaw): AwardCriterion =
    AwardCriterion(
      criterionType = AwardCriterionTypeCode(c.criterionType.getOrElse("")),
      description = c.description.filter(_.nonEmpty).map(Description.apply),
      weightPercent = c.weightPercent
    )

  // ── LotAward (joining result + contract + tender + contractor) ───────────────

  private def buildLotAwards(
      p: UblParsedNotice,
      orgMap: Map[String, UblOrganizationRaw]
  ): List[LotAward] =
    val contractMap = p.settledContracts.map(c => c.contractID -> c).toMap
    val lotTenderMap = p.lotTenders.map(t => t.tenderID -> t).toMap
    val tenderingPartyMap =
      p.tenderingParties.map(tp => tp.tenderingPartyID -> tp).toMap

    p.lotResults.map { lr =>
      val contract = contractMap.get(lr.contractID)
      // The winner is the tender referenced by the SettledContract, NOT the first LotTender
      // listed inside LotResult (which lists all submitted tenders for this lot).
      val lotTender = contract.flatMap(_.tenderID).flatMap(lotTenderMap.get)
      val tenderingParty =
        lotTender.flatMap(_.tenderingPartyID).flatMap(tenderingPartyMap.get)
      val contractor =
        tenderingParty.map(_.contractorOrgID).flatMap(orgMap.get).map(mapOrg)

      val contractValue = for
        lt <- lotTender
        amt <- lt.payableAmount
        curr <- lt.payableCurrency
        m <- parseMoney(amt, curr)
      yield m

      LotAward(
        contractID = ContractID(lr.contractID),
        lotID = LotID(lr.lotID),
        tenderResultCode = TenderResultCode(lr.tenderResultCode.getOrElse("")),
        awardDate = contract.flatMap(_.awardDate).flatMap(parseDate),
        contractTitle =
          contract.flatMap(_.contractTitle).filter(_.nonEmpty).map(Title.apply),
        contractInternalRef = contract
          .flatMap(_.contractInternalRef)
          .filter(_.nonEmpty)
          .map(Title.apply),
        tendersReceived = lr.tendersReceivedCount,
        contractValue = contractValue,
        contractor = contractor
      )
    }
