package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type ContractingAuthorityActivity = String
object ContractingAuthorityActivity:
  def apply(s: String): ContractingAuthorityActivity = s
  extension (x: ContractingAuthorityActivity) def value: String = x
  given Show[ContractingAuthorityActivity] = Show.fromToString
  given Eq[ContractingAuthorityActivity] = Eq.fromUniversalEquals
