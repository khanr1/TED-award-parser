package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type ContractNature = String
object ContractNature:
  def apply(s: String): ContractNature = s
  extension (x: ContractNature) def value: String = x
  given Show[ContractNature] = Show.fromToString
  given Eq[ContractNature] = Eq.fromUniversalEquals
