package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type EuProjectReference = String
object EuProjectReference:
  def apply(s: String): EuProjectReference = s
  extension (x: EuProjectReference) def value: String = x
  given Show[EuProjectReference] = Show.fromToString
  given Eq[EuProjectReference] = Eq.fromUniversalEquals
