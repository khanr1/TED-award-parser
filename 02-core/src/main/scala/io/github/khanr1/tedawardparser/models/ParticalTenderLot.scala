package io.github.khanr1.tedawardparser
package models

final case class PartialTenderLot(
    id: Either[DomainError, ContractID],
    title: Either[DomainError, Title],
    description: Either[DomainError, Description],
    value: Either[DomainError, squants.Money],
    awardedSupplier: Option[PartialAwardedSupplier],
    justification: Either[DomainError, Justification]
)
