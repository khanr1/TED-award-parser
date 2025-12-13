package io.github.khanr1.tedawardparser.repository
package xpath

import io.github.khanr1.tedawardparser.repository.xml.XMLPath

object FormSectionPathR209 extends FormSectionPath {

  override val root: XMLPath = XMLPath("FORM_SECTION")

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

        override val ContractingAuthorityActivityPath =
          root / "CA_ACTIVITY" attr ("VALUE")
        override val ContractingAuthorityActivityPath2 =
          root / "CA_ACTIVITY_OTHER" attr ("VALUE")

        override val pathToInfo = XMLPath("")

        override val officialNamePath: XMLPath =
          XMLPath("")
        override val nationalIDPath: XMLPath =
          XMLPath("")
        override val addressPath: XMLPath =
          XMLPath("")
        override val townPath: XMLPath =
          XMLPath("")
        override val postalCodePath: XMLPath = XMLPath("")
        override val countryPath: XMLPath = XMLPath("")
        override val pointOfContactPath: XMLPath = XMLPath("")
        override val phonePath: XMLPath = XMLPath("")
        override val emailPath: XMLPath = XMLPath("")
      }

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

        override val ContractingAuthorityActivityPath =
          root / "CA_ACTIVITY" attr ("VALUE")
        override val ContractingAuthorityActivityPath2 =
          root / "CA_ACTIVITY_OTHER" attr ("VALUE")

        override val pathToInfo = XMLPath("")

        override val officialNamePath: XMLPath =
          XMLPath("")
        override val nationalIDPath: XMLPath =
          XMLPath("")
        override val addressPath: XMLPath =
          XMLPath("")
        override val townPath: XMLPath =
          XMLPath("")
        override val postalCodePath: XMLPath = XMLPath("")
        override val countryPath: XMLPath = XMLPath("")
        override val pointOfContactPath: XMLPath = XMLPath("")
        override val phonePath: XMLPath = XMLPath("")
        override val emailPath: XMLPath = XMLPath("")
      }

    }

  }

}
