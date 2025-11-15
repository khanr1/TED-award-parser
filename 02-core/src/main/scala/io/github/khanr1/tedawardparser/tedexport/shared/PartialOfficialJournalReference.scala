package io.github.khanr1.tedawardparser
package tedexport
package shared

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import cats.syntax.all.*
import scala.util.control.NoStackTrace
import io.github.khanr1.tedawardparser.common.Date

final case class PartialOfficialJournalReference[E <: NoStackTrace](
    journalSeries: Either[E, JournalSeries],
    journalNumber: Either[E, JournalNumber],
    publicationDate: Either[E, Date]
)
