package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type RegulationScope = String
object RegulationScope:
  def apply(s: String): RegulationScope = s
  extension (x: RegulationScope) def value: String = x
  given Show[RegulationScope] = Show.fromToString
  given Eq[RegulationScope] = Eq.fromUniversalEquals
