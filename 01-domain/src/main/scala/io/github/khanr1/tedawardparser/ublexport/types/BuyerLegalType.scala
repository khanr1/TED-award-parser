package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type BuyerLegalType = String
object BuyerLegalType:
  def apply(s: String): BuyerLegalType = s
  extension (x: BuyerLegalType) def value: String = x
  given Show[BuyerLegalType] = Show.fromToString
  given Eq[BuyerLegalType]   = Eq.fromUniversalEquals
