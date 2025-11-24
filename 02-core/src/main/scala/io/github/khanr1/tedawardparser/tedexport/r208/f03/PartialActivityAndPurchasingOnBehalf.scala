package io.github.khanr1.tedawardparser.tedexport
package r208
package f03

import io.github.khanr1.tedawardparser.common.*
import scala.util.control.NoStackTrace

final case class PartialActivityAndPurchasingOnBehalf[E <: NoStackTrace](
    contractingAuthorityType: Either[E, ContractingAuthorityType],
    ContraactingAuthorityActivity: Either[E, ContractingAuthorityActivity],
    PurchasingInBehafl: Option[PartialContractingAuthority[E]]
)
