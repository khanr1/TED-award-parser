package io.github.khanr1.tedawardparser.tedExport
package formSection

import io.github.khanr1.tedawardparser.tedExport.types.*
import squants.Money

final case class AwardContract(
    contractNumber: Option[ContractNumber],
    lotNumber: Option[LotNumber],
    contractTitle: Option[Title],
    awardDate: Option[Date],
    contractor: Option[Contractor],
    contractValue: Option[Money]
)
