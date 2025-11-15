package io.github.khanr1.tedawardparser
package tedexport
package shared

import scala.util.control.NoStackTrace

final case class PartialCodedDataSection[E <: NoStackTrace](
    officialJournalReference: PartialOfficialJournalReference[E],
    noticeData: PartialNoticeData[E],
    codeIF: PartialCodeIF[E]
)
