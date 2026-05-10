package io.github.khanr1.tedawardparser.tedExport.formSection

import io.github.khanr1.tedawardparser.common.Country
import io.github.khanr1.tedawardparser.tedExport.types.*

final case class Contractor(
    name: Name,
    nationalID: NationalID,
    address: Address,
    town: Town,
    postalCode: PostalCode,
    country: Country,
    pointOfContact: PointOfContact,
    phone: Phone,
    email: Email
)
