package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type UBLVersionID = String
object UBLVersionID:
  def apply(s: String): UBLVersionID = s
  extension (x: UBLVersionID) def value: String = x
  given Show[UBLVersionID] = Show.fromToString
  given Eq[UBLVersionID]   = Eq.fromUniversalEquals
