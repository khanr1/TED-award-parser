package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type NoticeTypeCode = String
object NoticeTypeCode:
  def apply(s: String): NoticeTypeCode = s
  extension (x: NoticeTypeCode) def value: String = x
  given Show[NoticeTypeCode] = Show.fromToString
  given Eq[NoticeTypeCode]   = Eq.fromUniversalEquals
