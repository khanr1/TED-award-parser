package io.github.khanr1.tedawardparser
package models

import cats.Show
import common.*

final case class PartialAwardedSupplier(
    name: Either[DomainError, AwardedSupplierName],
    countryCode: Either[DomainError, Country]
)
