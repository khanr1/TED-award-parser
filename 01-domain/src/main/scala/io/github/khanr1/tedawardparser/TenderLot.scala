package io.github.khanr1.tedawardparser

import cats.{Show, Eq}

final case class TenderLot(
    id: ContractID,
    title: Title,
    description: Description,
    value: squants.Money,
    awardedSupplier: Option[AwardedSupplier],
    justification: Justification
)

opaque type ContractID = String

object ContractID:
  def apply(s: String): ContractID = s
  extension (contractID: ContractID) def value: String = contractID
  given Eq[ContractID] = Eq.fromUniversalEquals
  given Show[ContractID] = Show.show(contractID => contractID.value)

opaque type Title = String

object Title:
  def apply(s: String): Title = s
  extension (title: Title) def value: String = title
  given Eq[Title] = Eq.fromUniversalEquals
  given Show[Title] = Show.show(title => title.value)

opaque type Description = String

object Description:
  def apply(s: String): Description = s
  extension (description: Description) def value: String = description
  given Eq[Description] = Eq.fromUniversalEquals
  given Show[Description] = Show.show(description => description.value)

opaque type Justification = String

object Justification:
  def apply(s: String): Justification = s
  extension (justification: Justification) def value: String = justification
  given Eq[Justification] = Eq.fromUniversalEquals
  given Show[Justification] = Show.show(justification => justification.value)
