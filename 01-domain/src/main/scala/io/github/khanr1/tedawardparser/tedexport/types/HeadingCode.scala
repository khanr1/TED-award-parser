package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type HeadingCode = String
object HeadingCode:
  def apply(s: String): HeadingCode = s
  extension (x: HeadingCode) def value: String = x
  given Show[HeadingCode] = Show.fromToString
  given Eq[HeadingCode] = Eq.fromUniversalEquals
