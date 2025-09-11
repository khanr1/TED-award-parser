package io.github.khanr1.tedawardparser
package models

import java.time.LocalDate

final case class PartialNotice(
    noticeID: Either[DomainError, OJSNoticeID],
    publicationDate: Either[DomainError, LocalDate],
    contractingAuthority: PartialContractingAuthority,
    lots: List[PartialTenderLot]
)
