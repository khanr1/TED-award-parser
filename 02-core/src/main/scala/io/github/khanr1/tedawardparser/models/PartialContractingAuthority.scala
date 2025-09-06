package io.github.khanr1.tedawardparser
package models

final case class PartialContractingAuthority(
    name: Either[DomainError, ContractingAuthorityName],
    country: Either[DomainError, Country]
)
