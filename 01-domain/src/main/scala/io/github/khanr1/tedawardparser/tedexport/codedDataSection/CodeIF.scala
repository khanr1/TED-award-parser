package io.github.khanr1.tedawardparser
package tedexport
package codedDataSection

import io.github.khanr1.tedawardparser.common.Date
import cats.Show
import cats.Eq

final case class CodeIF(
    dispatchDate: Date, // DS_DATE_DISPATCH
    authorityType: AuthorityType, // AA_AUTHORITY_TYPE
    documentType: DocumentType, // TD_DOCUMENT_TYPE
    contractNature: ContractNature, // NC_CONTRACT_NATURE
    procedureType: ProcedureType, // PR_PROC
    regulationScope: RegulationScope, // RP_REGULATION
    bidType: BidType, // TY_TYPE_BID
    awardCriterion: AwardCriterion, // AC_AWARD_CRIT
    mainActivity: MainActivity, // MA_MAIN_ACTIVITIES
    headingCode: HeadingCode, // HEADING
    initiatorCode: InitiatorCode, // INITIATOR
    directive: Directive
)

opaque type AuthorityType = String
object AuthorityType:
  def apply(s: String): AuthorityType = s
  extension (x: AuthorityType) def value: String = x
  given Show[AuthorityType] = Show.fromToString
  given Eq[AuthorityType] = Eq.fromUniversalEquals

opaque type DocumentType = String
object DocumentType:
  def apply(s: String): DocumentType = s
  extension (x: DocumentType) def value: String = x
  given Show[DocumentType] = Show.fromToString
  given Eq[DocumentType] = Eq.fromUniversalEquals

opaque type ContractNature = String
object ContractNature:
  def apply(s: String): ContractNature = s
  extension (x: ContractNature) def value: String = x
  given Show[ContractNature] = Show.fromToString
  given Eq[ContractNature] = Eq.fromUniversalEquals

opaque type ProcedureType = String
object ProcedureType:
  def apply(s: String): ProcedureType = s
  extension (x: ProcedureType) def value: String = x
  given Show[ProcedureType] = Show.fromToString
  given Eq[ProcedureType] = Eq.fromUniversalEquals

opaque type RegulationScope = String
object RegulationScope:
  def apply(s: String): RegulationScope = s
  extension (x: RegulationScope) def value: String = x
  given Show[RegulationScope] = Show.fromToString
  given Eq[RegulationScope] = Eq.fromUniversalEquals

opaque type BidType = String
object BidType:
  def apply(s: String): BidType = s
  extension (x: BidType) def value: String = x
  given Show[BidType] = Show.fromToString
  given Eq[BidType] = Eq.fromUniversalEquals

opaque type AwardCriterion = String
object AwardCriterion:
  def apply(s: String): AwardCriterion = s
  extension (x: AwardCriterion) def value: String = x
  given Show[AwardCriterion] = Show.fromToString
  given Eq[AwardCriterion] = Eq.fromUniversalEquals

opaque type MainActivity = String
object MainActivity:
  def apply(s: String): MainActivity = s
  extension (x: MainActivity) def value: String = x
  given Show[MainActivity] = Show.fromToString
  given Eq[MainActivity] = Eq.fromUniversalEquals

opaque type HeadingCode = String
object HeadingCode:
  def apply(s: String): HeadingCode = s
  extension (x: HeadingCode) def value: String = x
  given Show[HeadingCode] = Show.fromToString
  given Eq[HeadingCode] = Eq.fromUniversalEquals

opaque type InitiatorCode = String
object InitiatorCode:
  def apply(s: String): InitiatorCode = s
  extension (x: InitiatorCode) def value: String = x
  given Show[InitiatorCode] = Show.fromToString
  given Eq[InitiatorCode] = Eq.fromUniversalEquals

opaque type Directive = String
object Directive:
  def apply(s: String): Directive = s
  extension (x: Directive) def value: String = x
  given Show[Directive] = Show.fromToString
  given Eq[Directive] = Eq.fromUniversalEquals
