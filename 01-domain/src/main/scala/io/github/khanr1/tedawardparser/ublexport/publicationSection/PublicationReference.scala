package io.github.khanr1.tedawardparser
package ublExport
package publicationSection

import io.github.khanr1.tedawardparser.tedExport.types.Date
import io.github.khanr1.tedawardparser.ublExport.types.{GazetteID, NoticePublicationID}

/** Publication metadata from efac:Publication inside the eForms extension.
  * Equivalent to OfficialJournalReference in the TED export domain.
  */
final case class PublicationReference(
    noticePublicationID: NoticePublicationID, // efbc:NoticePublicationID e.g. "00112986-2025"
    gazetteID: GazetteID,                     // efbc:GazetteID e.g. "35/2025"
    publicationDate: Date                     // efbc:PublicationDate
)
