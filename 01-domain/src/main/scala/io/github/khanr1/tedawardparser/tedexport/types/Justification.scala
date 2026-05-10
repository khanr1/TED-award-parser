package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Justification = String
object Justification:
  def apply(s: String): Justification = s
  extension (x: Justification) def value: String = x
  given Show[Justification] = Show.fromToString
  given Eq[Justification] = Eq.fromUniversalEquals
