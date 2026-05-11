package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type FundingProgramCode = String
object FundingProgramCode:
  def apply(s: String): FundingProgramCode = s
  extension (x: FundingProgramCode) def value: String = x
  given Show[FundingProgramCode] = Show.fromToString
  given Eq[FundingProgramCode]   = Eq.fromUniversalEquals
