package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Town = String
object Town:
  def apply(s: String): Town = s
  extension (x: Town) def value: String = x
  given Show[Town] = Show.fromToString
  given Eq[Town] = Eq.fromUniversalEquals
