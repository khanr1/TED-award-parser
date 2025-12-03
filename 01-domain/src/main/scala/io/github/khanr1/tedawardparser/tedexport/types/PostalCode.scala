package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type PostalCode = String
object PostalCode:
  def apply(s: String): PostalCode = s
  extension (x: PostalCode) def value: String = x
  given Show[PostalCode] = Show.fromToString
  given Eq[PostalCode] = Eq.fromUniversalEquals
