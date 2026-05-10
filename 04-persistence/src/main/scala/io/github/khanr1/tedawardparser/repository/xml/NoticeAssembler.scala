package io.github.khanr1.tedawardparser
package repository
package xml

import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*
import io.github.khanr1.tedawardparser.repository.xml.decoders.codedDataSection.*
import io.github.khanr1.tedawardparser.repository.xml.decoders.formSection.*
import io.github.khanr1.tedawardparser.repository.xpath.{
  FormSectionPathR208,
  FormSectionPathR209
}

sealed trait TedFormat
object TedFormat:
  case object R208F02 extends TedFormat
  case object R208F03 extends TedFormat
  case object R208F15 extends TedFormat
  case object R209F02 extends TedFormat
  case object R209F03 extends TedFormat
  case object R209F15 extends TedFormat

final case class ParsedNotice(
    format: TedFormat,
    codedDataSection: CodedDataSectionRaw,
    contractingAuthority: ContractingAuthorityRaw,
    objectContract: ContractAwardObjectInformationRaw,
    awardContracts: List[AwardContractRaw],
    complementaryInformation: ComplementaryInformationRaw
)

object NoticeAssembler:

  private val formSectionPath = XMLPath("FORM_SECTION")

  private def stubAward(reason: String): AwardContractRaw =
    val missing = (field: String) => Left(ParserError.MissingField(field, None))
    AwardContractRaw(
      contractNumber           = missing("Contract Number"),
      lotNumber                = missing("Lot Number"),
      contractTitle            = missing("Contract Title"),
      awardDate                = missing("Award Date"),
      contractorName           = Right(reason),
      contractorNationalID     = missing("Contractor NationalID"),
      contractorAddress        = missing("Contractor Address"),
      contractorTown           = missing("Contractor Town"),
      contractorPostalCode     = missing("Contractor PostalCode"),
      contractorCountry        = None,
      contractorPointOfContact = missing("Contractor PointOfContact"),
      contractorPhone          = missing("Contractor Phone"),
      contractorEmail          = missing("Contractor Email"),
      contractValue            = missing("Contract Value")
    )

  def detectFormat(e: Elem): Option[TedFormat] =
    val hasF03_2014 = e.nodesAt(formSectionPath / "F03_2014").nonEmpty
    val hasF15_2014 = e.nodesAt(formSectionPath / "F15_2014").nonEmpty
    val hasF02_2014 = e.nodesAt(formSectionPath / "F02_2014").nonEmpty
    val hasFDContractAward =
      e.nodesAt(formSectionPath / "CONTRACT_AWARD" / "FD_CONTRACT_AWARD").nonEmpty
    val hasFDVeat =
      e.nodesAt(formSectionPath / "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE" / "FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE").nonEmpty
    val hasFDContract =
      e.nodesAt(formSectionPath / "CONTRACT" / "FD_CONTRACT").nonEmpty

    if hasF03_2014 then Some(TedFormat.R209F03)
    else if hasF15_2014 then Some(TedFormat.R209F15)
    else if hasF02_2014 then Some(TedFormat.R209F02)
    else if hasFDContractAward then Some(TedFormat.R208F03)
    else if hasFDVeat then Some(TedFormat.R208F15)
    else if hasFDContract then Some(TedFormat.R208F02)
    else None

  def decode(e: Elem): Option[ParsedNotice] =
    detectFormat(e).map { format =>
      import CodedDataSectionRaw.given
      import OfficialJournalReferenceRaw.given
      import NoticeDataRaw.given
      import CodeIFRaw.given
      val codedData = summon[XMLDecoder[CodedDataSectionRaw]].decode(e)
      format match
        case TedFormat.R208F03 | TedFormat.R208F15 => decodeR208(e, format, codedData)
        case TedFormat.R209F03 | TedFormat.R209F15 => decodeR209(e, format, codedData)
        case TedFormat.R208F02                      => decodeR208F02(e, codedData)
        case TedFormat.R209F02                      => decodeR209F02(e, codedData)
    }

  private def decodeR208(
      e: Elem,
      format: TedFormat,
      codedData: CodedDataSectionRaw
  ): ParsedNotice =
    import ContractingAuthorityDecoder208.given
    import ContractAwardObjectInformationDecoder208.given

    val ca  = summon[XMLDecoder[ContractingAuthorityRaw]].decode(e)
    val obj = summon[XMLDecoder[ContractAwardObjectInformationRaw]].decode(e)

    val (formRoot, awardLabel) = format match
      case TedFormat.R208F15 => (FormSectionPathR208.F15.root, "AWARD_OF_CONTRACT_DEFENCE")
      case _                 => (FormSectionPathR208.F03.root, "AWARD_OF_CONTRACT")

    // Take only the first FD_CONTRACT_AWARD block to avoid duplicates when a
    // notice is published in multiple languages (each language produces its own
    // FD_CONTRACT_AWARD with identical AWARD_OF_CONTRACT children).
    val awardElems = e.nodesAt(formRoot)
      .collect { case el: Elem => el }
      .headOption
      .toList
      .flatMap(_.child.collect { case el: Elem if el.label == awardLabel => el })

    val lots = awardElems.map(decodeAwardContractR208(_, format))

    val compInfo = format match
      case TedFormat.R208F15 =>
        import ComplementaryInformationDecoderR208F15.given
        summon[XMLDecoder[ComplementaryInformationRaw]].decode(e)
      case _ =>
        import ComplementaryInformationDecoderR208F03.given
        summon[XMLDecoder[ComplementaryInformationRaw]].decode(e)

    ParsedNotice(format, codedData, ca, obj, lots, compInfo)

  private def decodeAwardContractR208(award: Elem, format: TedFormat): AwardContractRaw =
    val reason =
      if award.child.collect { case el: Elem => el }.isEmpty then Some("No awarded contract")
      else if (award \ "NO_AWARDED_CONTRACT").nonEmpty then Some("No awarded contract")
      else None
    if reason.isDefined then return stubAward(reason.get)

    val path = format match
      case TedFormat.R208F15 => FormSectionPathR208.F15.AwardContractPath
      case _                 => FormSectionPathR208.F03.AwardContractPath

    val currency = award.attrAt(path.contractValueCurrencyPath attr ("CURRENCY"))
    // Prefer FMTVAL attribute (canonical numeric value) over display text which
    // may use space thousands-separators and comma decimal-separators.
    val value: Either[ParserError, String] =
      award.attrAt(path.contractValueAmountPath attr ("FMTVAL"))
        .toRight(ParserError.MissingField("Amount", None))
        .orElse(
          award.textAtOrError(path.contractValueAmountPath, "Amount")
            .map(_.replace(" ", "").replace(",", "."))
        )

    val contractValue = (currency, value) match
      case (Some(c), Right(v)) => Right(s"$v $c")
      case _                   => Left(ParserError.Unknown("not proper currency", None))

    AwardContractRaw(
      contractNumber           = award.textAtOrError(path.contractNumberPath, "Contract Number"),
      lotNumber                = award.textAtOrError(path.lotNumberPath, "Lot Number"),
      contractTitle            = award.textAtOrError(path.contractTitlePath, "Contract Title"),
      awardDate                = award.textAtOrError(path.awardDatePath, "Award Date"),
      contractorName           = award.textAtOrError(path.contractorNamePath, "Contractor Name"),
      contractorNationalID     = award.textAtOrError(path.contractorNationalIDPath, "Contractor NationalID"),
      contractorAddress        = award.textAtOrError(path.contractorAddressPath, "Contractor Address"),
      contractorTown           = award.textAtOrError(path.contractorTownPath, "Contractor Town"),
      contractorPostalCode     = award.textAtOrError(path.contractorPostalCodePath, "Contractor PostalCode"),
      contractorCountry        = award.attrAt(path.contractorCountryPath),
      contractorPointOfContact = award.textAtOrError(path.contractorPointOfContactPath, "Contractor PointOfContact"),
      contractorPhone          = award.textAtOrError(path.contractorPhonePath, "Contractor Phone"),
      contractorEmail          = award.textAtOrError(path.contractorEmailPath, "Contractor Email"),
      contractValue            = contractValue
    )

  private def decodeR209(
      e: Elem,
      format: TedFormat,
      codedData: CodedDataSectionRaw
  ): ParsedNotice =
    import ContractingAuthorityDecoder209.given
    import ContractAwardObjectInformationDecoder209.given

    val ca  = summon[XMLDecoder[ContractingAuthorityRaw]].decode(e)
    val obj = summon[XMLDecoder[ContractAwardObjectInformationRaw]].decode(e)

    val formRoot = format match
      case TedFormat.R209F15 => formSectionPath / "F15_2014"
      case _                 => formSectionPath / "F03_2014"

    val awardContractElems = e
      .nodesAt(formRoot / "AWARD_CONTRACT")
      .collect { case el: Elem => el }
      .toList

    import AwardContractDecoder209.given
    val lots = awardContractElems.map(summon[XMLDecoder[AwardContractRaw]].decode)

    val compInfo = format match
      case TedFormat.R209F15 =>
        import ComplementaryInformationDecoderR209F15.given
        summon[XMLDecoder[ComplementaryInformationRaw]].decode(e)
      case _ =>
        import ComplementaryInformationDecoderR209F03.given
        summon[XMLDecoder[ComplementaryInformationRaw]].decode(e)

    ParsedNotice(format, codedData, ca, obj, lots, compInfo)

  private def decodeR208F02(e: Elem, codedData: CodedDataSectionRaw): ParsedNotice =
    import ContractingAuthorityDecoderR208F02.given
    import ContractAwardObjectInformationDecoderR208F02.given
    import ComplementaryInformationDecoderR208F03.given
    val ca      = summon[XMLDecoder[ContractingAuthorityRaw]].decode(e)
    val obj     = summon[XMLDecoder[ContractAwardObjectInformationRaw]].decode(e)
    val compInfo = summon[XMLDecoder[ComplementaryInformationRaw]].decode(e)
    ParsedNotice(TedFormat.R208F02, codedData, ca, obj, Nil, compInfo)

  private def decodeR209F02(e: Elem, codedData: CodedDataSectionRaw): ParsedNotice =
    import ContractingAuthorityDecoderR209F02.given
    import ContractAwardObjectInformationDecoderR209F02.given
    import ComplementaryInformationDecoderR209F03.given
    val ca      = summon[XMLDecoder[ContractingAuthorityRaw]].decode(e)
    val obj     = summon[XMLDecoder[ContractAwardObjectInformationRaw]].decode(e)
    val compInfo = summon[XMLDecoder[ComplementaryInformationRaw]].decode(e)
    ParsedNotice(TedFormat.R209F02, codedData, ca, obj, Nil, compInfo)
