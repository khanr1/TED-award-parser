package io.github.khanr1.tedawardparser

final case class Notice(
    noticeNumber: NoticeNumber,
    PublicationDate: PublicationDate,
    noticeType: NoticeType,
    procurementProcess: ProcurementProcess,
    contractingBody: ContractingBodyName,
    contractingBodyCountry: Country
)
