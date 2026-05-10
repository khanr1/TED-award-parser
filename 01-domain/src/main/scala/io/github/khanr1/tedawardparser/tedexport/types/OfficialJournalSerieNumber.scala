package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type OfficialJournalSerieNumber = String
object OfficialJournalSerieNumber:
  def apply(s: String): OfficialJournalSerieNumber = s
  extension (x: OfficialJournalSerieNumber) def value: String = x
  given Show[OfficialJournalSerieNumber] = Show.fromToString
  given Eq[OfficialJournalSerieNumber] = Eq.fromUniversalEquals
