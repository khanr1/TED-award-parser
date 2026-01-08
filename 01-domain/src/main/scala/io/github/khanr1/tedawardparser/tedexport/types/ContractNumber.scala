package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type ContractNumber = String
object ContractNumber:
  def apply(s: String): ContractNumber = s
  extension (x: ContractNumber) def value: String = x
  given Show[ContractNumber] = Show.fromToString
  given Eq[ContractNumber] = Eq.fromUniversalEquals
