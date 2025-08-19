package io.github.khanr1.tedawardparser
package repository
package file
package r208

object R208Path {

  // Common

  val OjsID: XMLPath =
    XMLPath("CODED_DATA_SECTION", "NOTICE_DATA", "NO_DOC_OJS")
  val PublicationDate: XMLPath =
    XMLPath("CODED_DATA_SECTION", "REF_OJS", "DATE_PUB")

  // Form
  val Form = XMLPath("FORM_SECTION")

  // Contract Award
  val ContractAward = Form / "CONTRACT_AWARD" / "FD_CONTRACT_AWARD"
  // Voluntary ex ante notive
  val VEAT =
    Form / "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE" / "FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE"

  // Contracting Authority
  val DirectPurchaseContractingAuthority =
    ContractAward / "CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD" / "NAME_ADDRESSES_CONTACT_CONTRACT_AWARD" / "CA_CE_CONCESSIONAIRE_PROFILE"

  val DelegatedPurchaseContractingAuthority =
    ContractAward / "CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD" /
      "TYPE_AND_ACTIVITIES_AND_PURCHASING_ON_BEHALF" /
      "PURCHASING_ON_BEHALF" /
      "PURCHASING_ON_BEHALF_YES" /
      "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY"

  val VeatPurchaseContractingAuthority =
    VEAT / "CONTRACTING_AUTHORITY_VEAT" / "NAME_ADDRESSES_CONTACT_VEAT" / "CA_CE_CONCESSIONAIRE_PROFILE"

  // Contract Award
  val AwardOfContract = ContractAward / "AWARD_OF_CONTRACT"
  val VeatAwardOfContact = VEAT / "AWARD_OF_CONTRACT_DEFENCE"

  // Contract Award Information
  val ContractAwardInfo =
    ContractAward / "OBJECT_CONTRACT_INFORMATION_CONTRACT_AWARD_NOTICE" /
      "DESCRIPTION_AWARD_NOTICE_INFORMATION"
  val VeatAwardInfo = VEAT / "OBJECT_VEAT" / "DESCRIPTION_VEAT"

  // Award Justification
  val ContractAwardJustification = ContractAward /
    "PROCEDURE_DEFINITION_CONTRACT_AWARD_NOTICE" /
    "TYPE_OF_PROCEDURE_DEF" /
    "F03_AWARD_WITHOUT_PRIOR_PUBLICATION" /
    "ANNEX_D"

  val VeatAwardJustification = VEAT /
    "PROCEDURE_DEFINITION_VEAT" /
    "TYPE_OF_PROCEDURE_DEF_F15" /
    "F15_PT_NEGOTIATED_WITHOUT_COMPETITION" /
    "ANNEX_D_F15" /
    "ANNEX_D1"

}
