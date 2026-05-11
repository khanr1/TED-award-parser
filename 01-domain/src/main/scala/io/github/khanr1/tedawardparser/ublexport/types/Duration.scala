package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type Duration = Int
object Duration:
  def apply(n: Int): Duration = n
  extension (x: Duration) def value: Int = x
  given Show[Duration] = Show.fromToString
  given Eq[Duration]   = Eq.fromUniversalEquals
