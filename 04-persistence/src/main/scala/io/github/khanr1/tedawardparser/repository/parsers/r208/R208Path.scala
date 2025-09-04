package io.github.khanr1.tedawardparser
package repository
package parsers
package r208

object R208Path {

  // ======================
  // Bases
  // ======================
  private val Form = XMLPath("FORM_SECTION")

  // ======================
  // CODED_DATA_SECTION
  // ======================
  object CodedDataSection {
    val Root = XMLPath("CODED_DATA_SECTION")
    val OjsID = Root / "NOTICE_DATA" / "NO_DOC_OJS"
    val PublicationDate = Root / "REF_OJS" / "DATE_PUB"
  }

  // ======================
  // FORM_SECTION branches
  // ======================
  object FormSection {

    // -------- Contract Award --------
    object ContractAward {
      val Root = Form / "CONTRACT_AWARD" / "FD_CONTRACT_AWARD"

      object ContractingAuthority {
        // roots used for name/country
        val Direct =
          Root / "CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD" /
            "NAME_ADDRESSES_CONTACT_CONTRACT_AWARD" /
            "CA_CE_CONCESSIONAIRE_PROFILE"

        val Delegated =
          Root / "CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD" /
            "TYPE_AND_ACTIVITIES_AND_PURCHASING_ON_BEHALF" /
            "PURCHASING_ON_BEHALF" /
            "PURCHASING_ON_BEHALF_YES" /
            "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY"

        // common leaves used by parser
        val OrgOfficialName = XMLPath("ORGANISATION") / "OFFICIALNAME"
        val CountryValue = XMLPath("COUNTRY") attr "VALUE"
      }

      // repeating award container
      val AwardOfContract = Root / "AWARD_OF_CONTRACT"

      // "information" block under which titles/descriptions live
      val AwardInfo =
        Root / "OBJECT_CONTRACT_INFORMATION_CONTRACT_AWARD_NOTICE" /
          "DESCRIPTION_AWARD_NOTICE_INFORMATION"

      // leaves used under AwardInfo
      val TitleContract = XMLPath("TITLE_CONTRACT")
      val ShortContractDescription = XMLPath("SHORT_CONTRACT_DESCRIPTION")

      // lot/contract IDs (used directly under AwardOfContract children)
      val ContractNumber = XMLPath("CONTRACT_NUMBER")
      val LotNumber = XMLPath("LOT_NUMBER")

      // awarded supplier (under AwardOfContract children)
      object EconomicOperator {
        val Base =
          XMLPath("ECONOMIC_OPERATOR_NAME_ADDRESS") /
            "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME"

        val OrganisationOfficialName = Base / "ORGANISATION" / "OFFICIALNAME"
        val CountryValue = (Base / "COUNTRY") attr "VALUE"
      }

      // contract value subtree (under AwardOfContract children)
      object ContractValue {
        val Base =
          XMLPath("CONTRACT_VALUE_INFORMATION") /
            "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE"

        val ValueCost = Base / "VALUE_COST"
        def Currency = Base attr "CURRENCY"
      }

      // justification tree and the leaf used by parser
      val JustificationRoot =
        Root / "PROCEDURE_DEFINITION_CONTRACT_AWARD_NOTICE" /
          "TYPE_OF_PROCEDURE_DEF" /
          "F03_AWARD_WITHOUT_PRIOR_PUBLICATION" /
          "ANNEX_D"

      val JustificationReason = XMLPath("REASON_CONTRACT_LAWFUL")
    }

    // -------- VEAT branch --------
    object Veat {
      val Root =
        Form / "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE" /
          "FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE"

      object ContractingAuthority {
        val Veat =
          Root / "CONTRACTING_AUTHORITY_VEAT" /
            "NAME_ADDRESSES_CONTACT_VEAT" /
            "CA_CE_CONCESSIONAIRE_PROFILE"

        // same leaves for name/country
        val OrgOfficialName = XMLPath("ORGANISATION") / "OFFICIALNAME"
        val CountryValue = XMLPath("COUNTRY") attr "VALUE"
      }

      // repeating award container (defence)
      val AwardOfContract = Root / "AWARD_OF_CONTRACT_DEFENCE"

      // info block (titles/descriptions)
      val AwardInfo = Root / "OBJECT_VEAT" / "DESCRIPTION_VEAT"

      // share the same leaves as ContractAward.AwardInfo
      val TitleContract = XMLPath("TITLE_CONTRACT")
      val ShortContractDescription = XMLPath("SHORT_CONTRACT_DESCRIPTION")

      // IDs under veat award children
      val ContractNumber = XMLPath("CONTRACT_NUMBER")
      val LotNumber = XMLPath("LOT_NUMBER")

      // awarded supplier (same structure)
      object EconomicOperator {
        val Base =
          XMLPath("ECONOMIC_OPERATOR_NAME_ADDRESS") /
            "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME"

        val OrganisationOfficialName = Base / "ORGANISATION" / "OFFICIALNAME"
        val CountryValue = (Base / "COUNTRY") attr "VALUE"
      }

      // contract value subtree (same structure)
      object ContractValue {
        val Base =
          XMLPath("CONTRACT_VALUE_INFORMATION") /
            "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE"

        val ValueCost = Base / "VALUE_COST"
        def Currency = Base attr "CURRENCY"
      }

      // justification tree + leaf
      val JustificationRoot =
        Root / "PROCEDURE_DEFINITION_VEAT" /
          "TYPE_OF_PROCEDURE_DEF_F15" /
          "F15_PT_NEGOTIATED_WITHOUT_COMPETITION" /
          "ANNEX_D_F15" /
          "ANNEX_D1"

      val JustificationReason = XMLPath("REASON_CONTRACT_LAWFUL")
    }
  }

  // ======================
  // Compatibility aliases (your existing names — unchanged)
  // ======================

  // Common
  val OjsID: XMLPath = CodedDataSection.OjsID
  val PublicationDate: XMLPath = CodedDataSection.PublicationDate

  // Contract Award & VEAT roots
  val ContractAward: XMLPath = FormSection.ContractAward.Root
  val VEAT: XMLPath = FormSection.Veat.Root

  // Contracting Authority roots
  val DirectPurchaseContractingAuthority: XMLPath =
    FormSection.ContractAward.ContractingAuthority.Direct

  val DelegatedPurchaseContractingAuthority: XMLPath =
    FormSection.ContractAward.ContractingAuthority.Delegated

  val VeatPurchaseContractingAuthority: XMLPath =
    FormSection.Veat.ContractingAuthority.Veat

  // Award containers
  val AwardOfContract: XMLPath = FormSection.ContractAward.AwardOfContract
  val VeatAwardOfContact: XMLPath = FormSection.Veat.AwardOfContract

  // Award “Information” blocks
  val ContractAwardInfo: XMLPath = FormSection.ContractAward.AwardInfo
  val VeatAwardInfo: XMLPath = FormSection.Veat.AwardInfo

  // Justifications (roots)
  val ContractAwardJustification: XMLPath =
    FormSection.ContractAward.JustificationRoot
  val VeatAwardJustification: XMLPath = FormSection.Veat.JustificationRoot

  // ======================
  // Convenience fragments to mirror  parser leaves
  // (so call sites don’t need XMLPath("..") literals)
  // ======================

  // Contracting authority leaves
  val OrgOfficialName: XMLPath = XMLPath("ORGANISATION") / "OFFICIALNAME"
  val CountryValue: XMLPath = XMLPath("COUNTRY") attr "VALUE"

  // IDs
  val ContractNumber: XMLPath = XMLPath("CONTRACT_NUMBER")
  val LotNumber: XMLPath = XMLPath("LOT_NUMBER")

  // Titles / descriptions under *Info* blocks
  val TitleContract: XMLPath = XMLPath("TITLE_CONTRACT")
  val ShortContractDescription: XMLPath = XMLPath("SHORT_CONTRACT_DESCRIPTION")

  // Awarded supplier leaves (relative to AwardOfContract/VeatAwardOfContact children)
  object EconomicOperator {
    val Base = XMLPath(
      "ECONOMIC_OPERATOR_NAME_ADDRESS"
    ) / "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME"
    val OrganisationOfficialName = Base / "ORGANISATION" / "OFFICIALNAME"
    val CountryValue = (Base / "COUNTRY") attr "VALUE"
  }

  // Contract value subtree (relative to award children)
  object ContractValue {
    val Base = XMLPath(
      "CONTRACT_VALUE_INFORMATION"
    ) / "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE"
    val ValueCost = Base / "VALUE_COST"
    def Currency = Base attr "CURRENCY"
  }

  // Justification leaf used by parser (relative to justification roots)
  val JustificationReason: XMLPath = XMLPath("REASON_CONTRACT_LAWFUL")
}
