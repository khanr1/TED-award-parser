package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type NutsCode = String
object NutsCode:
  def apply(s: String): NutsCode = s
  extension (x: NutsCode) def value: String = x
  given Show[NutsCode] = Show.fromToString
  given Eq[NutsCode]   = Eq.fromUniversalEquals
