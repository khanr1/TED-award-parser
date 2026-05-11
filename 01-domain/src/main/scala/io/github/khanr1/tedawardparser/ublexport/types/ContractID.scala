package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type ContractID = String
object ContractID:
  def apply(s: String): ContractID = s
  extension (x: ContractID) def value: String = x
  given Show[ContractID] = Show.fromToString
  given Eq[ContractID]   = Eq.fromUniversalEquals
