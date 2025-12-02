package io.github.khanr1.tedawardparser
package repository
package xml
package decoders
package codedDataSection

import scala.xml.Elem
import io.github.khanr1.tedawardparser.repository.xpath.CodedDataSectionPath
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.textAtOrError
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.attrAt
import cats.syntax.all.*

final case class CodeIFRaw(
    dispatchDate: Either[ParserError, String], // DS_DATE_DISPATCH
    authorityType: Either[ParserError, String], // AA_AUTHORITY_TYPE
    documentType: Either[ParserError, String], // TD_DOCUMENT_TYPE
    contractNature: Either[ParserError, String], // NC_CONTRACT_NATURE
    procedureType: Either[ParserError, String], // PR_PROC
    regulationScope: Either[ParserError, String], // RP_REGULATION
    bidType: Either[ParserError, String], // TY_TYPE_BID
    awardCriterion: Either[ParserError, String], // AC_AWARD_CRIT
    mainActivity: Either[ParserError, String], // MA_MAIN_ACTIVITIES
    headingCode: Either[ParserError, String], // HEADING
    initiatorCode: Either[ParserError, String], // INITIATOR
    directive: Either[ParserError, String]
) extends Raw

object CodeIFRaw:
  given XMLDecoder[CodeIFRaw] = new XMLDecoder[CodeIFRaw] {

    override def decode(e: Elem): CodeIFRaw = {
      val dispatchDate =
        e.textAtOrError(
          CodedDataSectionPath.CodeIFPath.dispatchDatePath,
          "Dispatch Date"
        )

      val authorityType = e
        .textAtOrError(
          CodedDataSectionPath.CodeIFPath.authorityTypePath,
          "Authority type"
        )
      val documentType = e
        .textAtOrError(
          CodedDataSectionPath.CodeIFPath.documentTypePath,
          "Document Type"
        )
      val contractNature = e
        .textAtOrError(
          CodedDataSectionPath.CodeIFPath.contractNaturePath,
          "Contract Nature"
        )
      val procedureType = e
        .textAtOrError(
          CodedDataSectionPath.CodeIFPath.procedureTypePath,
          "Procedure Type"
        )
      val regulationScope = e
        .textAtOrError(
          CodedDataSectionPath.CodeIFPath.regulationScopePath,
          "Regulation Scope"
        )
      val bidType = e
        .textAtOrError(CodedDataSectionPath.CodeIFPath.bidTypePath, "Bid Type")

      val awardCriterion = e
        .textAtOrError(
          CodedDataSectionPath.CodeIFPath.awardCriterionPath,
          "award critetion"
        )
      val mainActivity = e
        .textAtOrError(
          CodedDataSectionPath.CodeIFPath.mainActivityPath,
          "Main Activity"
        )
      val headingCode =
        e.textAtOrError(
          CodedDataSectionPath.CodeIFPath.headingCodePath,
          "Heading Code"
        )
      val initiatorCode = e
        .textAtOrError(
          CodedDataSectionPath.CodeIFPath.initiatorCodePath,
          "Initiator Code"
        )
      val directive = e
        .attrAt(CodedDataSectionPath.CodeIFPath.directivePath)
        .toRight(
          ParserError.MissingField(
            "Directive",
            Some((CodedDataSectionPath.CodeIFPath.directivePath).show)
          )
        )

      CodeIFRaw(
        dispatchDate, // DS_DATE_DISPATCH
        authorityType, // AA_AUTHORITY_TYPE
        documentType, // TD_DOCUMENT_TYPE
        contractNature, // NC_CONTRACT_NATURE
        procedureType, // PR_PROC
        regulationScope, // RP_REGULATION
        bidType, // TY_TYPE_BID
        awardCriterion, // AC_AWARD_CRIT
        mainActivity, // MA_MAIN_ACTIVITIES
        headingCode, // HEADING
        initiatorCode, // INITIATOR
        directive
      )
    }

  }
