package io.github.khanr1.tedawardparser
package ublExport
package formSection

import io.github.khanr1.tedawardparser.ublExport.types.{FundingProgramCode, LotID}

/** A single procurement lot from cac:ProcurementProjectLot.
  * Lots are first-class domain objects in UBL; TED export merged lot data into AwardContract.
  */
final case class ProcurementLot(
    lotID: LotID,                                       // cbc:ID schemeName="Lot"
    procurementProject: ProcurementProject,             // cac:ProcurementProject (lot-specific)
    fundingProgramCode: Option[FundingProgramCode],     // cac:TenderingTerms/cbc:FundingProgramCode
    awardCriteria: List[AwardCriterion],                // cac:TenderingTerms/.../cac:SubordinateAwardingCriterion
    appealInformation: Option[AppealInformation]        // cac:TenderingTerms/cac:AppealTerms
)
