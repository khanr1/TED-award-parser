package io.github.khanr1.tedawardparser
package repository
package xpath

import io.github.khanr1.tedawardparser.repository.xml.XMLPath

trait FormSectionPath {
  val root: XMLPath
  trait F03Path:
    def contractingAutorityPath:ContractingAuthorityPath
    def objectContractPath: ObjectContractPath
    def procedurePath : ProcedurePath
    def awardOfcontractPath:AwardOfContractPath
    def ComplementaryInformationPath:ComplementaryInformationPath

    trait ContractingAuthorityPath:
        val root: XMLPath
        val officialNamePath :XMLPath
        val nationalIDPath :XMLPath
        val addressPath :XMLPath
        val townPath :XMLPath
        val postalCodePath :XMLPath
        val countryPath :XMLPath
        val pointOfContactPath :XMLPath
        val phonePath :XMLPath
        val emailPath :XMLPath

    trait ObjectContractPath:
        def awardNoticeInformationPath:AwardNoticeInformationPath
        def totalFinalValuePath:TotalFinalValuePath

        trait AwardNoticeInformationPath:
            val titlePath:XMLPath
            val locationPath:XMLPath
            val shorContractDescriptionPath:XMLPath

        trait TotalFinalValuePath:
            val currencyPath:XMLPath
            val valuePath:XMLPath
    trait ProcedurePath
    trait AwardOfContractPath:
        val root:XMLPath
        val contactNumberPath:XMLPath
        val lotNumberPath: XMLPath
        val contractTitlePath:XMLPath
        val numberOfOfferReceivedPath:XMLPath
        def awardedContractorPath:AwardedContractorPath
        def contractValueInformationPath:ContractValueInformationPath 

        trait AwardedContractorPath:
            val rootPath :XMLPath
            val officialnamePath :XMLPath
            val addressPath: XMLPath
            val townPath:XMLPath
            val postalCodePath:XMLPath
            val CountryPath: XMLPath

        trait ContractValueInformationPath



    trait ComplementaryInformationPath




}




