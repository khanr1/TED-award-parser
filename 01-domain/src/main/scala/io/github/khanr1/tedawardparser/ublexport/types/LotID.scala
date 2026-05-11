package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type LotID = String
object LotID:
  def apply(s: String): LotID = s
  extension (x: LotID) def value: String = x
  given Show[LotID] = Show.fromToString
  given Eq[LotID]   = Eq.fromUniversalEquals
