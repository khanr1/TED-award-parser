package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Title = String
object Title:
  def apply(s: String): Title = s
  extension (x: Title) def value: String = x
  given Show[Title] = Show.fromToString
  given Eq[Title] = Eq.fromUniversalEquals
