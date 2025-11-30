package io.github.khanr1.tedawardparser
package tedExport
package types

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import cats.{Show, Eq}
import cats.syntax.all.*

opaque type Date = LocalDate
object Date:
  private val fmt = DateTimeFormatter.ofPattern("yyyyMMdd")
  def apply(s: String): Either[Throwable, Date] =
    Either.catchNonFatal(LocalDate.parse(s, fmt))
  extension (d: Date) def value: String = d.toString()

  given Show[Date] = Show.fromToString
  given Eq[Date] = Eq.fromUniversalEquals
