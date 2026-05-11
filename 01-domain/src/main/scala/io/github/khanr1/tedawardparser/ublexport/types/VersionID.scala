package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type VersionID = String
object VersionID:
  def apply(s: String): VersionID = s
  extension (x: VersionID) def value: String = x
  given Show[VersionID] = Show.fromToString
  given Eq[VersionID]   = Eq.fromUniversalEquals
