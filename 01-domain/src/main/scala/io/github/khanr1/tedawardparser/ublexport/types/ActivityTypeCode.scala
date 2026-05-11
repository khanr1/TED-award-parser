package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type ActivityTypeCode = String
object ActivityTypeCode:
  def apply(s: String): ActivityTypeCode = s
  extension (x: ActivityTypeCode) def value: String = x
  given Show[ActivityTypeCode] = Show.fromToString
  given Eq[ActivityTypeCode]   = Eq.fromUniversalEquals
