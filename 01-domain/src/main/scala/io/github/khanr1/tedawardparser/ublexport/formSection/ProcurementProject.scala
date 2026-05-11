package io.github.khanr1.tedawardparser
package ublExport
package formSection

import squants.market.Money
import io.github.khanr1.tedawardparser.common.Country
import io.github.khanr1.tedawardparser.tedExport.types.{CPV, ContractNature, Description, Title}
import io.github.khanr1.tedawardparser.ublExport.types.{Duration, NutsCode}

/** Procurement project details, used for both the top-level project and lot-level projects.
  * Equivalent to ObjectContract in the TED export domain but substantially richer.
  */
final case class ProcurementProject(
    internalID: Option[Title],          // cbc:ID schemeName="InternalID"
    name: Title,                        // cbc:Name
    description: Description,           // cbc:Description
    contractNature: ContractNature,     // cbc:ProcurementTypeCode (supplies/services/works)
    estimatedAmount: Option[Money],     // cac:RequestedTenderTotal/cbc:EstimatedOverallContractAmount
    mainCPV: CPV,                       // cac:MainCommodityClassification/cbc:ItemClassificationCode
    additionalCPVs: List[CPV],         // cac:AdditionalCommodityClassification (0+)
    deliveryCountry: Option[Country],   // cac:RealizedLocation/cac:Address/cac:Country
    deliveryNutsCode: Option[NutsCode], // cac:RealizedLocation/.../cbc:CountrySubentityCode
    duration: Option[Duration],         // cac:PlannedPeriod/cbc:DurationMeasure unitCode="MONTH"
    notes: Option[Description]          // cbc:Note
)
