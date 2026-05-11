package io.github.khanr1.tedawardparser
package ublExport
package formSection

import squants.market.Money

/** The main content section of a UBL ContractAwardNotice.
  * Aggregates all procurement and award data after organization references are resolved.
  * Equivalent to ContractAward in the TED export domain.
  */
final case class ContractAwardSection(
    contractingParty: ContractingParty,     // resolved buyer organization + party metadata
    procurementProject: ProcurementProject, // top-level cac:ProcurementProject
    lots: List[ProcurementLot],             // one per cac:ProcurementProjectLot (schemeName="Lot")
    lotAwards: List[LotAward],              // one per efac:LotResult, with contractor resolved
    totalAmount: Option[Money]              // efac:NoticeResult/cbc:TotalAmount
)
