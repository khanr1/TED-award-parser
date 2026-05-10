package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Language = String
object Language:
  def apply(s: String): Language = s
  extension (x: Language) def value: String = x
  given Show[Language] = Show.fromToString
  given Eq[Language] = Eq.fromUniversalEquals
