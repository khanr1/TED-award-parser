package io.github.khanr1.tedawardparser
package tedExport
package types

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type OfficialJournalNumber = Int
object OfficialJournalNumber:
  def apply(s: String): Either[Throwable, OfficialJournalNumber] =
    Either.catchNonFatal(s.toInt)

  extension (x: OfficialJournalNumber) def value: Int = x

  given Show[OfficialJournalNumber] = Show.fromToString
  given Eq[OfficialJournalNumber] = Eq.fromUniversalEquals
