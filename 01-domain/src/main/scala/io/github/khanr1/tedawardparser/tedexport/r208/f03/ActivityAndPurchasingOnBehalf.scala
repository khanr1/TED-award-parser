package io.github.khanr1.tedawardparser.tedexport
package r208
package f03

import io.github.khanr1.tedawardparser.common.*

final case class ActivityAndPurchasingOnBehalf(
    contractingAuthorityType: ContractingAuthorityType,
    contraactingAuthorityActivity: ContractingAuthorityActivity,
    purchasingOnBehalf: Option[ContractingAuthority]
)
