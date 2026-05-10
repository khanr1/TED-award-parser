package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type DispatchDate = String
object DispatchDate:
  def apply(s: String): DispatchDate = s
  extension (x: DispatchDate) def value: String = x
  given Show[DispatchDate] = Show.fromToString
  given Eq[DispatchDate] = Eq.fromUniversalEquals
