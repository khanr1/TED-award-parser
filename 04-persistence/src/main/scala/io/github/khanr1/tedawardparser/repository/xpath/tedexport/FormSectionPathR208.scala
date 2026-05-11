package io.github.khanr1.tedawardparser.repository
package xpath.tedexport

import io.github.khanr1.tedawardparser.repository.xml.XMLPath
import io.github.khanr1.tedawardparser.repository.xpath.tedexport.{
  ActivityAndPurchasingOnBehalfPath,
  AwardContractPath,
  ComplementaryInformationPath,
  ContractAwardObjectInformationPath,
  ContractingAuthorityPath,
  F03,
  F15,
  FormSectionPath
}

/** XPath constants for the TED R208 form-section structure.
  *
  * R208 is the older TED schema (pre-2014 EU directives). The XML uses verbose
  * element names and wraps awards in `FD_CONTRACT_AWARD` /
  * `FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE` containers.
  *
  * Sub-objects:
  *   - [[F03]] — Contract Award Notice (Form F03)
  *   - [[F15]] — Voluntary Ex Ante Transparency Notice (Form F15)
  *   - [[F02]] — Contract Notice (Form F02, used as a reference form only)
  */
object FormSectionPathR208 extends FormSectionPath {

  override val root: XMLPath = XMLPath("FORM_SECTION")

  object F03 extends F03 {
    override val root: XMLPath =
      FormSectionPathR208.root / "CONTRACT_AWARD" / "FD_CONTRACT_AWARD"

    object ContractingAuthorityPath extends ContractingAuthorityPath {

      override val root: XMLPath =
        F03.root / "CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD" / "NAME_ADDRESSES_CONTACT_CONTRACT_AWARD" / "CA_CE_CONCESSIONAIRE_PROFILE"
      override val officialNamePath: XMLPath =
        root / "ORGANISATION" / "OFFICIALNAME"
      override val nationalIDPath: XMLPath =
        root / "ORGANISATION" / "NATIONALID"
      override val addressPath: XMLPath = root / "ADDRESS"
      override val townPath: XMLPath = root / "TOWN"
      override val postalCodePath: XMLPath = root / "POSTAL_CODE"
      override val countryPath: XMLPath = root / "COUNTRY" attr ("VALUE")
      override val pointOfContactPath: XMLPath = root / "CONTACT_POINT"
      override val phonePath: XMLPath = root / "PHONE"
      override val emailPath: XMLPath = root / "E_MAILS" / "E_MAIL"

      object ActivityAndPurchasingOnBehalfPath
          extends ActivityAndPurchasingOnBehalfPath {

        override val root: XMLPath =
          F03.root / "CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD" / "TYPE_AND_ACTIVITIES_AND_PURCHASING_ON_BEHALF"

        override val contractingAuthorityTypePath =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_CONTRACTING_AUTHORITY" attr ("VALUE")
        override val contractingAuthorityTypePath2 =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_CONTRACTING_AUTHORITY_OTHER" attr ("VALUE")

        override val contractingAuthorityActivityPath =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_ACTIVITY" attr ("VALUE")
        override val contractingAuthorityActivityPath2 =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_ACTIVITY_OTHER" attr ("VALUE")

        override val pathToInfo =
          root / "PURCHASING_ON_BEHALF" / "PURCHASING_ON_BEHALF_YES"

        private val onBehalf =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY"

        override val officialNamePath: XMLPath =
          onBehalf / "ORGANISATION" / "OFFICIALNAME"
        override val nationalIDPath: XMLPath =
          onBehalf / "ORGANISATION" / "NATIONALID"
        override val addressPath: XMLPath = onBehalf / "ADDRESS"
        override val townPath: XMLPath = onBehalf / "TOWN"
        override val postalCodePath: XMLPath = onBehalf / "POSTAL_CODE"
        override val countryPath: XMLPath = onBehalf / "COUNTRY" attr ("VALUE")
        override val pointOfContactPath: XMLPath = onBehalf / "CONTACT_POINT"
        override val phonePath: XMLPath = onBehalf / "PHONE"
        override val emailPath: XMLPath = onBehalf / "E_MAILS" / "E_MAIL"
      }

    }

    object ContractAwardObjectInformationPath
        extends ContractAwardObjectInformationPath {

      override val root: XMLPath =
        F03.root / "OBJECT_CONTRACT_INFORMATION_CONTRACT_AWARD_NOTICE"

      override val titlePath: XMLPath =
        root / "DESCRIPTION_AWARD_NOTICE_INFORMATION" / "TITLE_CONTRACT"

      override val descriptionPath: XMLPath =
        root / "DESCRIPTION_AWARD_NOTICE_INFORMATION" / "SHORT_CONTRACT_DESCRIPTION"

      override val valuePath: XMLPath =
        root / "TOTAL_FINAL_VALUE" / "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE" / "VALUE_COST"

      override val currencyPath: XMLPath =
        root / "TOTAL_FINAL_VALUE" / "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE" attr ("CURRENCY")

    }

    // Path relative to a single AWARD_OF_CONTRACT element.
    // The assembler extracts each AWARD_OF_CONTRACT child and calls the decoder on it.
    object AwardContractPath extends AwardContractPath {

      override val root: XMLPath = XMLPath("AWARD_OF_CONTRACT")

      override val contractNumberPath: XMLPath = root / "CONTRACT_NUMBER"
      override val contractTitlePath: XMLPath = root / "CONTRACT_TITLE" / "P"
      override val lotNumberPath: XMLPath = root / "LOT_NUMBER"
      override val awardDatePath: XMLPath = root / "CONTRACT_AWARD_DATE"

      private val contractorBase: XMLPath =
        root / "ECONOMIC_OPERATOR_NAME_ADDRESS" / "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME"

      override val contractorNamePath: XMLPath =
        contractorBase / "ORGANISATION" / "OFFICIALNAME"
      override val contractorNationalIDPath: XMLPath =
        contractorBase / "ORGANISATION" / "NATIONALID"
      override val contractorAddressPath: XMLPath = contractorBase / "ADDRESS"
      override val contractorTownPath: XMLPath = contractorBase / "TOWN"
      override val contractorPostalCodePath: XMLPath =
        contractorBase / "POSTAL_CODE"
      override val contractorCountryPath: XMLPath =
        contractorBase / "COUNTRY" attr ("VALUE")
      override val contractorPointOfContactPath: XMLPath =
        contractorBase / "CONTACT_POINT"
      override val contractorPhonePath: XMLPath = contractorBase / "PHONE"
      override val contractorEmailPath: XMLPath =
        contractorBase / "E_MAILS" / "E_MAIL"

      override val contractValueCurrencyPath: XMLPath =
        root / "CONTRACT_VALUE_INFORMATION" / "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE"
      override val contractValueAmountPath: XMLPath =
        contractValueCurrencyPath / "VALUE_COST"

    }

    object ComplementaryInformationPath extends ComplementaryInformationPath {

      override val root: XMLPath =
        F03.root / "COMPLEMENTARY_INFORMATION_CONTRACT_AWARD"

      override val additionalInformationPath: XMLPath =
        root / "ADDITIONAL_INFORMATION"

      override val relatesToEuProjectYesPath: XMLPath =
        root / "RELATES_TO_EU_PROJECT_YES" / "P"

      override val noticeDispatchDatePath: XMLPath =
        root / "NOTICE_DISPATCH_DATE"

      override val appealBodyNamePath: XMLPath =
        root / "PROCEDURES_FOR_APPEAL" / "LODGING_INFORMATION_FOR_SERVICE" /
          "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME" / "ORGANISATION" / "OFFICIALNAME"

      override val justificationPath: XMLPath = XMLPath("")
    }

  }
  object F02 {

    val root: XMLPath =
      FormSectionPathR208.root / "CONTRACT" / "FD_CONTRACT"

    object ContractingAuthorityPath extends ContractingAuthorityPath {

      override val root: XMLPath =
        F02.root / "CONTRACTING_AUTHORITY_INFORMATION" / "NAME_ADDRESSES_CONTACT_CONTRACT" / "CA_CE_CONCESSIONAIRE_PROFILE"

      override val officialNamePath: XMLPath =
        root / "ORGANISATION" / "OFFICIALNAME"
      override val nationalIDPath: XMLPath =
        root / "ORGANISATION" / "NATIONALID"
      override val addressPath: XMLPath = root / "ADDRESS"
      override val townPath: XMLPath = root / "TOWN"
      override val postalCodePath: XMLPath = root / "POSTAL_CODE"
      override val countryPath: XMLPath = root / "COUNTRY" attr ("VALUE")
      override val pointOfContactPath: XMLPath = root / "CONTACT_POINT"
      override val phonePath: XMLPath = root / "PHONE"
      override val emailPath: XMLPath = root / "E_MAILS" / "E_MAIL"
    }

    object ContractAwardObjectInformationPath
        extends ContractAwardObjectInformationPath {

      private val descRoot: XMLPath =
        F02.root / "OBJECT_CONTRACT_INFORMATION" / "DESCRIPTION_CONTRACT_INFORMATION"

      override val root: XMLPath = descRoot
      override val titlePath: XMLPath = descRoot / "TITLE_CONTRACT" / "P"
      override val descriptionPath: XMLPath =
        descRoot / "SHORT_CONTRACT_DESCRIPTION"
      override val valuePath: XMLPath = XMLPath("")
      override val currencyPath: XMLPath = XMLPath("")
    }
  }

  object F15 extends F15 {

    override val root: XMLPath =
      FormSectionPathR208.root / "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE" / "FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE"

    object AwardContractPath extends AwardContractPath {

      override val root: XMLPath = XMLPath("AWARD_OF_CONTRACT_DEFENCE")

      override val contractNumberPath: XMLPath = root / "CONTRACT_NUMBER"
      override val contractTitlePath: XMLPath = root / "CONTRACT_TITLE" / "P"
      override val lotNumberPath: XMLPath = root / "LOT_NUMBER"
      override val awardDatePath: XMLPath = root / "CONTRACT_AWARD_DATE"

      private val contractorBase: XMLPath =
        root / "ECONOMIC_OPERATOR_NAME_ADDRESS" / "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME"

      override val contractorNamePath: XMLPath =
        contractorBase / "ORGANISATION" / "OFFICIALNAME"
      override val contractorNationalIDPath: XMLPath =
        contractorBase / "ORGANISATION" / "NATIONALID"
      override val contractorAddressPath: XMLPath = contractorBase / "ADDRESS"
      override val contractorTownPath: XMLPath = contractorBase / "TOWN"
      override val contractorPostalCodePath: XMLPath =
        contractorBase / "POSTAL_CODE"
      override val contractorCountryPath: XMLPath =
        contractorBase / "COUNTRY" attr ("VALUE")
      override val contractorPointOfContactPath: XMLPath =
        contractorBase / "CONTACT_POINT"
      override val contractorPhonePath: XMLPath = contractorBase / "PHONE"
      override val contractorEmailPath: XMLPath =
        contractorBase / "E_MAILS" / "E_MAIL"

      override val contractValueCurrencyPath: XMLPath =
        root / "CONTRACT_VALUE_INFORMATION" / "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE"
      override val contractValueAmountPath: XMLPath =
        contractValueCurrencyPath / "VALUE_COST"
    }

    object ContractingAuthorityPath extends ContractingAuthorityPath {

      override val root: XMLPath =
        F15.root / "CONTRACTING_AUTHORITY_VEAT" / "NAME_ADDRESSES_CONTACT_VEAT" / "CA_CE_CONCESSIONAIRE_PROFILE"

      override val officialNamePath: XMLPath =
        root / "ORGANISATION" / "OFFICIALNAME"
      override val nationalIDPath: XMLPath =
        root / "ORGANISATION" / "NATIONALID"
      override val addressPath: XMLPath = root / "ADDRESS"
      override val townPath: XMLPath = root / "TOWN"
      override val postalCodePath: XMLPath = root / "POSTAL_CODE"
      override val countryPath: XMLPath = root / "COUNTRY" attr ("VALUE")
      override val pointOfContactPath: XMLPath = root / "CONTACT_POINT"
      override val phonePath: XMLPath = root / "PHONE"
      override val emailPath: XMLPath = root / "E_MAILS" / "E_MAIL"

      object ActivityAndPurchasingOnBehalfPath
          extends ActivityAndPurchasingOnBehalfPath {

        override val root: XMLPath =
          F15.root / "CONTRACTING_AUTHORITY_VEAT" / "TYPE_AND_ACTIVITIES_OR_CONTRACTING_ENTITY_AND_PURCHASING_ON_BEHALF"

        override val contractingAuthorityTypePath =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_CONTRACTING_AUTHORITY" attr ("VALUE")
        override val contractingAuthorityTypePath2 =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_CONTRACTING_AUTHORITY_OTHER" attr ("VALUE")

        override val contractingAuthorityActivityPath =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_ACTIVITY" attr ("VALUE")
        override val contractingAuthorityActivityPath2 =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_ACTIVITY_OTHER" attr ("VALUE")

        override val pathToInfo =
          root / "PURCHASING_ON_BEHALF" / "PURCHASING_ON_BEHALF_YES"

        private val onBehalf =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY"

        override val officialNamePath: XMLPath =
          onBehalf / "ORGANISATION" / "OFFICIALNAME"
        override val nationalIDPath: XMLPath =
          onBehalf / "ORGANISATION" / "NATIONALID"
        override val addressPath: XMLPath = onBehalf / "ADDRESS"
        override val townPath: XMLPath = onBehalf / "TOWN"
        override val postalCodePath: XMLPath = onBehalf / "POSTAL_CODE"
        override val countryPath: XMLPath = onBehalf / "COUNTRY" attr ("VALUE")
        override val pointOfContactPath: XMLPath = onBehalf / "CONTACT_POINT"
        override val phonePath: XMLPath = onBehalf / "PHONE"
        override val emailPath: XMLPath = onBehalf / "E_MAILS" / "E_MAIL"
      }

    }

    object ContractAwardObjectInformationPath
        extends ContractAwardObjectInformationPath {

      override val root: XMLPath = F15.root / "OBJECT_VEAT"

      override val titlePath: XMLPath =
        root / "DESCRIPTION_VEAT" / "TITLE_CONTRACT"

      override val descriptionPath: XMLPath =
        root / "DESCRIPTION_VEAT" / "SHORT_CONTRACT_DESCRIPTION"

      override val valuePath: XMLPath =
        root / "TOTAL_FINAL_VALUE" / "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE" / "VALUE_COST"
      override val currencyPath =
        root / "TOTAL_FINAL_VALUE" / "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE" attr ("CURRENCY")
    }

    object ComplementaryInformationPath extends ComplementaryInformationPath {

      override val root: XMLPath =
        F15.root / "COMPLEMENTARY_INFORMATION_VEAT"

      override val additionalInformationPath: XMLPath =
        root / "ADDITIONAL_INFORMATION"

      override val relatesToEuProjectYesPath: XMLPath =
        root / "RELATES_TO_EU_PROJECT_YES" / "P"

      override val noticeDispatchDatePath: XMLPath =
        root / "NOTICE_DISPATCH_DATE"

      override val appealBodyNamePath: XMLPath =
        root / "PROCEDURES_FOR_APPEAL" / "LODGING_INFORMATION_FOR_SERVICE" /
          "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME" / "ORGANISATION" / "OFFICIALNAME"

      override val justificationPath: XMLPath =
        F15.root / "PROCEDURE_DEFINITION_VEAT" / "TYPE_OF_PROCEDURE_DEF_F15" /
          "F15_PT_NEGOTIATED_WITHOUT_COMPETITION" / "ANNEX_D_F15" / "ANNEX_D1" /
          "REASON_CONTRACT_LAWFUL"
    }

  }

}
