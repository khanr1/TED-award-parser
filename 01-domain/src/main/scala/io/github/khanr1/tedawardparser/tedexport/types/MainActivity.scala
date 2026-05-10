package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type MainActivity = String
object MainActivity:
  def apply(s: String): MainActivity = s
  extension (x: MainActivity) def value: String = x
  given Show[MainActivity] = Show.fromToString
  given Eq[MainActivity] = Eq.fromUniversalEquals
