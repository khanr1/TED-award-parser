package io.github.khanr1.tedawardparser
package tedexport
package shared

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import cats.syntax.all.*
import io.github.khanr1.tedawardparser.common.Date
import cats.Show
import cats.Eq

final case class OfficialJournalReference(
    journalSeries: JournalSeries,
    journalNumber: JournalNumber,
    publicationDate: Date
)

opaque type JournalSeries = String
object JournalSeries:
  def apply(s: String): JournalSeries = s
  extension (x: JournalSeries) def value: String = x
  given Show[JournalSeries] = Show.fromToString
  given Eq[JournalSeries] = Eq.fromUniversalEquals

opaque type JournalNumber = String
object JournalNumber:
  def apply(s: String): JournalNumber = s
  extension (x: JournalNumber) def value: String = x
  given Show[JournalNumber] = Show.fromToString
  given Eq[JournalNumber] = Eq.fromUniversalEquals
