package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Directive = String
object Directive:
  def apply(s: String): Directive = s
  extension (x: Directive) def value: String = x
  given Show[Directive] = Show.fromToString
  given Eq[Directive] = Eq.fromUniversalEquals
