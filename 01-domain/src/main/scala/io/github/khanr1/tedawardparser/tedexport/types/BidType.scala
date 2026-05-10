package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type BidType = String
object BidType:
  def apply(s: String): BidType = s
  extension (x: BidType) def value: String = x
  given Show[BidType] = Show.fromToString
  given Eq[BidType] = Eq.fromUniversalEquals
