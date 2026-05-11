package io.github.khanr1.tedawardparser
package ublExport
package publicationSection

/** Aggregates publication metadata for a UBL notice.
  * Equivalent to CodedDataSection in the TED export domain.
  */
final case class PublicationSection(
    noticeHeader: NoticeHeader,
    publicationReference: PublicationReference
)
