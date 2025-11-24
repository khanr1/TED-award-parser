package io.github.khanr1.tedawardparser
package tedexport
package r208
package f03

import scala.util.control.NoStackTrace

final case class PartialContractingAuthorityInformation[E <: NoStackTrace](
    contractingAuthority: Either[E, PartialContractingAuthority[E]],
    activityAndPurchasingOnBehalf: Either[
      E,
      PartialActivityAndPurchasingOnBehalf[E]
    ]
)
