package io.github.khanr1.tedawardparser
package repository
package xpath.tedexport

import io.github.khanr1.tedawardparser.repository.xml.XMLPath

/** Base trait for all form-section path containers. Every versioned variant
  * (R208, R209) has a `root` path that prefixes all nested paths.
  */
trait FormSectionPath:
  val root: XMLPath

/** Paths for a Form F03 (Contract Award Notice) section. */
trait F03:
  val root: XMLPath
  def ContractingAuthorityPath: ContractingAuthorityPath
  def AwardContractPath: AwardContractPath
  def ComplementaryInformationPath: ComplementaryInformationPath

/** Paths for a Form F15 (Voluntary Ex Ante Transparency Notice) section. */
trait F15:
  val root: XMLPath
  def ContractingAuthorityPath: ContractingAuthorityPath
  def AwardContractPath: AwardContractPath
  def ComplementaryInformationPath: ComplementaryInformationPath

/** Paths for the complementary information block of a notice. */
trait ComplementaryInformationPath:
  val root: XMLPath
  val additionalInformationPath: XMLPath
  val relatesToEuProjectYesPath: XMLPath
  val noticeDispatchDatePath: XMLPath
  val appealBodyNamePath: XMLPath
  val justificationPath: XMLPath

/** Paths for the contracting authority identification block. */
trait ContractingAuthorityPath:
  val root: XMLPath
  val officialNamePath: XMLPath
  val nationalIDPath: XMLPath
  val addressPath: XMLPath
  val townPath: XMLPath
  val postalCodePath: XMLPath
  val countryPath: XMLPath
  val pointOfContactPath: XMLPath
  val phonePath: XMLPath
  val emailPath: XMLPath

/** Paths for the activity, authority-type, and purchasing-on-behalf block.
  * Extends [[ContractingAuthorityPath]] with the extra paths needed for the
  * "on behalf of" contracting authority.
  */
trait ActivityAndPurchasingOnBehalfPath extends ContractingAuthorityPath:
  val contractingAuthorityTypePath: XMLPath
  val contractingAuthorityTypePath2: XMLPath
  val contractingAuthorityActivityPath: XMLPath
  val contractingAuthorityActivityPath2: XMLPath
  val pathToInfo: XMLPath

/** Paths for the contract award object information block (title, description,
  * total value).
  */
trait ContractAwardObjectInformationPath:
  val root: XMLPath
  val titlePath: XMLPath
  val descriptionPath: XMLPath
  val valuePath: XMLPath
  val currencyPath: XMLPath

/** Paths for a single `AWARD_OF_CONTRACT` (or `AWARD_OF_CONTRACT_DEFENCE`)
  * element. All paths are relative to that element, not the document root.
  */
trait AwardContractPath:
  val root: XMLPath
  val contractNumberPath: XMLPath
  val contractTitlePath: XMLPath
  val lotNumberPath: XMLPath
  val awardDatePath: XMLPath
  val contractorNamePath: XMLPath
  val contractorNationalIDPath: XMLPath
  val contractorAddressPath: XMLPath
  val contractorTownPath: XMLPath
  val contractorPostalCodePath: XMLPath
  val contractorCountryPath: XMLPath
  val contractorPointOfContactPath: XMLPath
  val contractorPhonePath: XMLPath
  val contractorEmailPath: XMLPath
  val contractValueCurrencyPath: XMLPath
  val contractValueAmountPath: XMLPath

/** Paths for an economic operator (contractor) address block. */
trait ContractorPath:
  val root: XMLPath
  val officialNamePath: XMLPath
  val nationalIDPath: XMLPath
  val addressPath: XMLPath
  val townPath: XMLPath
  val postalCodePath: XMLPath
  val countryPath: XMLPath
  val pointOfContactPath: XMLPath
  val phonePath: XMLPath
  val emailPath: XMLPath
