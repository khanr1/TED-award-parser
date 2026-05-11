package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type CustomizationID = String
object CustomizationID:
  def apply(s: String): CustomizationID = s
  extension (x: CustomizationID) def value: String = x
  given Show[CustomizationID] = Show.fromToString
  given Eq[CustomizationID]   = Eq.fromUniversalEquals
