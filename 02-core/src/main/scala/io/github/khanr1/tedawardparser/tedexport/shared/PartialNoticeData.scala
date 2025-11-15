package io.github.khanr1.tedawardparser.tedexport
package shared

import io.github.khanr1.tedawardparser.common.Country

import squants.market.Money
import scala.util.control.NoStackTrace

final case class PartialNoticeData[E <: NoStackTrace](
    ojsNumber: Either[E, OjsNumber],
    documentURIs: List[Either[E, URL]],
    langages: Either[E, List[Language]],
    buyerCountry: Either[E, Country],
    buyerURL: Either[E, URL],
    CPV: Either[E, CPV],
    values: List[Either[E, (String, Money)]],
    referenceNotice: Either[E, OjsNumber]
)
