package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type PointOfContact = String
object PointOfContact:
  def apply(s: String): PointOfContact = s
  extension (x: PointOfContact) def value: String = x
  given Show[PointOfContact] = Show.fromToString
  given Eq[PointOfContact] = Eq.fromUniversalEquals
