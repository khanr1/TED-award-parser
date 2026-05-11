package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type NoticePublicationID = String
object NoticePublicationID:
  def apply(s: String): NoticePublicationID = s
  extension (x: NoticePublicationID) def value: String = x
  given Show[NoticePublicationID] = Show.fromToString
  given Eq[NoticePublicationID]   = Eq.fromUniversalEquals
