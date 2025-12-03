package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type NationalID = String
object NationalID:
  def apply(s: String): NationalID = s
  extension (x: NationalID) def value: String = x
  given Show[NationalID] = Show.fromToString
  given Eq[NationalID] = Eq.fromUniversalEquals
