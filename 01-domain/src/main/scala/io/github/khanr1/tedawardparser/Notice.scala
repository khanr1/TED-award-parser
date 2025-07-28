package io.github.khanr1.tedawardparser

import cats.{Show, Eq}
import java.time.LocalDate

final case class Notice(
    noticeID: OJSNoticeID,
    publicationDate: LocalDate,
    contractingAuthority: ContractingAuthority,
    lots: List[TenderLot]
)

opaque type OJSNoticeID = String

object OJSNoticeID:
  def apply(s: String): OJSNoticeID = s
  extension (ojs: OJSNoticeID) def value: String = ojs
  given Eq[OJSNoticeID] = Eq.fromUniversalEquals
  given Show[OJSNoticeID] = Show.show(ojs => ojs.value)
