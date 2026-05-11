package io.github.khanr1.tedawardparser
package tedExport
package types

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Date = LocalDate
object Date:
  private val formats = List(
    DateTimeFormatter.ofPattern("yyyyMMdd"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd")
  )
  def apply(s: String): Either[Throwable, Date] =
    val trimmed = s.trim
    // UBL dates carry timezone offsets ("2024-07-09+02:00", "2025-02-17Z"); strip them
    val dateOnly = if trimmed.length > 10 && (trimmed(10) == '+' || trimmed(10) == 'Z' || trimmed(10) == '-' && trimmed.length > 11)
                   then trimmed.take(10)
                   else trimmed
    formats.iterator
      .map(f => Either.catchNonFatal(LocalDate.parse(dateOnly, f)))
      .collectFirst { case Right(d) => d }
      .toRight(new Exception(s"Cannot parse date: $s"))

  extension (d: Date) def value: String = d.toString()

  given Show[Date] = Show.fromToString
  given Eq[Date] = Eq.fromUniversalEquals
