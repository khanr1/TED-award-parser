package io.github.khanr1.tedawardparser

import cats.{Eq, Show}
import common.*

final case class ContractingAuthority(
    name: ContractingAuthorityName,
    countryCode: Country
)

opaque type ContractingAuthorityName = String

object ContractingAuthorityName:
  def apply(s: String): ContractingAuthorityName = s
  extension (name: ContractingAuthorityName) def value: String = name
  given Eq[ContractingAuthorityName] = Eq.fromUniversalEquals
  given Show[ContractingAuthorityName] = Show.show(name => name.value)
