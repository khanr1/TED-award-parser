package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type LotNumber = String
object LotNumber:
  def apply(s: String): LotNumber = s
  extension (x: LotNumber) def value: String = x
  given Show[LotNumber] = Show.fromToString
  given Eq[LotNumber] = Eq.fromUniversalEquals
