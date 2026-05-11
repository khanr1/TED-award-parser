package io.github.khanr1.tedawardparser
package repository
package xpath.tedexport

import io.github.khanr1.tedawardparser.repository.xml.XMLPath

/** XPath-style path constants for the `CODED_DATA_SECTION` block.
  *
  * The coded-data section is form-format independent and appears in every TED
  * notice. Sub-objects group the three logical blocks:
  *   - [[CodeIFPath]] — coded indicator fields (`CODIF_DATA`)
  *   - [[NoticeDataPath]] — notice metadata (`NOTICE_DATA`)
  *   - [[OfficialJournalReferencePath]] — OJ reference (`REF_OJS`)
  */
object CodedDataSectionPath {
  val root: XMLPath = XMLPath("CODED_DATA_SECTION")

  /** Paths within `CODIF_DATA` — coded classification fields. */
  object CodeIFPath {
    val root: XMLPath = CodedDataSectionPath.root / "CODIF_DATA"
    val dispatchDatePath = root / "DS_DATE_DISPATCH"
    val authorityTypePath = root / "AA_AUTHORITY_TYPE"
    val documentTypePath = root / "TD_DOCUMENT_TYPE"
    val contractNaturePath = root / "NC_CONTRACT_NATURE"
    val procedureTypePath = root / "PR_PROC"
    val regulationScopePath = root / "RP_REGULATION"
    val bidTypePath = root / "TY_TYPE_BID"
    val awardCriterionPath = root / "AC_AWARD_CRIT"
    val mainActivityPath = root / "MA_MAIN_ACTIVITIES"
    val headingCodePath = root / "HEADING"
    val initiatorCodePath = root / "INITIATOR"
    val directivePath = root / "DIRECTIVE" attr ("VALUE")
  }

  /** Paths within `NOTICE_DATA` — document URIs, buyer info, CPV codes, values.
    */
  object NoticeDataPath {
    val root: XMLPath = CodedDataSectionPath.root / "NOTICE_DATA"
    val ojsNumberPath = root / "NO_DOC_OJS"
    val documentURIPath = root / "URI_LIST"
    val languagePath = root / "LG_ORIG"
    val buyerCountryPath = root / "ISO_COUNTRY" attr ("VALUE")
    val buyerURLPath = root / "IA_URL_GENERAL"
    val CPVPath = root / "ORIGINAL_CPV"
    val valuePath = root / "VALUES_LIST" / "VALUES"

    /** Relative path from a `VALUES` child element to the monetary amount. */
    val amountPath = XMLPath("SINGLE_VALUE") / "VALUE"
    val currencyPath = amountPath attr ("CURRENCY")
    val refNoticePath = root / "REF_NOTICE" / "NO_DOC_OJS"
  }

  /** Paths within `REF_OJS` — Official Journal publication reference. */
  object OfficialJournalReferencePath {
    val root: XMLPath = CodedDataSectionPath.root / "REF_OJS"
    val journalSeriesPath = root / "COLL_OJ"
    val journalNumberPath = root / "NO_OJ"
    val publicationDatePath = root / "DATE_PUB"
  }
}
