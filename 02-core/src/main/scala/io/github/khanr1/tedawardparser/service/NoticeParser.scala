package io.github.khanr1.tedawardparser
package service

import scala.xml.Elem
import fs2.Pipe
import io.github.khanr1.tedawardparser.NoticeType.toDomain

trait NoticeParser[F[_], A] {
  def parseStream: Pipe[F, A, Notice]
}
object NoticeParser:

  def TedExportParser[F[_]]: NoticeParser[F, Elem] =
    /** Parses an XML document by locating a specific parent section element,
      * finding the first child element within that section that matches a known
      * set of tags, and applying the corresponding handler function to produce
      * a result.
      *
      * This is useful for scenarios where the XML has a known parent section
      * (e.g., `<FORM_SECTION>`) containing one of several mutually exclusive
      * child tags (e.g., `<F03_2014>`, `<F02_2014>`, etc.), and you want to
      * dispatch to the correct parsing logic based on which child is present.
      *
      * If the section element is missing, or no matching child tag is found,
      * the provided `default` value is returned.
      *
      * @param xml
      *   The root XML element to parse.
      * @param sectionTag
      *   The name of the section element to search for (e.g., "FORM_SECTION").
      * @param handlers
      *   A map from expected child tag names to handler functions. Each handler
      *   takes the matching child element and produces a result.
      * @param default
      *   A default value to return if the section is not found, or if no known
      *   child tag is present within the section.
      * @tparam A
      *   The type of the result produced by the handler functions.
      * @return
      *   The result of applying the appropriate handler to the matching child
      *   element, or the default value if no match is found.
      */
    def parseSection[A](
        xml: Elem,
        sectionTag: String,
        handler: Map[String, Elem => A],
        default: => A
    ): A = {
      (for
        section <- (xml \\ sectionTag).headOption
        field <- section.child.collectFirst {
          case e: Elem if handler.contains(e.label) => e
        }
      yield handler(field.label)(field)).getOrElse(default)
    }

    def parseNoticeNumber(xml: Elem): NoticeNumber =
      NoticeNumber(
        (xml \ "CODED_DATA_SECTION" \ "NOTICE_DATA" \ "NO_DOC_OJS").text
      )
    def parseNoticePublicationDate(xml: Elem): PublicationDate =
      PublicationDate(
        (xml \ "CODED_DATA_SECTION" \ "REF_OJS" \ "DATE_PUB").text
      )
    def parseNoticetype(xml: Elem): NoticeType =
      NoticeType.toDomain(
        (xml \ "CODED_DATA_SECTION" \ "CODIF_DATA" \ "TD_DOCUMENT_TYPE").text
      )
    def parseProcurementProcess(xml: Elem): ProcurementProcess =
      ProcurementProcess.toDomain(
        (xml \ "CODED_DATA_SECTION" \ "CODIF_DATA" \ "PR_PROC").text
      )
    def parseContractingBodyName(xml: Elem): ContractingBodyName = {

      def parseF(elem: Elem) =
        ContractingBodyName(
          (elem \\ "ADDRESS_CONTRACTING_BODY" \ "OFFICIALNAME").text.trim
        )
      def parseVEAT(elem: Elem) =
        ContractingBodyName(
          (elem \\ "ORGANISATION" \ "OFFICIALNAME").text.trim
        )

      def parseContractAward(elem: Elem) =
        if (
          (elem \\ "PURCHASING_ON_BEHALF" \ "PURCHASING_ON_BEHALF_YES").nonEmpty
        ) {
          ContractingBodyName(
            (
              elem \\ "PURCHASING_ON_BEHALF" \ "PURCHASING_ON_BEHALF_YES" \ "CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY" \ "ORGANISATION" \ "OFFICIALNAME"
            ).text
          )
        } else
          ContractingBodyName(
            (
              elem \\ "CA_CE_CONCESSIONAIRE_PROFILE" \ "ORGANISATION" \ "OFFICIALNAME"
            ).text
          )

      def contractingHandler: Map[String, Elem => ContractingBodyName] = Map(
        "F01_2014" -> parseF,
        "F02_2014" -> parseF,
        "F03_2014" -> parseF,
        "F15_2014" -> parseF,
        "F20_2014" -> parseF,
        "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE" -> parseVEAT,
        "CONTRACT_AWARD" -> parseContractAward
      )

      parseSection(
        xml,
        "FORM_SECTION",
        contractingHandler,
        ContractingBodyName(" Form not Found")
      )
    }

    def parseContractingBodyCountry(xml: Elem): Country = {
      def parseF(elem: Elem): Country = Country.toDomain(
        (elem \\ "CONTRACTING_BODY" \ "ADDRESS_CONTRACTING_BODY" \ "COUNTRY" \ "@VALUE").text
      )
      def parseContractAward(elem: Elem): Country = Country.toDomain(
        (elem \\ "ORGANISATION" \ "COUNTRY" \ "@VALUE").text
      )

      def parseVEAT(elem: Elem): Country = Country.toDomain(
        (elem \\ "ORGANISATION" \ "COUNTRY" \ "@VALUE").text
      )

      def countryHandler: Map[String, Elem => Country] = Map(
        "F01_2014" -> parseF,
        "F02_2014" -> parseF,
        "F03_2014" -> parseF,
        "F15_2014" -> parseF,
        "F20_2014" -> parseF,
        "VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE" -> parseVEAT,
        "CONTRACT_AWARD" -> parseContractAward
      )

      parseSection(xml, "FORM_SECTION", countryHandler, Country.Unknown)

    }

    new NoticeParser[F, Elem] {

      override def parseStream: Pipe[F, Elem, Notice] = s =>
        s.map(elem =>
          Notice(
            parseNoticeNumber(elem),
            parseNoticePublicationDate(elem),
            parseNoticetype(elem),
            parseProcurementProcess(elem),
            parseContractingBodyName(elem),
            parseContractingBodyCountry(elem)
          )
        )

    }
