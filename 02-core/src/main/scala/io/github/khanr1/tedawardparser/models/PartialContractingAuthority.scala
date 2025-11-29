package io.github.khanr1.tedawardparser
package models

import common.*

final case class PartialContractingAuthority(
    name: Either[DomainError, ContractingAuthorityName],
    country: Either[DomainError, Country]
)
