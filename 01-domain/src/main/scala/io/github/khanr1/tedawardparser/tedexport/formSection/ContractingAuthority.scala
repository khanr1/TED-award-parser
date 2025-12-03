package io.github.khanr1.tedawardparser.tedExport.formSection

import io.github.khanr1.tedawardparser.common.Country
import io.github.khanr1.tedawardparser.tedExport.types.*

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

final case class ActivityAndPurchasingOnBehalf(
    contractingAuthorityType: ContractingAuthorityType,
    contraactingAuthorityActivity: ContractingAuthorityActivity,
    purchasingOnBehalf: Option[ContractingAuthority]
)
