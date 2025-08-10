package io.github.khanr1.tedawardparser.repository.file

import cats.kernel.Eq
import cats.effect.kernel.Par
import scala.annotation.meta.param

enum ParsingError(msg: String):
  def getMessage: String = msg
  case NoPublicationDate
      extends ParsingError(msg = "No publication date could not be parsed")
  case NoOJSID
      extends ParsingError(msg = " No OJS reference ID could not be parsed")
  case NoContractingAuthorityName
      extends ParsingError(
        msg = "No contracting authority Name could not be parsed"
      )
  case NoContractingAuthorityCountry
      extends ParsingError(msg = "No contracting authority could not be parsed")
  case NoContractID extends ParsingError(msg = "No contract ID could be parsed")
  case NoTitle extends ParsingError(msg = "No tender's title could be parsed")
  case NoDescription
      extends ParsingError(msg = "No tender's description could be parse")
  case NoValue extends ParsingError("No value for the tender could be parsed")
  case NoAwardedSupplier
      extends ParsingError("No awarded supplier could be parsed")
  case NoAwardedSupplierCountry
      extends ParsingError(
        "No country for the awarded supplier could be parsed"
      )
  case NoJustification
      extends ParsingError("No tender award justification could be parsed")
