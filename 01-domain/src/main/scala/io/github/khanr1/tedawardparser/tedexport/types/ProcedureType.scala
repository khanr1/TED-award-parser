package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type ProcedureType = String
object ProcedureType:
  def apply(s: String): ProcedureType = s
  extension (x: ProcedureType) def value: String = x
  given Show[ProcedureType] = Show.fromToString
  given Eq[ProcedureType] = Eq.fromUniversalEquals
