package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type RegulatoryDomain = String
object RegulatoryDomain:
  def apply(s: String): RegulatoryDomain = s
  extension (x: RegulatoryDomain) def value: String = x
  given Show[RegulatoryDomain] = Show.fromToString
  given Eq[RegulatoryDomain]   = Eq.fromUniversalEquals
