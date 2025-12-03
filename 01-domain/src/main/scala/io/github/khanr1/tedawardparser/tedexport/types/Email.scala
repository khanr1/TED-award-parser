package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Email = String
object Email:
  def apply(s: String): Email = s
  extension (x: Email) def value: String = x
  given Show[Email] = Show.fromToString
  given Eq[Email] = Eq.fromUniversalEquals
