package io.github.khanr1.tedawardparser
package repository
package xpath

import io.github.khanr1.tedawardparser.repository.xml.XMLPath
import io.github.khanr1.tedawardparser.tedExport.formSection.ActivityAndPurchasingOnBehalf

trait FormSectionPath:
    val root:XMLPath

trait F03:
    val root:XMLPath
    def ContractingAuthorityPath:ContractingAuthorityPath

trait F15:
    val root: XMLPath
    def ContractingAuthorityPath:ContractingAuthorityPath

trait ContractingAuthorityPath:
    val root :XMLPath 
    val officialNamePath :XMLPath
    val nationalIDPath :XMLPath
    val addressPath :XMLPath
    val townPath :XMLPath
    val postalCodePath :XMLPath
    val countryPath :XMLPath
    val pointOfContactPath :XMLPath
    val phonePath :XMLPath
    val emailPath :XMLPath

trait ActivityAndPurchasingOnBehalfPath extends ContractingAuthorityPath:
    val contractingAuthorityTypePath : XMLPath
    val contractingAuthorityTypePath2 : XMLPath
    val ContractingAuthorityActivityPath: XMLPath
    val ContractingAuthorityActivityPath2: XMLPath
    val pathToInfo: XMLPath

trait ContractAwardObjectInformationPath:
    val root : XMLPath
    val titlePath: XMLPath
    val descriptionPath: XMLPath
    val valuePath:XMLPath
    val currencyPath: XMLPath


