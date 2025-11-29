package io.github.khanr1.tedawardparser

import common.*

import cats.{Show, Eq}

final case class AwardedSupplier(
    name: AwardedSupplierName,
    countryCode: Country
)

opaque type AwardedSupplierName = String

object AwardedSupplierName:
  def apply(s: String): AwardedSupplierName = s
  extension (name: AwardedSupplierName) def value: String = name

  given Eq[AwardedSupplierName] = Eq.fromUniversalEquals
  given Show[AwardedSupplierName] = Show.show(name => name.value)
