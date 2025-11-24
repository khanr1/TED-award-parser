package io.github.khanr1.tedawardparser
package tedexport
package r208
package f03

import cats.syntax.all.*
import cats.{Eq, Show}
import common.*
import scala.util.control.NoStackTrace

final case class PartialContractingAuthority[E <: NoStackTrace](
    name: Either[E, Name],
    NationalID: Either[E, NationalID],
    adress: Either[E, Address],
    town: Either[E, Town],
    postalCode: Either[E, PostalCode],
    country: Either[E, Country],
    pointOfContact: Either[E, PointOfContact],
    phone: Either[E, Phone],
    email: Either[E, List[Email]]
)
