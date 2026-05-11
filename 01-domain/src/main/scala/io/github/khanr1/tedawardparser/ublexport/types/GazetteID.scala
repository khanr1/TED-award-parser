package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type GazetteID = String
object GazetteID:
  def apply(s: String): GazetteID = s
  extension (x: GazetteID) def value: String = x
  given Show[GazetteID] = Show.fromToString
  given Eq[GazetteID]   = Eq.fromUniversalEquals
