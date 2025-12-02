package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type AwardCriterion = String
object AwardCriterion:
  def apply(s: String): AwardCriterion = s
  extension (x: AwardCriterion) def value: String = x
  given Show[AwardCriterion] = Show.fromToString
  given Eq[AwardCriterion] = Eq.fromUniversalEquals
