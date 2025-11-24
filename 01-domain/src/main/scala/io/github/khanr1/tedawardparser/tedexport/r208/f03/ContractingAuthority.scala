package io.github.khanr1.tedawardparser
package tedexport
package r208
package f03

import cats.syntax.all.*
import cats.{Eq, Show}
import common.*

final case class ContractingAuthority(
    name: Name,
    NationalID: NationalID,
    adress: Address,
    town: Town,
    postalCode: PostalCode,
    country: Country,
    pointOfContact: PointOfContact,
    phone: Phone,
    email: Email,
    activityAndPurchasingOnBehalf: ActivityAndPurchasingOnBehalf
)
