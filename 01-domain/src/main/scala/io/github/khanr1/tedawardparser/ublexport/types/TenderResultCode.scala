package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type TenderResultCode = String
object TenderResultCode:
  def apply(s: String): TenderResultCode = s
  extension (x: TenderResultCode) def value: String = x
  given Show[TenderResultCode] = Show.fromToString
  given Eq[TenderResultCode]   = Eq.fromUniversalEquals
