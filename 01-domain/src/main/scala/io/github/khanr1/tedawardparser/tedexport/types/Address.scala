package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Address = String
object Address:
  def apply(s: String): Address = s
  extension (x: Address) def value: String = x
  given Show[Address] = Show.fromToString
  given Eq[Address] = Eq.fromUniversalEquals
