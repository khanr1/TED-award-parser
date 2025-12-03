package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type ContractingAuthorityType = String
object ContractingAuthorityType:
  def apply(s: String): ContractingAuthorityType = s
  extension (x: ContractingAuthorityType) def value: String = x
  given Show[ContractingAuthorityType] = Show.fromToString
  given Eq[ContractingAuthorityType] = Eq.fromUniversalEquals
