package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Name = String
object Name:
  def apply(s: String): Name = s
  extension (x: Name) def value: String = x
  given Show[Name] = Show.fromToString
  given Eq[Name] = Eq.fromUniversalEquals
