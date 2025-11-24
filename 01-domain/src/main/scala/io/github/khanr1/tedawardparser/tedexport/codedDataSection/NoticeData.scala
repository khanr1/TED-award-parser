package io.github.khanr1.tedawardparser.tedexport
package codedDataSection

import io.github.khanr1.tedawardparser.Country

import squants.market.Money
import cats.Show
import cats.Eq

final case class NoticeData(
    ojsNumber: OjsNumber,
    documentURIs: List[URL],
    langages: List[Language],
    buyerCountry: Country,
    buyerURL: URL,
    CPV: CPV,
    values: List[(String, Money)],
    referenceNotice: OjsNumber
)

opaque type OjsNumber = String
object OjsNumber:
  def apply(s: String): OjsNumber = s
  extension (x: OjsNumber) def value: String = x
  given Show[OjsNumber] = Show.fromToString
  given Eq[OjsNumber] = Eq.fromUniversalEquals

opaque type Language = String
object Language:
  def apply(s: String): Language = s
  extension (x: Language) def value: String = x
  given Show[Language] = Show.fromToString
  given Eq[Language] = Eq.fromUniversalEquals

opaque type CPV = String
object CPV:
  def apply(s: String): CPV = s
  extension (x: CPV) def value: String = x
  given Show[CPV] = Show.fromToString
  given Eq[CPV] = Eq.fromUniversalEquals

opaque type URL = String
object URL:
  def apply(s: String): URL = s
  extension (x: URL) def value: String = x
  given Show[URL] = Show.fromToString
  given Eq[URL] = Eq.fromUniversalEquals
