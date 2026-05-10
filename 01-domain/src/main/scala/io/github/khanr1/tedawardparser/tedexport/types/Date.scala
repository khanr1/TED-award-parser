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
    formats.iterator
      .map(f => Either.catchNonFatal(LocalDate.parse(s.trim, f)))
      .collectFirst { case Right(d) => d }
      .toRight(new Exception(s"Cannot parse date: $s"))

  extension (d: Date) def value: String = d.toString()

  given Show[Date] = Show.fromToString
  given Eq[Date] = Eq.fromUniversalEquals
