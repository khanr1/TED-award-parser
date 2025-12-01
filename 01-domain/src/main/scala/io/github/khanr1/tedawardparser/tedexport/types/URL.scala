package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type URL = String
object URL:
  def apply(s: String): URL = s
  extension (x: URL) def value: String = x
  given Show[URL] = Show.fromToString
  given Eq[URL] = Eq.fromUniversalEquals
