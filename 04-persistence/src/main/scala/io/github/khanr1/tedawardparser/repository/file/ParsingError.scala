package io.github.khanr1.tedawardparser.repository.file

import cats.kernel.Eq

enum ParsingError:
  case NoPublicationDate
  case NoOJSID
  case NoContractingAuthorityName
  case NoContractingAuthorityCountry
