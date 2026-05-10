package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Description = String
object Description:
  def apply(s: String): Description = s
  extension (x: Description) def value: String = x
  given Show[Description] = Show.fromToString
  given Eq[Description] = Eq.fromUniversalEquals
