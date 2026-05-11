package io.github.khanr1.tedawardparser
package ublExport
package formSection

import squants.market.Money
import io.github.khanr1.tedawardparser.tedExport.types.{Date, Title}
import io.github.khanr1.tedawardparser.ublExport.types.{ContractID, LotID, TenderResultCode}

/** The result of a procurement award for one lot.
  * Combines efac:LotResult + efac:SettledContract + efac:LotTender data,
  * with the winning contractor resolved from the organizations registry.
  * Equivalent to AwardContract in the TED export domain.
  */
final case class LotAward(
    contractID: ContractID,              // efac:SettledContract/cbc:ID e.g. "CON-0001"
    lotID: LotID,                        // efac:TenderLot/cbc:ID e.g. "LOT-0001"
    tenderResultCode: TenderResultCode,  // cbc:TenderResultCode e.g. "selec-w"
    awardDate: Option[Date],             // efac:SettledContract/cbc:AwardDate
    contractTitle: Option[Title],        // efac:SettledContract/cbc:Title
    contractInternalRef: Option[Title],  // efac:ContractReference/cbc:ID (internal reference number)
    tendersReceived: Option[Int],        // efac:ReceivedSubmissionsStatistics where type="tenders"
    contractValue: Option[Money],        // efac:LotTender/cac:LegalMonetaryTotal/cbc:PayableAmount
    contractor: Option[Organization]     // resolved from efac:TenderingParty/efac:Tenderer ORG-ID
)
