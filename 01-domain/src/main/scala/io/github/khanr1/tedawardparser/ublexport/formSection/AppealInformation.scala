package io.github.khanr1.tedawardparser
package ublExport
package formSection

import io.github.khanr1.tedawardparser.tedExport.types.{Description, Name}

/** Appeal and review body information from cac:AppealTerms inside a lot's TenderingTerms.
  * Equivalent to parts of ComplementaryInformation in the TED export domain.
  * Organization names are resolved from ORG-ID references by the parser.
  */
final case class AppealInformation(
    deadlineDescription: Option[Description], // cac:PresentationPeriod/cbc:Description
    appealBodyName: Option[Name],             // resolved from cac:AppealInformationParty ORG-ID
    appealReceiverName: Option[Name]          // resolved from cac:AppealReceiverParty ORG-ID
)
