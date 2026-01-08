package io.github.khanr1.tedawardparser.tedExport
package formSection

import io.github.khanr1.tedawardparser.tedExport.types.*
import squants.Money

final case class AwardContract(
    contractNumber: Option[ContractNumber],
    lotNumber: Option[LotNumber],
    contractTitle: Title,
    awardDate: Date,
    contractor: Contractor,
    contractValue: Option[Money]
)
