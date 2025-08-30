package io.github.khanr1.tedawardparser
package repository
package file
package r209

object R209Path {

  // Common

  val OjsID: XMLPath =
    XMLPath("CODED_DATA_SECTION", "NOTICE_DATA", "NO_DOC_OJS")
  val PublicationDate: XMLPath =
    XMLPath("CODED_DATA_SECTION", "REF_OJS", "DATE_PUB")

  // Form
  val Form = XMLPath("FORM_SECTION")

  // Contract Award
  val ContractAward = Form / "F03_2014"
  // Voluntary ex ante notive
  val VEAT =
    Form / "F15_2014"

  // Contracting Authority
  val DirectPurchaseContractingAuthority =
    ContractAward / "CONTRACTING_BODY" / "ADDRESS_CONTRACTING_BODY"

  val VeatPurchaseContractingAuthority =
    VEAT / "CONTRACTING_BODY" / "ADDRESS_CONTRACTING_BODY"

  // Contract Award
  val AwardOfContract = ContractAward / "AWARD_CONTRACT"
  val VeatAwardOfContact = VEAT / "AWARD_CONTRACT"

  // Contract Award Information
  val ContractAwardInfo = ContractAward / "OBJECT_CONTRACT"
  val VeatAwardInfo = VEAT / "OBJECT_CONTRACT"

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

  val ContractAwardProcedure = ContractAward / "PROCEDURE"
  val VeatAwardProcedure = VEAT / "PROCEDURE"

}
