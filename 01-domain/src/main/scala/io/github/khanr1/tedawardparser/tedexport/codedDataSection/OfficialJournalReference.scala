package io.github.khanr1.tedawardparser
package tedExport
package codedDataSection

import cats.syntax.all.*
import cats.Show
import cats.Eq
import io.github.khanr1.tedawardparser.tedExport.types.{
  OfficialJournalSeries,
  Date,
  OfficialJournalNumber
}

final case class OfficialJournalReference(
    journalSeries: OfficialJournalSeries,
    journalNumber: OfficialJournalNumber,
    publicationDate: Date
)
