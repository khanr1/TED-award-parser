package io.github.khanr1.tedawardparser
package repository
package parsers
package tedexport
package r208
package f03

import io.github.khanr1.tedawardparser.tedexport.r208.f03.PartialContractingAuthorityInformation
import scala.xml.Elem

object PartialContractingAuthorityInformationDecoder {
  val root = XMLPath(
    "FORM_SECTION"
  ) / "CONTRACT_AWARD" / "FD_CONTRACT_AWARD" / "CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD"

  given decoder
      : XMLDecoder[PartialContractingAuthorityInformation[ParserError]] =
    new XMLDecoder[PartialContractingAuthorityInformation[ParserError]] {

      override def decode(
          e: Elem
      ): Either[ParserError, PartialContractingAuthorityInformation[
        ParserError
      ]] = {
        val contractingAuthority =
          PartialContractingAuthorityDecoder.decoder.decode(e)
        val purchasingOnBehalf =
          PartialActivityAndPurchasingOnBehalfDecoder.decoder.decode(e)
        Right(
          PartialContractingAuthorityInformation(
            contractingAuthority,
            purchasingOnBehalf
          )
        )
      }

    }
}
