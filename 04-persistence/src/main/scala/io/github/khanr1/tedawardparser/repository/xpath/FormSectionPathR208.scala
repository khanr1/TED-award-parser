package io.github.khanr1.tedawardparser.repository
package xpath

import io.github.khanr1.tedawardparser.repository.xml.XMLPath

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

        override val ContractingAuthorityActivityPath =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_ACTIVITY" attr ("VALUE")
        override val ContractingAuthorityActivityPath2 =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_ACTIVITY_OTHER" attr ("VALUE")

        override val pathToInfo =
          root / "PURCHASING_ON_BEHALF" / "PURCHASING_ON_BEHALF_YES"

        override val officialNamePath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "ORGANISATION" / "OFFICIALNAME"
        override val nationalIDPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "ORGANISATION" / "NATIONALID"
        override val addressPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "ADDRESS"
        override val townPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "TOWN"
        override val postalCodePath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "POSTAL_CODE"
        override val countryPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "COUNTRY" attr ("VALUE")
        override val pointOfContactPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "CONTACT_POINT"
        override val phonePath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "PHONE"
        override val emailPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "E_MAILS" / "E_MAIL"
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

    object AwardContractPath extends AwardContractPath {

      override val root: XMLPath = F03.root / "AWARD_OF_CONTRACT"

      override val contractNumberPath: XMLPath = root / "CONTRACT_NUMBER"

      override val contractTitlePath: XMLPath = root / "CONTRACT_TITLE"

      override val lotNumberPath: XMLPath = root / "LOT_NUMBER"

      override val awardDatePath: XMLPath = root / "CONTRACT_AWARD_DATE"

      override val contractorPath: XMLPath =
        root / "ECONOMIC_OPERATOR_NAME_ADDRESS" / "CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME"

      override val contractValueCurrencyPath: XMLPath =
        root / "CONTRACT_VALUE_INFORMATION" / "COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE"
      override val contractValueAmountPath: XMLPath =
        contractValueCurrencyPath / "VALUE_COST"

    }
  }
  object F15 extends F15 {

    override val root: XMLPath =
      FormSectionPathR208.root / "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE" / "FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE"

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

        override val ContractingAuthorityActivityPath =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_ACTIVITY" attr ("VALUE")
        override val ContractingAuthorityActivityPath2 =
          root / "TYPE_AND_ACTIVITIES" / "TYPE_OF_ACTIVITY_OTHER" attr ("VALUE")

        override val pathToInfo =
          root / "PURCHASING_ON_BEHALF" / "PURCHASING_ON_BEHALF_YES"

        override val officialNamePath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "ORGANISATION" / "OFFICIALNAME"
        override val nationalIDPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "ORGANISATION" / "NATIONALID"
        override val addressPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "ADDRESS"
        override val townPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "TOWN"
        override val postalCodePath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "POSTAL_CODE"
        override val countryPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "COUNTRY" attr ("VALUE")
        override val pointOfContactPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "CONTACT_POINT"
        override val phonePath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "PHONE"
        override val emailPath: XMLPath =
          pathToInfo / "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" / "E_MAILS" / "E_MAIL"
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

  }

}
