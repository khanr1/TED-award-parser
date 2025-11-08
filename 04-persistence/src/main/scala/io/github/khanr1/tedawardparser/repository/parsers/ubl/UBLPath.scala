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
  val pContractingOrgId =
    XMLPath(
      "cac:ContractingParty",
      "cac:Party",
      "cac:PartyIdentification",
      "cbc:ID"
    )
  // This is the path of all the Organizations relevant for this tender
  val pOrganizations =
    XMLPath(
      "ext:UBLExtensions",
      "ext:UBLExtension",
      "ext:ExtensionContent",
      "efext:EformsExtension",
      "efac:Organizations",
      "efac:Organization"
    )
  // This is the path in the registry where the orgID is
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

  // Tenders Lot
  val pTenderLot = XMLPath("cac:ProcurementProjectLot")
  val pTenderLotID = XMLPath("cbc:ID")
  val pTenderLotName = XMLPath("cac:ProcurementProject", "cbc:Name")
  val pTenderLotDescription =
    XMLPath("cac:ProcurementProject", "cbc:Description")

  // Currency value and suppliers name
  val pNoticeResult =
    XMLPath(
      "ext:UBLExtensions",
      "ext:UBLExtension",
      "ext:ExtensionContent",
      "efext:EformsExtension",
      "efac:NoticeResult"
    )
  val pLotResult = XMLPath("efac:LotResult")
  val pTotalValue = XMLPath("cbc:TotalAmount")
  val pLotTenderID = XMLPath("cbc:ID")

  val pLotTender = XMLPath("efac:LotTender")
  val pLotTenderValue = XMLPath("cac:LegalMonetaryTotal", "cbc:PayableAmount")
  val pLotTenderingParty = XMLPath("efac:TenderingParty")
  val pLotTenderingPartyOrgID =
    XMLPath("efac:Tenderer", "cbc:ID")

  val pJustification = XMLPath(
    "cac:TenderingProcess",
    "cac:ProcessJustification",
    "cbc:ProcessReason"
  )

}
