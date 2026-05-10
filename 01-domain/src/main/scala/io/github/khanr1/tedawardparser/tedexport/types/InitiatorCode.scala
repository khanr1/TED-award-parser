package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type InitiatorCode = String
object InitiatorCode:
  def apply(s: String): InitiatorCode = s
  extension (x: InitiatorCode) def value: String = x
  given Show[InitiatorCode] = Show.fromToString
  given Eq[InitiatorCode] = Eq.fromUniversalEquals
