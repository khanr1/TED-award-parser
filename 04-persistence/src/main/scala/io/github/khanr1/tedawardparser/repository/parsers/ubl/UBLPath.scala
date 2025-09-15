package io.github.khanr1.tedawardparser
package repository
package parsers
package ubl

object UBLPath {
  // --- namespace bindings
  val ns = Ns(
    Map(
      "ext" -> "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2",
      "cbc" -> "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2",
      "cac" -> "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2",
      "efext" -> "http://data.europa.eu/p27/eforms-ubl-extensions/1",
      "efac" -> "http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1",
      "efbc" -> "http://data.europa.eu/p27/eforms-ubl-extension-basic-components/1"
    )
  )
  // ---------- Publication (OJS) ----------
  val pOjsNumber =
    XMLPath(
      "ext:UBLExtensions",
      "ext:UBLExtension",
      "ext:ExtensionContent",
      "efext:EformsExtension",
      "efac:Publication",
      "efbc:GazetteID"
    ).whereAttr("schemeName", "ojs-id")

  val pPublicationDate =
    XMLPath(
      "ext:UBLExtensions",
      "ext:UBLExtension",
      "ext:ExtensionContent",
      "efext:EformsExtension",
      "efac:Publication",
      "efbc:PublicationDate"
    )

  val pOjsNoticeId =
    XMLPath(
      "ext:UBLExtensions",
      "ext:UBLExtension",
      "ext:ExtensionContent",
      "efext:EformsExtension",
      "efac:Publication",
      "efbc:NoticePublicationID"
    ).whereAttr("schemeName", "ojs-notice-id")

  // ---------- Contracting Authority ----------

  // This is the path to all contracting authority OrgID
  val pOrgId =
    XMLPath(
      "cac:ContractingParty",
      "cac:Party",
      "cac:PartyIdentification",
      "cbc:ID"
    )
  // This is the path of all the Organizations relevant for this tender (both authority and supplier)
  val pOrganizations =
    XMLPath(
      "ext:UBLExtensions",
      "ext:UBLExtension",
      "ext:ExtensionContent",
      "efext:EformsExtension",
      "efac:Organizations",
      "efac:Organization"
    )
  // This is the parth in the registry where the orgID is
  val pOrgIdInRegistry =
    XMLPath("efac:Company", "cac:PartyIdentification", "cbc:ID")
  // This is where the name of the contracting authority is
  val pOrgNameInRegistry =
    XMLPath("efac:Company", "cac:PartyName", "cbc:Name")
  // This is where the country of the contracting autority is
  val pOrgCountryInRegistry =
    XMLPath(
      "efac:Company",
      "cac:PostalAddress",
      "cac:Country",
      "cbc:IdentificationCode"
    )

}
