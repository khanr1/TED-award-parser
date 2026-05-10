package io.github.khanr1.tedawardparser.repository.xml
package decoders
package formSection

import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR208
import io.github.khanr1.tedawardparser.repository.xpath.FormSectionPathR209

final case class AwardContractRaw(
    contractNumber: Either[ParserError, String],
    lotNumber: Either[ParserError, String],
    contractTitle: Either[ParserError, String],
    awardDate: Either[ParserError, String],
    contractorName: Either[ParserError, String],
    contractorNationalID: Either[ParserError, String],
    contractorAddress: Either[ParserError, String],
    contractorTown: Either[ParserError, String],
    contractorPostalCode: Either[ParserError, String],
    contractorCountry: Option[String],
    contractorPointOfContact: Either[ParserError, String],
    contractorPhone: Either[ParserError, String],
    contractorEmail: Either[ParserError, String],
    contractValue: Either[ParserError, String]
) extends Raw

private def emptyAward(reason: String): AwardContractRaw = AwardContractRaw(
  contractNumber           = Left(ParserError.MissingField("Contract Number", None)),
  lotNumber                = Left(ParserError.MissingField("Lot Number", None)),
  contractTitle            = Left(ParserError.MissingField("Contract Title", None)),
  awardDate                = Left(ParserError.MissingField("Award Date", None)),
  contractorName           = Right(reason),
  contractorNationalID     = Left(ParserError.MissingField("Contractor NationalID", None)),
  contractorAddress        = Left(ParserError.MissingField("Contractor Address", None)),
  contractorTown           = Left(ParserError.MissingField("Contractor Town", None)),
  contractorPostalCode     = Left(ParserError.MissingField("Contractor PostalCode", None)),
  contractorCountry        = None,
  contractorPointOfContact = Left(ParserError.MissingField("Contractor PointOfContact", None)),
  contractorPhone          = Left(ParserError.MissingField("Contractor Phone", None)),
  contractorEmail          = Left(ParserError.MissingField("Contractor Email", None)),
  contractValue            = Left(ParserError.MissingField("Contract Value", None))
)

object AwardContractDecoder208:
  given XMLDecoder[AwardContractRaw] =
    new XMLDecoder[AwardContractRaw]:
      override def decode(e: Elem): AwardContractRaw =
        if e.child.collect { case el: scala.xml.Elem => el }.isEmpty then return emptyAward("No awarded contract")
        if (e \ "NO_AWARDED_CONTRACT").nonEmpty then return emptyAward("No awarded contract")

        val path = FormSectionPathR208.F03.AwardContractPath

        val currency = e.attrAt(path.contractValueCurrencyPath attr ("CURRENCY"))
        val value    = e.textAtOrError(path.contractValueAmountPath, "Amount")
        val contractValue = (currency, value) match
          case (Some(c), Right(v)) => Right(s"$v $c")
          case _                   => Left(ParserError.Unknown("not proper currency", None))

        AwardContractRaw(
          contractNumber           = e.textAtOrError(path.contractNumberPath, "Contract Number"),
          lotNumber                = e.textAtOrError(path.lotNumberPath, "Lot Number"),
          contractTitle            = e.textAtOrError(path.contractTitlePath, "Contract Title"),
          awardDate                = e.textAtOrError(path.awardDatePath, "Award Date"),
          contractorName           = e.textAtOrError(path.contractorNamePath, "Contractor Name"),
          contractorNationalID     = e.textAtOrError(path.contractorNationalIDPath, "Contractor NationalID"),
          contractorAddress        = e.textAtOrError(path.contractorAddressPath, "Contractor Address"),
          contractorTown           = e.textAtOrError(path.contractorTownPath, "Contractor Town"),
          contractorPostalCode     = e.textAtOrError(path.contractorPostalCodePath, "Contractor PostalCode"),
          contractorCountry        = e.attrAt(path.contractorCountryPath),
          contractorPointOfContact = e.textAtOrError(path.contractorPointOfContactPath, "Contractor PointOfContact"),
          contractorPhone          = e.textAtOrError(path.contractorPhonePath, "Contractor Phone"),
          contractorEmail          = e.textAtOrError(path.contractorEmailPath, "Contractor Email"),
          contractValue            = contractValue
        )

object AwardContractDecoder209:
  given XMLDecoder[AwardContractRaw] =
    new XMLDecoder[AwardContractRaw]:
      override def decode(e: Elem): AwardContractRaw =
        if e.child.collect { case el: scala.xml.Elem => el }.isEmpty then return emptyAward("No awarded contract")
        if (e \ "NO_AWARDED_CONTRACT").nonEmpty then return emptyAward("No awarded contract")

        val path = FormSectionPathR209.F03.AwardContractPath

        val currency = e.attrAt(path.contractValueAmountPath attr ("CURRENCY"))
        val value    = e.textAtOrError(path.contractValueAmountPath, "Amount")
          .map(_.replace(" ", "").replace(",", "."))
        val contractValue = (currency, value) match
          case (Some(c), Right(v)) => Right(s"$v $c")
          case _                   => Left(ParserError.Unknown("not proper currency", None))

        // R209 files differ: some wrap contractors in <CONTRACTORS>, some use bare <CONTRACTOR>
        val root    = path.root
        val awarded = root / "AWARDED_CONTRACT"
        val base1   = awarded / "CONTRACTORS" / "CONTRACTOR" / "ADDRESS_CONTRACTOR"
        val base2   = awarded / "CONTRACTOR"  / "ADDRESS_CONTRACTOR"

        AwardContractRaw(
          contractNumber           = e.textAtOrError(path.contractNumberPath, "Contract Number"),
          lotNumber                = e.textAtOrError(path.lotNumberPath, "Lot Number"),
          contractTitle            = e.textAtOrError(path.contractTitlePath, "Contract Title"),
          awardDate                = e.textAtOrError(path.awardDatePath, "Award Date"),
          contractorName           = e.firstTextOrError(List(base1 / "OFFICIALNAME",   base2 / "OFFICIALNAME"),   "Contractor Name"),
          contractorNationalID     = e.firstTextOrError(List(base1 / "NATIONALID",     base2 / "NATIONALID"),     "Contractor NationalID"),
          contractorAddress        = e.firstTextOrError(List(base1 / "ADDRESS",        base2 / "ADDRESS"),        "Contractor Address"),
          contractorTown           = e.firstTextOrError(List(base1 / "TOWN",           base2 / "TOWN"),           "Contractor Town"),
          contractorPostalCode     = e.firstTextOrError(List(base1 / "POSTAL_CODE",    base2 / "POSTAL_CODE"),    "Contractor PostalCode"),
          contractorCountry        = e.firstAttr(List(base1 / "COUNTRY" attr "VALUE",  base2 / "COUNTRY" attr "VALUE")),
          contractorPointOfContact = e.firstTextOrError(List(base1 / "CONTACT_POINT",  base2 / "CONTACT_POINT"),  "Contractor PointOfContact"),
          contractorPhone          = e.firstTextOrError(List(base1 / "PHONE",          base2 / "PHONE"),          "Contractor Phone"),
          contractorEmail          = e.firstTextOrError(List(base1 / "E_MAIL",         base2 / "E_MAIL"),         "Contractor Email"),
          contractValue            = contractValue
        )
