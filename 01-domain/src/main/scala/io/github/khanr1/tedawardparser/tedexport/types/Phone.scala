package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Phone = String
object Phone:
  def apply(s: String): Phone = s
  extension (x: Phone) def value: String = x
  given Show[Phone] = Show.fromToString
  given Eq[Phone] = Eq.fromUniversalEquals
