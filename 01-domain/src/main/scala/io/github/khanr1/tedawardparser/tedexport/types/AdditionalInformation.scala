package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type AdditionalInformation = String
object AdditionalInformation:
  def apply(s: String): AdditionalInformation = s
  extension (x: AdditionalInformation) def value: String = x
  given Show[AdditionalInformation] = Show.fromToString
  given Eq[AdditionalInformation] = Eq.fromUniversalEquals
