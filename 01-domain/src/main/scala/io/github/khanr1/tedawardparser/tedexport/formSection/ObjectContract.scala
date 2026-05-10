package io.github.khanr1.tedawardparser.tedExport.formSection

import squants.market.Money
import io.github.khanr1.tedawardparser.tedExport.types.*

final case class ObjectContract(
    titleOfContract: Option[Title],
    contractDescription: Option[Description],
    totalValue: Option[Money]
)
