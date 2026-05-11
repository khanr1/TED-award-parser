package io.github.khanr1.tedawardparser
package repository
package xml

import io.github.khanr1.tedawardparser.tedExport.*
import io.github.khanr1.tedawardparser.tedExport.codedDataSection.*
import io.github.khanr1.tedawardparser.tedExport.formSection.*
import io.github.khanr1.tedawardparser.tedExport.types.*
import io.github.khanr1.tedawardparser.common.Country
import io.github.khanr1.tedawardparser.repository.decoders.tedexport.formSection.*
import io.github.khanr1.tedawardparser.repository.decoders.tedexport.codedDataSection.*
import squants.market.{Money, defaultCurrencySet}

import scala.util.Try

object NoticeMapper:

  def toDomain(p: ParsedNotice): Notice =
    Notice(
      formType = mapFormType(p.format),
      codedDataSection = mapCodedData(p),
      formSection = FormSection.ContractSection(mapContractAward(p))
    )

  private def mapFormType(f: TedFormat): FormType = f match
    case TedFormat.R208F02 => FormType.R208F02
    case TedFormat.R208F03 => FormType.R208F03
    case TedFormat.R208F15 => FormType.R208F15
    case TedFormat.R209F02 => FormType.R209F02
    case TedFormat.R209F03 => FormType.R209F03
    case TedFormat.R209F15 => FormType.R209F15

  private def str(e: Either[ParserError, String]): String = e.getOrElse("")
  private def opt(e: Either[ParserError, String]): Option[String] =
    e.toOption.filter(_.nonEmpty)

  private def parseMoney(raw: String): Option[Money] =
    val parts = raw.trim.split(" ", 2)
    if parts.length == 2 then
      for
        amount <- Try(parts(0).toDouble).toOption
        currency <- defaultCurrencySet.find(_.code == parts(1).trim.toUpperCase)
      yield Money(amount, currency)
    else None

  private def parseDate(raw: String): Option[Date] =
    Date(raw.trim).toOption

  private def mapCodedData(p: ParsedNotice): CodedDataSection =
    val ojr = p.codedDataSection.officialJournalReference
    val nd = p.codedDataSection.noticeData
    val cif = p.codedDataSection.codeIF
    CodedDataSection(
      officialJournalReference = OfficialJournalReference(
        journalSeries = OfficialJournalSeries(str(ojr.journalSerie))
          .getOrElse(OfficialJournalSeries.S),
        journalNumber = OfficialJournalNumber(str(ojr.journalNumber)).getOrElse(
          OfficialJournalNumber("0").getOrElse(
            throw new RuntimeException("impossible")
          )
        ),
        publicationDate = parseDate(str(ojr.date)).getOrElse(
          Date("19700101").getOrElse(throw new RuntimeException("impossible"))
        )
      ),
      noticeData = NoticeData(
        ojsNumber = OfficialJournalSerieNumber(str(nd.ojsNumber)),
        documentURIs = nd.documentURIs.flatMap(_.toOption).map(URL.apply),
        langages = nd.langages.toOption.getOrElse(Nil).map(Language.apply),
        buyerCountry = Country.toDomain(str(nd.buyerCountry)),
        buyerURL = URL(str(nd.buyerURL)),
        CPV = CPV(str(nd.CPV)),
        values = Nil,
        referenceNotice = OfficialJournalSerieNumber(str(nd.referenceNotice))
      ),
      codeIF = CodeIF(
        dispatchDate = parseDate(str(cif.dispatchDate)).getOrElse(
          Date("19700101").getOrElse(throw new RuntimeException("impossible"))
        ),
        authorityType = AuthorityType(str(cif.authorityType)),
        documentType = DocumentType(str(cif.documentType)),
        contractNature = ContractNature(str(cif.contractNature)),
        procedureType = ProcedureType(str(cif.procedureType)),
        regulationScope = RegulationScope(str(cif.regulationScope)),
        bidType = BidType(str(cif.bidType)),
        awardCriterion = AwardCriterion(str(cif.awardCriterion)),
        mainActivity = MainActivity(str(cif.mainActivity)),
        headingCode = HeadingCode(str(cif.headingCode)),
        initiatorCode = InitiatorCode(str(cif.initiatorCode)),
        directive = Directive(str(cif.directive))
      )
    )

  private def mapContractingAuthority(
      raw: ContractingAuthorityRaw
  ): ContractingAuthority =
    ContractingAuthority(
      name = Name(str(raw.name)),
      nationalID = NationalID(str(raw.nationalID)),
      address = Address(str(raw.address)),
      town = Town(str(raw.town)),
      postalCode = PostalCode(str(raw.postalCode)),
      country = Country.toDomain(str(raw.country)),
      pointOfContact = PointOfContact(str(raw.pointOfContact)),
      phone = Phone(str(raw.phone)),
      email = Email(str(raw.email))
    )

  private def mapObjectContract(
      raw: ContractAwardObjectInformationRaw
  ): ObjectContract =
    ObjectContract(
      titleOfContract = opt(raw.titleOfContract).map(Title.apply),
      contractDescription = opt(raw.contractDescription).map(Description.apply),
      totalValue = opt(raw.totalValue).flatMap(parseMoney)
    )

  private def mapAwardContract(raw: AwardContractRaw): AwardContract =
    AwardContract(
      contractNumber = opt(raw.contractNumber).map(ContractNumber.apply),
      lotNumber = opt(raw.lotNumber).map(LotNumber.apply),
      contractTitle = opt(raw.contractTitle).map(Title.apply),
      awardDate = opt(raw.awardDate).flatMap(parseDate),
      contractor = opt(raw.contractorName).map(name =>
        Contractor(
          name = Name(name),
          nationalID = NationalID(opt(raw.contractorNationalID).getOrElse("")),
          address = Address(opt(raw.contractorAddress).getOrElse("")),
          town = Town(opt(raw.contractorTown).getOrElse("")),
          postalCode = PostalCode(opt(raw.contractorPostalCode).getOrElse("")),
          country = Country.toDomain(raw.contractorCountry.getOrElse("")),
          pointOfContact =
            PointOfContact(opt(raw.contractorPointOfContact).getOrElse("")),
          phone = Phone(opt(raw.contractorPhone).getOrElse("")),
          email = Email(opt(raw.contractorEmail).getOrElse(""))
        )
      ),
      contractValue = opt(raw.contractValue).flatMap(parseMoney)
    )

  private def mapComplementaryInfo(
      raw: ComplementaryInformationRaw
  ): ComplementaryInformation =
    ComplementaryInformation(
      additionalInformation = raw.additionalInformation.toOption
        .filter(_.nonEmpty)
        .map(AdditionalInformation.apply),
      relatesToEuProject =
        raw.relatesToEuProject.filter(_.nonEmpty).map(EuProjectReference.apply),
      noticeDispatchDate = raw.noticeDispatchDate.toOption
        .filter(_.nonEmpty)
        .map(DispatchDate.apply),
      appealBodyName =
        raw.appealBodyName.toOption.filter(_.nonEmpty).map(Name.apply),
      justification =
        raw.justification.toOption.filter(_.nonEmpty).map(Justification.apply)
    )

  private def mapContractAward(p: ParsedNotice): ContractAward =
    ContractAward(
      contractingAuthority = mapContractingAuthority(p.contractingAuthority),
      objectOfContract = mapObjectContract(p.objectContract),
      awardContracts = p.awardContracts.map(mapAwardContract),
      complementaryInformartion =
        mapComplementaryInfo(p.complementaryInformation)
    )
