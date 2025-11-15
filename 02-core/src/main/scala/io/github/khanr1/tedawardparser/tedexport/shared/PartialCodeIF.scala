package io.github.khanr1.tedawardparser
package tedexport
package shared

import io.github.khanr1.tedawardparser.common.Date
import scala.util.control.NoStackTrace

final case class PartialCodeIF[E <: NoStackTrace](
    dispatchDate: Either[E, Date], // DS_DATE_DISPATCH
    authorityType: Either[E, AuthorityType], // AA_AUTHORITY_TYPE
    documentType: Either[E, DocumentType], // TD_DOCUMENT_TYPE
    contractNature: Either[E, ContractNature], // NC_CONTRACT_NATURE
    procedureType: Either[E, ProcedureType], // PR_PROC
    regulationScope: Either[E, RegulationScope], // RP_REGULATION
    bidType: Either[E, BidType], // TY_TYPE_BID
    awardCriterion: Either[E, AwardCriterion], // AC_AWARD_CRIT
    mainActivity: Either[E, MainActivity], // MA_MAIN_ACTIVITIES
    headingCode: Either[E, HeadingCode], // HEADING
    initiatorCode: Either[E, InitiatorCode], // INITIATOR
    directive: Either[E, Directive]
)
