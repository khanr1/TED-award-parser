package io.github.khanr1.tedawardparser
package common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import cats.syntax.all.*
import cats.Show
import cats.Eq

opaque type Date = LocalDate
object Date:
  private val fmt = DateTimeFormatter.ofPattern("yyyyMMdd")
  def apply(s: String): Either[Throwable, Date] =
    Either.catchNonFatal(LocalDate.parse(s, fmt))
  extension (d: Date) def value: String = d.toString()

  given Show[Date] = Show.fromToString
  given Eq[Date] = Eq.fromUniversalEquals

opaque type Name = String
object Name:
  def apply(s: String): Name = s
  extension (x: Name) def value: String = x
  given Show[Name] = Show.fromToString
  given Eq[Name] = Eq.fromUniversalEquals

opaque type NationalID = String
object NationalID:
  def apply(s: String): NationalID = s
  extension (x: NationalID) def value: String = x
  given Show[NationalID] = Show.fromToString
  given Eq[NationalID] = Eq.fromUniversalEquals

opaque type Address = String
object Address:
  def apply(s: String): Address = s
  extension (x: Address) def value: String = x
  given Show[Address] = Show.fromToString
  given Eq[Address] = Eq.fromUniversalEquals

opaque type Town = String
object Town:
  def apply(s: String): Town = s
  extension (x: Town) def value: String = x
  given Show[Town] = Show.fromToString
  given Eq[Town] = Eq.fromUniversalEquals

opaque type PostalCode = String
object PostalCode:
  def apply(s: String): PostalCode = s
  extension (x: PostalCode) def value: String = x
  given Show[PostalCode] = Show.fromToString
  given Eq[PostalCode] = Eq.fromUniversalEquals

opaque type PointOfContact = String
object PointOfContact:
  def apply(s: String): PointOfContact = s
  extension (x: PointOfContact) def value: String = x
  given Show[PointOfContact] = Show.fromToString
  given Eq[PointOfContact] = Eq.fromUniversalEquals

opaque type Phone = String
object Phone:
  def apply(s: String): Phone = s
  extension (x: Phone) def value: String = x
  given Show[Phone] = Show.fromToString
  given Eq[Phone] = Eq.fromUniversalEquals

opaque type Email = String
object Email:
  def apply(s: String): Email = s
  extension (x: Email) def value: String = x
  given Show[Email] = Show.fromToString
  given Eq[Email] = Eq.fromUniversalEquals

opaque type ContractingAuthorityType = String
object ContractingAuthorityType:
  def apply(s: String): ContractingAuthorityType = s
  extension (x: ContractingAuthorityType) def value: String = x
  given Show[ContractingAuthorityType] = Show.fromToString
  given Eq[ContractingAuthorityType] = Eq.fromUniversalEquals

opaque type ContractingAuthorityActivity = String
object ContractingAuthorityActivity:
  def apply(s: String): ContractingAuthorityActivity = s
  extension (x: ContractingAuthorityActivity) def value: String = x
  given Show[ContractingAuthorityActivity] = Show.fromToString
  given Eq[ContractingAuthorityActivity] = Eq.fromUniversalEquals
