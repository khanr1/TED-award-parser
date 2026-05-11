package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type NoticeUUID = String
object NoticeUUID:
  def apply(s: String): NoticeUUID = s
  extension (x: NoticeUUID) def value: String = x
  given Show[NoticeUUID] = Show.fromToString
  given Eq[NoticeUUID]   = Eq.fromUniversalEquals
