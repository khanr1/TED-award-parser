package io.github.khanr1.tedawardparser
package ublExport
package types

import cats.{Eq, Show}

opaque type ContractFolderID = String
object ContractFolderID:
  def apply(s: String): ContractFolderID = s
  extension (x: ContractFolderID) def value: String = x
  given Show[ContractFolderID] = Show.fromToString
  given Eq[ContractFolderID]   = Eq.fromUniversalEquals
