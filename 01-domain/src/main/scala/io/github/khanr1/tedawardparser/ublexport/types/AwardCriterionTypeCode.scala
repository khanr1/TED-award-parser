package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type AwardCriterionTypeCode = String
object AwardCriterionTypeCode:
  def apply(s: String): AwardCriterionTypeCode = s
  extension (x: AwardCriterionTypeCode) def value: String = x
  given Show[AwardCriterionTypeCode] = Show.fromToString
  given Eq[AwardCriterionTypeCode]   = Eq.fromUniversalEquals
