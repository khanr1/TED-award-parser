package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type NoticeSubTypeCode = String
object NoticeSubTypeCode:
  def apply(s: String): NoticeSubTypeCode = s
  extension (x: NoticeSubTypeCode) def value: String = x
  given Show[NoticeSubTypeCode] = Show.fromToString
  given Eq[NoticeSubTypeCode]   = Eq.fromUniversalEquals
