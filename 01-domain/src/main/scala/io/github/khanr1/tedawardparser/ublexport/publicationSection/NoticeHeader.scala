package io.github.khanr1.tedawardparser
package ublExport
package publicationSection

import io.github.khanr1.tedawardparser.tedExport.types.{Date, Language}
import io.github.khanr1.tedawardparser.ublExport.types.*

/** Administrative header from the UBL document root elements.
  * Equivalent to NoticeData + CodeIF in the TED export domain.
  */
final case class NoticeHeader(
    noticeUUID: NoticeUUID,               // cbc:ID schemeName="notice-id"
    contractFolderID: ContractFolderID,   // cbc:ContractFolderID
    issueDate: Date,                      // cbc:IssueDate
    ublVersionID: UBLVersionID,           // cbc:UBLVersionID e.g. "2.3"
    customizationID: CustomizationID,     // cbc:CustomizationID e.g. "eforms-sdk-1.12"
    versionID: VersionID,                 // cbc:VersionID e.g. "01"
    regulatoryDomain: RegulatoryDomain,   // cbc:RegulatoryDomain e.g. "32014L0024"
    noticeLanguageCode: Language,         // cbc:NoticeLanguageCode e.g. "FRA"
    noticeSubTypeCode: NoticeSubTypeCode  // efac:NoticeSubType/cbc:SubTypeCode e.g. "29"
)
