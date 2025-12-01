package io.github.khanr1.tedawardparser
package tedExport
package codedDataSection

import io.github.khanr1.tedawardparser.tedExport.types.*
import io.github.khanr1.tedawardparser.common.Country

import squants.market.Money

final case class NoticeData(
    ojsNumber: OfficialJournalSerieNumber,
    documentURIs: List[URL],
    langages: List[Language],
    buyerCountry: Country,
    buyerURL: URL,
    CPV: CPV,
    values: List[(String, Money)],
    referenceNotice: OfficialJournalSerieNumber
)
