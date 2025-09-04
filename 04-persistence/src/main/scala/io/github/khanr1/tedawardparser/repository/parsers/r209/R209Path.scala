package io.github.khanr1.tedawardparser
package repository
package parsers.r209
import xmlPath.XMLPath

object R209Path {

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
  // FORM_SECTION branches (F03 / F15)
  // ======================
  object FormSection {

    // -------- F03: Contract Award --------
    object ContractAward {
      val Root = Form / "F03_2014"

      object ContractingAuthority {
        val Direct = Root / "CONTRACTING_BODY" / "ADDRESS_CONTRACTING_BODY"
        // (No separate delegated node in your parser for R209)
      }

      // Repeating award container
      val AwardOfContract = Root / "AWARD_CONTRACT"

      // “Object” info block
      val AwardInfo = Root / "OBJECT_CONTRACT"

      // Procedure
      val Procedure = Root / "PROCEDURE"

      // Justification root (kept for parity with R208, though R209 parser uses Procedure+leaf)
      val JustificationRoot =
        Root / "PROCEDURE_DEFINITION_CONTRACT_AWARD_NOTICE" /
          "TYPE_OF_PROCEDURE_DEF" /
          "F03_AWARD_WITHOUT_PRIOR_PUBLICATION" /
          "ANNEX_D"

      // --- Leaves used by your parser under awards/info ---
      // Contract / Lot identifiers
      val ContractNo = XMLPath("CONTRACT_NO")
      val LotNo = XMLPath("LOT_NO")
      // Some notices put an ITEM attribute on the award container
      def ItemAttr(p: XMLPath) =
        p.attr("ITEM") // used via parent.attr("ITEM") in code

      // Titles (used with fallback sections)
      val Title = XMLPath("TITLE")

      // Descriptions
      val ObjectDescr = XMLPath("OBJECT_DESCR")
      val ShortDescr = XMLPath("SHORT_DESCR")

      // Awarded supplier (primary & fallback container chains)
      object EconomicOperator {
        // Primary chain: .../AWARDED_CONTRACT/CONTRACTORS/CONTRACTOR/ADDRESS_CONTRACTOR
        val PrimaryBase =
          XMLPath(
            "AWARDED_CONTRACT"
          ) / "CONTRACTORS" / "CONTRACTOR" / "ADDRESS_CONTRACTOR"
        // Fallback chain: .../AWARDED_CONTRACT/CONTRACTOR/ADDRESS_CONTRACTOR
        val FallbackBase =
          XMLPath("AWARDED_CONTRACT") / "CONTRACTOR" / "ADDRESS_CONTRACTOR"

        val OfficialNameLeaf = XMLPath("OFFICIALNAME")
        val CountryLeaf = XMLPath("COUNTRY")
        def CountryValue = CountryLeaf.attr("VALUE")
      }

      // Contract value subtree (primary used in awards, fallback in info)
      object ContractValue {
        // Primary (under Award): .../AWARDED_CONTRACT/VALUES/VAL_TOTAL
        val PrimaryValueTotal =
          XMLPath("AWARDED_CONTRACT") / "VALUES" / "VAL_TOTAL"
        // Fallback (under info): .../VAL_TOTAL
        val FallbackValueTotal = XMLPath("VAL_TOTAL")

        def PrimaryCurrencyAttr = PrimaryValueTotal.attr("CURRENCY")
        def FallbackCurrencyAttr = FallbackValueTotal.attr("CURRENCY")
      }

      // Procedure → Directive → negotiated-without-publication → justification leaf
      object ProcedureDirective {
        val Directive24EU =
          XMLPath("DIRECTIVE_2014_24_EU")
        val PtNegotiatedWithoutPub =
          Directive24EU / "PT_NEGOTIATED_WITHOUT_PUBLICATION"
        val JustificationLeaf =
          PtNegotiatedWithoutPub / "D_JUSTIFICATION"
      }
    }

    // -------- F15: VEAT --------
    object Veat {
      val Root = Form / "F15_2014"

      object ContractingAuthority {
        val Direct = Root / "CONTRACTING_BODY" / "ADDRESS_CONTRACTING_BODY"
      }

      val AwardOfContract = Root / "AWARD_CONTRACT"
      val AwardInfo = Root / "OBJECT_CONTRACT"
      val Procedure = Root / "PROCEDURE"

      val JustificationRoot =
        Root / "PROCEDURE_DEFINITION_VEAT" /
          "TYPE_OF_PROCEDURE_DEF_F15" /
          "F15_PT_NEGOTIATED_WITHOUT_COMPETITION" /
          "ANNEX_D_F15" /
          "ANNEX_D1"

      // Leaves mirrored from F03 so call sites can use the same constants
      val ContractNo = XMLPath("CONTRACT_NO")
      val LotNo = XMLPath("LOT_NO")
      def ItemAttr(p: XMLPath) = p.attr("ITEM")
      val Title = XMLPath("TITLE")
      val ObjectDescr = XMLPath("OBJECT_DESCR")
      val ShortDescr = XMLPath("SHORT_DESCR")

      object EconomicOperator {
        val PrimaryBase =
          XMLPath(
            "AWARDED_CONTRACT"
          ) / "CONTRACTORS" / "CONTRACTOR" / "ADDRESS_CONTRACTOR"
        val FallbackBase =
          XMLPath("AWARDED_CONTRACT") / "CONTRACTOR" / "ADDRESS_CONTRACTOR"

        val OfficialNameLeaf = XMLPath("OFFICIALNAME")
        val CountryLeaf = XMLPath("COUNTRY")
        def CountryValue = CountryLeaf.attr("VALUE")
      }

      object ContractValue {
        val PrimaryValueTotal =
          XMLPath("AWARDED_CONTRACT") / "VALUES" / "VAL_TOTAL"
        val FallbackValueTotal = XMLPath("VAL_TOTAL")
        def PrimaryCurrencyAttr = PrimaryValueTotal.attr("CURRENCY")
        def FallbackCurrencyAttr = FallbackValueTotal.attr("CURRENCY")
      }

      object ProcedureDirective {
        val Directive24EU = XMLPath("DIRECTIVE_2014_24_EU")
        val PtNegotiatedWithoutPub =
          Directive24EU / "PT_NEGOTIATED_WITHOUT_PUBLICATION"
        val JustificationLeaf = PtNegotiatedWithoutPub / "D_JUSTIFICATION"
      }
    }
  }

  // ======================
  // Compatibility aliases (preserve existing names)
  // ======================

  // Common
  val OjsID: XMLPath = CodedDataSection.OjsID
  val PublicationDate: XMLPath = CodedDataSection.PublicationDate

  // Roots
  val ContractAward: XMLPath = FormSection.ContractAward.Root
  val VEAT: XMLPath = FormSection.Veat.Root

  // Contracting Authority
  val DirectPurchaseContractingAuthority: XMLPath =
    FormSection.ContractAward.ContractingAuthority.Direct
  val VeatPurchaseContractingAuthority: XMLPath =
    FormSection.Veat.ContractingAuthority.Direct

  // Award containers
  val AwardOfContract: XMLPath = FormSection.ContractAward.AwardOfContract
  val VeatAwardOfContact: XMLPath = FormSection.Veat.AwardOfContract

  // Object/Info sections
  val ContractAwardInfo: XMLPath = FormSection.ContractAward.AwardInfo
  val VeatAwardInfo: XMLPath = FormSection.Veat.AwardInfo

  // Procedure
  val ContractAwardProcedure: XMLPath = FormSection.ContractAward.Procedure
  val VeatAwardProcedure: XMLPath = FormSection.Veat.Procedure

  // Justifications (legacy roots kept; parser uses ProcedureDirective leaf)
  val ContractAwardJustification: XMLPath =
    FormSection.ContractAward.JustificationRoot
  val VeatAwardJustification: XMLPath = FormSection.Veat.JustificationRoot

  // ======================
  // Convenience leaves (shared names you use in the parser)
  // ======================

  // IDs
  val ContractNo: XMLPath = XMLPath("CONTRACT_NO")
  val LotNo: XMLPath = XMLPath("LOT_NO")
  def ItemAttr(p: XMLPath): XMLPath =
    p.attr("ITEM") // used via parent.attr("ITEM")

  // Titles / descriptions
  val Title: XMLPath = XMLPath("TITLE")
  val ObjectDescr: XMLPath = XMLPath("OBJECT_DESCR")
  val ShortDescr: XMLPath = XMLPath("SHORT_DESCR")

  // Awarded supplier leaves (relative to the chosen EO base)
  object EconomicOperator {
    val OfficialNameLeaf: XMLPath = XMLPath("OFFICIALNAME")
    val CountryLeaf: XMLPath = XMLPath("COUNTRY")
    def CountryValue: XMLPath = CountryLeaf.attr("VALUE")
  }

  // Contract value leaves (used by extractManyWithFallback)
  object ContractValue {
    val PrimaryValueTotal: XMLPath =
      XMLPath("AWARDED_CONTRACT") / "VALUES" / "VAL_TOTAL"
    val FallbackValueTotal: XMLPath = XMLPath("VAL_TOTAL")
    def PrimaryCurrencyAttr: XMLPath = PrimaryValueTotal.attr("CURRENCY")
    def FallbackCurrencyAttr: XMLPath = FallbackValueTotal.attr("CURRENCY")
  }

  // Procedure → directive justification leaf (relative to ContractAwardProcedure/VeatAwardProcedure)
  object ProcedureDirective {
    val Directive24EU: XMLPath = XMLPath("DIRECTIVE_2014_24_EU")
    val PtNegotiatedWithoutPub: XMLPath =
      Directive24EU / "PT_NEGOTIATED_WITHOUT_PUBLICATION"
    val JustificationLeaf: XMLPath = PtNegotiatedWithoutPub / "D_JUSTIFICATION"
  }
}
