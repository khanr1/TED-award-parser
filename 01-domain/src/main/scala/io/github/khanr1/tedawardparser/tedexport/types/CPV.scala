package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type CPV = String
object CPV:
  def apply(s: String): CPV = s
  extension (x: CPV) def value: String = x
  given Show[CPV] = Show.fromToString
  given Eq[CPV] = Eq.fromUniversalEquals
