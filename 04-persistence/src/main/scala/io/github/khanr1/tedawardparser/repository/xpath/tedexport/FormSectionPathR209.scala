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
import io.github.khanr1.tedawardparser.repository.xpath.tedexport.FormSectionPathR208

/** XPath constants for the TED R209 form-section structure.
  *
  * R209 uses the 2014 EU directive XML schema. Element names are shorter and
  * follow a different hierarchy than [[FormSectionPathR208]]. Award elements
  * appear as `AWARD_CONTRACT` children directly under the form root.
  *
  * Sub-objects:
  *   - [[F03]] — Contract Award Notice (Form F03_2014)
  *   - [[F15]] — Voluntary Ex Ante Transparency Notice (Form F15_2014)
  *   - [[F02]] — Contract Notice (Form F02_2014, used as a reference form only)
  */
object FormSectionPathR209 extends FormSectionPath {

  override val root: XMLPath = XMLPath("FORM_SECTION")

  object F02 {

    val root: XMLPath = FormSectionPathR208.root / "F02_2014"

    object ContractingAuthorityPath extends ContractingAuthorityPath {

      override val root: XMLPath =
        F02.root / "CONTRACTING_BODY" / "ADDRESS_CONTRACTING_BODY"

      override val officialNamePath: XMLPath = root / "OFFICIALNAME"
      override val nationalIDPath: XMLPath = root / "NATIONALID"
      override val addressPath: XMLPath = root / "ADDRESS"
      override val townPath: XMLPath = root / "TOWN"
      override val postalCodePath: XMLPath = root / "POSTAL_CODE"
      override val countryPath: XMLPath = root / "COUNTRY" attr ("VALUE")
      override val pointOfContactPath: XMLPath = root / "CONTACT_POINT"
      override val phonePath: XMLPath = root / "PHONE"
      override val emailPath: XMLPath = root / "E_MAIL"
    }

    object ContractAwardObjectInformationPath
        extends ContractAwardObjectInformationPath {

      override val root: XMLPath = F02.root / "OBJECT_CONTRACT"
      override val titlePath: XMLPath = root / "TITLE" / "P"
      override val descriptionPath: XMLPath = root / "SHORT_DESCR" / "P"
      override val valuePath: XMLPath = XMLPath("")
      override val currencyPath: XMLPath = XMLPath("")
    }
  }

  object F03 extends F03 {
    override val root: XMLPath =
      FormSectionPathR208.root / "F03_2014"

    object ContractingAuthorityPath extends ContractingAuthorityPath {
      override val root: XMLPath =
        F03.root / "CONTRACTING_BODY" / "ADDRESS_CONTRACTING_BODY"
      override val officialNamePath: XMLPath =
        root / "OFFICIALNAME"
      override val nationalIDPath: XMLPath =
        root / "NATIONALID"
      override val addressPath: XMLPath = root / "ADDRESS"
      override val townPath: XMLPath = root / "TOWN"
      override val postalCodePath: XMLPath = root / "POSTAL_CODE"
      override val countryPath: XMLPath = root / "COUNTRY" attr ("VALUE")
      override val pointOfContactPath: XMLPath = root / "CONTACT_POINT"
      override val phonePath: XMLPath = root / "PHONE"
      override val emailPath: XMLPath = root / "E_MAIL"

      object ActivityAndPurchasingOnBehalfPath
          extends ActivityAndPurchasingOnBehalfPath {

        override val root: XMLPath =
          F03.root

        override val contractingAuthorityTypePath =
          root / "CA_TYPE" attr ("VALUE")
        override val contractingAuthorityTypePath2 =
          root / "CA_TYPE_OTHER" attr ("VALUE")

        override val contractingAuthorityActivityPath =
          root / "CA_ACTIVITY" attr ("VALUE")
        override val contractingAuthorityActivityPath2 =
          root / "CA_ACTIVITY_OTHER" attr ("VALUE")

        // R209 F03 does not have a purchasing-on-behalf block
        override val pathToInfo = XMLPath("")
        override val officialNamePath = XMLPath("")
        override val nationalIDPath = XMLPath("")
        override val addressPath = XMLPath("")
        override val townPath = XMLPath("")
        override val postalCodePath = XMLPath("")
        override val countryPath = XMLPath("")
        override val pointOfContactPath = XMLPath("")
        override val phonePath = XMLPath("")
        override val emailPath = XMLPath("")
      }

    }

    object ContractAwardObjectInformationPath
        extends ContractAwardObjectInformationPath {

      override val root: XMLPath = F03.root / "OBJECT_CONTRACT"

      override val titlePath: XMLPath = root / "TITLE"

      override val descriptionPath: XMLPath =
        root / "SHORT_DESCR"

      override val valuePath: XMLPath = root / "VAL_TOTAL"

      override val currencyPath: XMLPath = root / "VAL_TOTAL" attr ("CURRENCY")
    }

    // Paths relative to a single AWARD_CONTRACT element (not the document root)
    object AwardContractPath extends AwardContractPath {

      override val root: XMLPath = XMLPath("AWARD_CONTRACT")

      override val contractNumberPath: XMLPath = root / "CONTRACT_NO"
      override val contractTitlePath: XMLPath = root / "TITLE" / "P"
      override val lotNumberPath: XMLPath = root / "LOT_NO"
      override val awardDatePath: XMLPath =
        root / "AWARDED_CONTRACT" / "DATE_CONCLUSION_CONTRACT"

      private val contractorBase: XMLPath =
        root / "AWARDED_CONTRACT" / "CONTRACTORS" / "CONTRACTOR" / "ADDRESS_CONTRACTOR"

      override val contractorNamePath: XMLPath = contractorBase / "OFFICIALNAME"
      override val contractorNationalIDPath: XMLPath =
        contractorBase / "NATIONALID"
      override val contractorAddressPath: XMLPath = contractorBase / "ADDRESS"
      override val contractorTownPath: XMLPath = contractorBase / "TOWN"
      override val contractorPostalCodePath: XMLPath =
        contractorBase / "POSTAL_CODE"
      override val contractorCountryPath: XMLPath =
        contractorBase / "COUNTRY" attr ("VALUE")
      override val contractorPointOfContactPath: XMLPath =
        contractorBase / "CONTACT_POINT"
      override val contractorPhonePath: XMLPath = contractorBase / "PHONE"
      override val contractorEmailPath: XMLPath = contractorBase / "E_MAIL"

      override val contractValueCurrencyPath: XMLPath =
        root / "AWARDED_CONTRACT" / "VALUES"
      override val contractValueAmountPath: XMLPath =
        root / "AWARDED_CONTRACT" / "VALUES" / "VAL_TOTAL"
    }

    object ComplementaryInformationPath extends ComplementaryInformationPath {

      override val root: XMLPath = F03.root / "COMPLEMENTARY_INFO"

      override val additionalInformationPath: XMLPath = root / "INFO_ADD"

      override val relatesToEuProjectYesPath: XMLPath = XMLPath("")

      override val noticeDispatchDatePath: XMLPath =
        root / "DATE_DISPATCH_NOTICE"

      override val appealBodyNamePath: XMLPath =
        root / "ADDRESS_REVIEW_BODY" / "OFFICIALNAME"

      override val justificationPath: XMLPath = XMLPath("")
    }
  }
  object F15 extends F15 {

    override val root: XMLPath =
      FormSectionPathR208.root / "F15_2014"

    object ContractingAuthorityPath extends ContractingAuthorityPath {

      override val root: XMLPath =
        F15.root / "CONTRACTING_BODY" / "ADDRESS_CONTRACTING_BODY"

      override val officialNamePath: XMLPath =
        root / "OFFICIALNAME"
      override val nationalIDPath: XMLPath =
        root / "NATIONALID"
      override val addressPath: XMLPath = root / "ADDRESS"
      override val townPath: XMLPath = root / "TOWN"
      override val postalCodePath: XMLPath = root / "POSTAL_CODE"
      override val countryPath: XMLPath = root / "COUNTRY" attr ("VALUE")
      override val pointOfContactPath: XMLPath = root / "CONTACT_POINT"
      override val phonePath: XMLPath = root / "PHONE"
      override val emailPath: XMLPath = root / "E_MAIL"

      object ActivityAndPurchasingOnBehalfPath
          extends ActivityAndPurchasingOnBehalfPath {

        override val root: XMLPath =
          F15.root

        override val contractingAuthorityTypePath =
          root / "CA_TYPE" attr ("VALUE")
        override val contractingAuthorityTypePath2 =
          root / "CA_TYPE_OTHER" attr ("VALUE")

        override val contractingAuthorityActivityPath =
          root / "CA_ACTIVITY" attr ("VALUE")
        override val contractingAuthorityActivityPath2 =
          root / "CA_ACTIVITY_OTHER" attr ("VALUE")

        // R209 F15 does not have a purchasing-on-behalf block
        override val pathToInfo = XMLPath("")
        override val officialNamePath = XMLPath("")
        override val nationalIDPath = XMLPath("")
        override val addressPath = XMLPath("")
        override val townPath = XMLPath("")
        override val postalCodePath = XMLPath("")
        override val countryPath = XMLPath("")
        override val pointOfContactPath = XMLPath("")
        override val phonePath = XMLPath("")
        override val emailPath = XMLPath("")
      }

    }

    object ContractAwardObjectInformationPath
        extends ContractAwardObjectInformationPath {

      override val root: XMLPath = F15.root / "OBJECT_CONTRACT"

      override val titlePath: XMLPath = root / "TITLE"

      override val descriptionPath: XMLPath = root / "SHORT_DESCR"

      override val valuePath: XMLPath = root / "VAL_TOTAL"

      override val currencyPath: XMLPath = root / "VAL_TOTAL" attr ("CURRENCY")
    }

    object AwardContractPath extends AwardContractPath {

      override val root: XMLPath = XMLPath("AWARD_CONTRACT")

      override val contractNumberPath: XMLPath = root / "CONTRACT_NO"
      override val contractTitlePath: XMLPath = root / "TITLE" / "P"
      override val lotNumberPath: XMLPath = root / "LOT_NO"
      override val awardDatePath: XMLPath =
        root / "AWARDED_CONTRACT" / "DATE_CONCLUSION_CONTRACT"

      private val contractorBase: XMLPath =
        root / "AWARDED_CONTRACT" / "CONTRACTORS" / "CONTRACTOR" / "ADDRESS_CONTRACTOR"

      override val contractorNamePath: XMLPath = contractorBase / "OFFICIALNAME"
      override val contractorNationalIDPath: XMLPath =
        contractorBase / "NATIONALID"
      override val contractorAddressPath: XMLPath = contractorBase / "ADDRESS"
      override val contractorTownPath: XMLPath = contractorBase / "TOWN"
      override val contractorPostalCodePath: XMLPath =
        contractorBase / "POSTAL_CODE"
      override val contractorCountryPath: XMLPath =
        contractorBase / "COUNTRY" attr ("VALUE")
      override val contractorPointOfContactPath: XMLPath =
        contractorBase / "CONTACT_POINT"
      override val contractorPhonePath: XMLPath = contractorBase / "PHONE"
      override val contractorEmailPath: XMLPath = contractorBase / "E_MAIL"

      // F15 files (e.g. 91924-2018.xml) place VAL_TOTAL directly under AWARDED_CONTRACT
      override val contractValueCurrencyPath: XMLPath =
        root / "AWARDED_CONTRACT"
      override val contractValueAmountPath: XMLPath =
        root / "AWARDED_CONTRACT" / "VAL_TOTAL"
    }

    object ComplementaryInformationPath extends ComplementaryInformationPath {

      override val root: XMLPath = F15.root / "COMPLEMENTARY_INFO"

      override val additionalInformationPath: XMLPath = root / "INFO_ADD"

      override val relatesToEuProjectYesPath: XMLPath = XMLPath("")

      override val noticeDispatchDatePath: XMLPath =
        root / "DATE_DISPATCH_NOTICE"

      override val appealBodyNamePath: XMLPath =
        root / "ADDRESS_REVIEW_BODY" / "OFFICIALNAME"

      override val justificationPath: XMLPath =
        F15.root / "PROCEDURE" / "DIRECTIVE_2014_24_EU" /
          "PT_NEGOTIATED_WITHOUT_PUBLICATION" / "D_JUSTIFICATION"
    }
  }

}
