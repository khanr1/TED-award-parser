package io.github.khanr1.tedawardparser
package repository
package parsers
package tedexport
package shared

import cats.syntax.all.*

import io.github.khanr1.tedawardparser.tedexport.shared.PartialCodeIF
import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.textAtOrError
import io.github.khanr1.tedawardparser.common.Date
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.textAt
import io.github.khanr1.tedawardparser.tedexport.shared.AuthorityType
import scala.xml.dtd.DocType
import io.github.khanr1.tedawardparser.tedexport.shared.DocumentType
import io.github.khanr1.tedawardparser.tedexport.shared.ContractNature
import io.github.khanr1.tedawardparser.tedexport.shared.ProcedureType
import io.github.khanr1.tedawardparser.tedexport.shared.RegulationScope
import io.github.khanr1.tedawardparser.tedexport.shared.BidType
import io.github.khanr1.tedawardparser.tedexport.shared.AwardCriterion
import io.github.khanr1.tedawardparser.tedexport.shared.MainActivity
import io.github.khanr1.tedawardparser.tedexport.shared.HeadingCode
import io.github.khanr1.tedawardparser.tedexport.shared.InitiatorCode
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.attrAt
import io.github.khanr1.tedawardparser.tedexport.shared.Directive

object PartialCodeIFDataDecoder {

  def path: XMLPath = XMLPath("CODED_DATA_SECTION") / "CODIF_DATA"
  val dispatchDatePath = path / "DS_DATE_DISPATCH"
  val authorityTypePath = path / "AA_AUTHORITY_TYPE"
  val documentTypePath = path / "TD_DOCUMENT_TYPE"
  val contractNaturePath = path / "NC_CONTRACT_NATURE"
  val procedureTypePath = path / "PR_PROC"
  val regulationScopePath = path / "RP_REGULATION"
  val bidTypePath = path / "TY_TYPE_BID"
  val awardCriterionPath = path / "AC_AWARD_CRIT"
  val mainActivityPath = path / "MA_MAIN_ACTIVITIES"
  val headingCodePath = path / "HEADING"
  val initiatorCodePath = path / "INITIATOR"
  val directivePath = path / "DIRECTIVE" attr ("VALUE")

  given XMLDecoder[PartialCodeIF[ParserError]] =
    new XMLDecoder[PartialCodeIF[ParserError]] {

      override def decode(
          e: Elem
      ): Either[ParserError, PartialCodeIF[ParserError]] = {
        val dispatchDate =
          e.textAtOrError(dispatchDatePath, "Dispatch Date")
            .flatMap(s =>
              Date(s).leftMap(t =>
                ParserError.InvalidFormat(
                  "Date",
                  "yyyymmdd",
                  s,
                  Some(directivePath.show)
                )
              )
            )
        val authorityType = e
          .textAtOrError(authorityTypePath, "Authority type")
          .map(AuthorityType(_))
        val documentType = e
          .textAtOrError(documentTypePath, "Document Type")
          .map(DocumentType(_))
        val contractNature = e
          .textAtOrError(contractNaturePath, "Contract Nature")
          .map(ContractNature(_))
        val procedureType = e
          .textAtOrError(procedureTypePath, "Procedure Type")
          .map(ProcedureType(_))
        val regulationScope = e
          .textAtOrError(regulationScopePath, "Regulation Scope")
          .map(RegulationScope(_))
        val bidType = e.textAtOrError(bidTypePath, "Bid Type").map(BidType(_))
        val awardCriterion = e
          .textAtOrError(awardCriterionPath, "award critetion")
          .map(AwardCriterion(_))
        val mainActivity = e
          .textAtOrError(mainActivityPath, "Main Activity")
          .map(MainActivity(_))
        val headingCode =
          e.textAtOrError(headingCodePath, "Heading Code").map(HeadingCode(_))
        val initiatorCode = e
          .textAtOrError(initiatorCodePath, "Initiator Code")
          .map(InitiatorCode(_))
        val directive = e
          .attrAt(directivePath)
          .toRight(
            ParserError.MissingField("Directive", Some(directivePath.show))
          )
          .map(Directive(_))

        Right(
          PartialCodeIF(
            dispatchDate,
            authorityType,
            documentType,
            contractNature,
            procedureType,
            regulationScope,
            bidType,
            awardCriterion,
            mainActivity,
            headingCode,
            initiatorCode,
            directive
          )
        )
      }

    }

}
