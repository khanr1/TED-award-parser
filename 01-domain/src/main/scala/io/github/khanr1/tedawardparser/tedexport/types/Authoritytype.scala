package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type AuthorityType = String
object AuthorityType:
  def apply(s: String): AuthorityType = s
  extension (x: AuthorityType) def value: String = x
  given Show[AuthorityType] = Show.fromToString
  given Eq[AuthorityType] = Eq.fromUniversalEquals
