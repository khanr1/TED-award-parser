package io.github.khanr1.tedawardparser
package tedExport
package codedDataSection

import types.*

final case class CodeIF(
    dispatchDate: Date, // DS_DATE_DISPATCH
    authorityType: AuthorityType, // AA_AUTHORITY_TYPE
    documentType: DocumentType, // TD_DOCUMENT_TYPE
    contractNature: ContractNature, // NC_CONTRACT_NATURE
    procedureType: ProcedureType, // PR_PROC
    regulationScope: RegulationScope, // RP_REGULATION
    bidType: BidType, // TY_TYPE_BID
    awardCriterion: AwardCriterion, // AC_AWARD_CRIT
    mainActivity: MainActivity, // MA_MAIN_ACTIVITIES
    headingCode: HeadingCode, // HEADING
    initiatorCode: InitiatorCode, // INITIATOR
    directive: Directive
)
