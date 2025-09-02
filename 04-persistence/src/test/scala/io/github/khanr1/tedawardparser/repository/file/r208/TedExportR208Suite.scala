package io.github.khanr1.tedawardparser
package repository
package file
package r208

import cats.effect.IO
import cats.syntax.all.*
import Country.*
import fs2.{text, Pipe}
import fs2.io.file.{Path, Files}
import io.github.khanr1.tedawardparser.repository.file.r208.R208Path.*
import java.time.LocalDate
import scala.xml.{Elem, XML}
import squants.market.*
import weaver.SimpleIOSuite
import io.github.khanr1.tedawardparser.repository.file.XMLPathUtils.showAltPath

object TedExportR208Suite extends SimpleIOSuite {

  // path to the files we are using to test
  val path: Path = Path(
    "/Users/raphaelkhan/Developer/ted-award-parser/04-persistence/src/test/resources/r208"
  )
  // Our Stram of files
  val Elems = xmlElems(path)

  // Parser to be tested
  val parser = new TedExportR208[IO]

  // files expected value

  // OjsID
  val contractOJSIDs: List[Either[ParserError, OJSNoticeID]] = List(
    "2016/S 002-001503",
    "2016/S 011-015034",
    "2016/S 015-021991",
    "2017/S 032-059079",
    "2016/S 046-075906",
    "2016/S 050-082736",
    "2016/S 123-220974",
    "2018/S 143-328218",
    "2017/S 219-456124",
    "2017/S 046-085146"
  ).map(x => Right(OJSNoticeID(x)))
  // Publication Date
  val dates: List[Right[ParserError, LocalDate]] = List(
    "20160105",
    "20160116",
    "20160122",
    "20170215",
    "20160305",
    "20160311",
    "20160629",
    "20180727",
    "20171115",
    "20170307"
  ) map (x => Right(LocalDate.parse(x, parser.dateFormatter)))
  // Contracting Authority Name
  val contractingAuthorityName
      : List[Either[ParserError, ContractingAuthorityName]] = List(
    "Kungliga Tekniska Högskolan",
    "Delft University of Technology",
    "CNRS Délégation Paris B",
    "Paul-Scherrer-Institut (PSI), Strategischer Einkauf / Purchasing Department",
    "Deutsche Forschungsgemeinschaft e. V., Zentrale Beschaffungsstelle",
    "Max-Planck-Gesellschaft zur Förderung der Wissenschaften e. V., Max-Planck-Institut für Kernphysik",
    "Science and Technology Facilities Council",
    "Universiteit Leiden",
    "Universität zu Köln, Der Kanzler",
    "Deutsches Elektronen- Synchrotron DESY in der Helmholtz Gemeinschaft"
  ).map(s => Right(ContractingAuthorityName(s)))
  // Contracting Authority Coutntry
  val countries: List[Either[ParserError, Country]] = List(
    "SE",
    "NL",
    "FR",
    "CH",
    "DE",
    "DE",
    "UK",
    "NL",
    "DE",
    "DE"
  ).map(s => Right(Country.toDomain(s)))
  // Contracting Authorities
  val contractingAuthorities = {
    val name = contractingAuthorityName
    val country = countries

    (name
      .zip(countries))
      .map((maybeN, maybeC) =>
        (maybeN, maybeC) match
          case (Right(n), Right(c)) => Right(ContractingAuthority(n, c))
          case (Left(x), _)         => Left(x)
          case (_, Left(x))         => Left(x)
      )

  }
  val contractIDS =
    List(
      List("1"),
      List("0"),
      List("2016534"),
      List("1", "1"),
      List("1"),
      List("4115411"),
      List("PR16079"),
      List("1"),
      List("102/4300040295"),
      List("71H/5317900017", "71H/5317900017")
    ) map (l => l.map(s => Right(ContractID(s))))
  // title
  val titles = List(
    List("Dilution refrigerator."),
    List("Dilution refrigerators for extended spin qubit measurements."),
    List("AOO/2015/LPA/CRYOSTAT."),
    List(
      "Large bore cryo-magnet with dilution refrigerator for neutron scattering.",
      "Cryo-aimant avec un grand diamètre interne avec réfrigérateur à dilution pour des expériences de diffusion des neutrons."
    ),
    List(
      "Beschaffung eines Verdünnungskryostaten mit Pulsröhrenkühler (DFG-GZ: A 706)."
    ),
    List("3He/4He-Verdünnungskryostat mit Pulsrohrkühlung."),
    List("UK SBS PR16079 Muon Cryogenics Equipment."),
    List(
      "Upgrade of 3K system to Cryogen-Free Dilution Refrigerator System."
    ),
    List("Cryofree Dilution Refrigerator Systems"),
    List(
      "EV013-17 – Lieferung, Aufbau und Inbetriebnahme von 1 Stück 3He/4He-Mischungskühlmaschine.",
      "EO003-17 — delivery, construction and commissioning of a 3He / 4He dilution refrigerator."
    )
  ) map (l => l.map(s => Right(Title(s))))
  // Description
  val description = List(
    List(
      "The department of applied physics at KTH has a strong experience in non-linear and quantum optics as well as in the realization and study of nano-structures. Studies on quantum devices are usually performed at low temperatures to enable the use of superconducting materials and to reduce dephasing processes. We require a dilution refrigerator able to reach low temperatures (below 20 mK) while providing optical access to the sample."
    ),
    List("Dilution refrigerators for extended spin qubit measurements."),
    List(
      "Acquisition d'un cryostat á dilution sans helium liquide équipé d'une bobine de champ magnétique 14 t."
    ),
    List("", ""),
    List("Verdünnungskryostat mit Pulsröhrenkühler."),
    List(
      "3He/4He-Verdünnungskryostat mit Pulsrohr-Kühlung. Der 3He/4He-Verdünnungskryostat mit Pulsrohr-Kühlung muss unter anderem eine Basistemperatur unter 10 Millikelvin erreichen, bei einer Temperatur von 20 mK eine Kühlleistung von mehr als 12 Mikrowatt und bei einer Temperatur von 100 mK eine Kühlleistung von mehr als 250 Mikrowatt besitzen. Kundenspezifisch sind die Vakuumkammer und die Strahlungsschilde auf vom Kunden spezifizierbarer Höhe, etwa auf Höhe der Experimentierplattform, mit horizontalliegenden Trennungsflächen unterteilt. Die Verdünnungseinheit ist vertikal ausgerichtet mit der Mischkammer als tiefstgelegenes Element. Das System enthält alle Komponenten für ein autarkes Evakuieren und Abkühlen sowie einen autarken kontinuierlichen Betrieb über mehr als 6 Monate. Beide Vorgänge sind über ein einziges Steuerprogramm zu bedienen, über TCP/IP fernsteuerbar und über Skriptsprache automatisierbar."
    ),
    List(
      "Tenders are invited to provide the Science and Technology Facilities Council (STFC) with an integrated suite of sample environment equipment for use on the EC Muon Beamlines, which form part of the ISIS Neutron and Muon facility. The equipment includes a 4He cryostat together with dilution fridge and 3He inserts, to enable facility users to conduct experiments from millikelvin temperatures to above room temperature."
    ),
    List("Upgrade of 3K system to Cryogen-Free Dilution Refrigerator System."),
    List(
      "Delivery, Installation and Briefing of a Cryfree Dilution Refrigerator Systems"
    ),
    List(
      "Im Rahmen des am DESY durchgeführten ALPS-Experiments wird die Lieferung, der Aufbau und die Inbetriebnahme einer 3He/4He-Mischungskühlmaschine, bestehend aus den folgenden Komponenten, benötigt: 1. Kryostat und Kryostateneinsatz mit allen erforderlichen Komponenten des Mischungskreislaufs sowie zusätzlichen Komponenten; 2. Pulsröhrenkühler (pulse tube refrigerator, PTR, zweistufg) installiert im Kryostateneinsatz zu dessen Vorkühlung; 3. Gashandling- und Kontrollsystem-Komponenten; 4. Betriebsthermometrie und Heizer inklusive Verdrahtung installiert im Kryostateneinsatz; 5. Gestell für die Aufnahme des Kryostaten; 6. Dokumentation zu Betrieb und Wartung der Mischungskühlmaschine und der verwendeten Komponenten; 7. Aufbau und Zusammenbau aller Komponenten; 8. Inbetriebnahme und Einweisung in den Betrieb der Mischungskühlmaschine am Aufstellungsort.",
      "The delivery, construction and commissioning of a 3He / 4He dilution refrigerator, consisting of the following components, is required as part of the ALPS experiment carried out at the DESY: 1. Cryostat and cryostat insert with all necessary components of the mixing circuit as well as additional components, 2. Pulse tube refrigerator (PTR, two stage) installed in the cryostat for pre-cooling, 3. Gashandling and control system components, 4. Thermometers and heater incl. wiring installed in the cryostat insert needed for operation, 5. Frame for holding the cryostat, 6. Documentation of the operation and maintenance of the dilution refrigerator and all its components, 7. Assembly of the dilution refrigerator and all its components, 8. Commissioning and instruction on the operation of the dilution refrigerator at the installation site."
    )
  ) map (l =>
    l.map(s =>
      if (s.isEmpty)
        Left(
          ParserError.MissingField(
            "Description",
            Some(
              List(ContractAwardInfo, VeatAwardInfo).showAltPath()
            )
          )
        )
      else
        Right(Description(s))
    )
  )
  // Money
  val values: List[List[Either[ParserError, Money]]] = List(
    List(Right(Money(6.1292e+5, GBP))),
    List(
      Left(
        ParserError.MissingField(
          "Amount",
          Some(List(AwardOfContract, VeatAwardOfContact).showAltPath())
        )
      )
    ),
    List(
      Left(
        ParserError.MissingField(
          "Amount",
          Some(List(AwardOfContract, VeatAwardOfContact).showAltPath())
        )
      )
    ),
    List(
      Left(
        ParserError.MissingField(
          "Amount",
          Some(List(AwardOfContract, VeatAwardOfContact).showAltPath())
        )
      )
    ),
    List(
      Left(
        ParserError.MissingField(
          "Amount",
          Some(List(AwardOfContract, VeatAwardOfContact).showAltPath())
        )
      ),
      Left(
        ParserError.MissingField(
          "Amount",
          Some(List(AwardOfContract, VeatAwardOfContact).showAltPath())
        )
      )
    ),
    List(Right(Money(4.98e+5, EUR))),
    List(
      Left(
        ParserError.MissingField(
          "Amount",
          Some(List(AwardOfContract, VeatAwardOfContact).showAltPath())
        )
      )
    ),
    List(Right(Money(4.48e+5, EUR))),
    List(Right(Money(2.8e+5, EUR))),
    List(Right(Money(9.2451e+5, EUR)), Right(Money(9.2451e+5, EUR)))
  )
  // Awarded Supplier
  val awardedSupplerName: List[List[Either[ParserError, AwardedSupplierName]]] =
    List(
      List(
        Right(
          AwardedSupplierName("Oxford Instruments Nanotechnology Tools Ltd")
        )
      ),
      List(Right(AwardedSupplierName("BlueFors"))),
      List(Right(AwardedSupplierName("BlueFors Cryogenics Oy Ltd"))),
      List(Right(AwardedSupplierName("BLueFors Cryogenics Oy"))),
      List(
        Right(AwardedSupplierName("BlueFors Cryogenics Oy Ltd")),
        Right(AwardedSupplierName("BlueFors Cryogenics Oy Ltd"))
      ),
      List(Right(AwardedSupplierName("Oxford Instruments Nanotechnology"))),
      List(Right(AwardedSupplierName("BlueFors Cryogenics Oy Ltd"))),
      List(Right(AwardedSupplierName("Oxford Instruments GmbH"))),
      List(Right(AwardedSupplierName("Leiden Cryogenics BV"))),
      List(
        Right(AwardedSupplierName("Oxford Instruments GmbH")),
        Right(AwardedSupplierName("Oxford Instruments GmbH"))
      )
    )
  // Awarded Supplier Country
  val supplierCountry = List(
    List(Right(UK)),
    List(Right(FI)),
    List(Right(FI)),
    List(
      Left(
        ParserError.MissingField(
          "Awarded Supplier Country",
          Some(
            List(AwardOfContract, VeatAwardOfContact).showAltPath()
          )
        )
      )
    ),
    List(Right(FI), Right(FI)),
    List(Right(UK)),
    List(Right(FI)),
    List(Right(DE)),
    List(Right(NL)),
    List(Right(DE), Right(DE))
  )
  // Tests
  test("Parsing the OJSNumber") {
    assertParser(parser.parseOJSNoticeID, contractOJSIDs, Elems)
  }
  test(
    "Parsing the OJSNumber fails with MissingField when NO_DOC_OJS is absent"
  ) {
    val xml: Elem =
      <ROOT>
      <CODED_DATA_SECTION>
        <NOTICE_DATA>
          <!-- NO_DOC_OJS is intentionally missing -->
        </NOTICE_DATA>
      </CODED_DATA_SECTION>
    </ROOT>
    val io = parser.parseOJSNoticeID(xml)

    expectMissingField(io, "OjsID", R208Path.OjsID)
  }

  test(
    "Parsing the OJSNumber fails with MissingField when NO_DOC_OJS is empty text"
  ) {
    val xml: Elem =
      <ROOT>
      <CODED_DATA_SECTION>
        <NOTICE_DATA>
          <NO_DOC_OJS>   </NO_DOC_OJS>
        </NOTICE_DATA>
      </CODED_DATA_SECTION>
    </ROOT>
    val io = parser.parseOJSNoticeID(xml)
    expectMissingField(io, "OjsID", R208Path.OjsID, " for empty value")
  }

  test("Parsing the Publication Date") {

    assertParser(parser.parsePublicationDate, dates, Elems)

  }

  test(
    "Parsing the Publication Date fails with MissingField when DATE_PUB is absent"
  ) {
    val xml: Elem =
      <ROOT>
      <CODED_DATA_SECTION>
        <REF_OJS>
          <!-- DATE_PUB is intentionally missing -->
        </REF_OJS>
      </CODED_DATA_SECTION>
    </ROOT>
    val io = parser.parsePublicationDate(xml)

    expectMissingField(io, "Publication Date", R208Path.PublicationDate)
  }

  test(
    "Parsing the Publication Date fails with MissingField when DATE_PUB is empty text"
  ) {
    val xml: Elem =
      <ROOT>
      <CODED_DATA_SECTION>
        <REF_OJS>
          <DATE_PUB>
          </DATE_PUB>
        </REF_OJS>
      </CODED_DATA_SECTION>
    </ROOT>
    val io = parser.parsePublicationDate(xml)

    expectMissingField(io, "Publication Date", R208Path.PublicationDate)
  }

  test(
    "Parsing the Publication Date fails with InvalidForma when DATE_PUB text has not the format yyyyMMdd"
  ) {
    val xml: Elem =
      <ROOT>
      <CODED_DATA_SECTION>
        <REF_OJS>
          <DATE_PUB>
           23/12/2020
          </DATE_PUB>
        </REF_OJS>
      </CODED_DATA_SECTION>
    </ROOT>
    val io = parser.parsePublicationDate(xml)

    expectInvalidFormat(
      io,
      "Publication Date",
      "yyyyMMdd",
      "23/12/2020",
      R208Path.PublicationDate
    )
  }

  test("Parsing the Contracting Authority Name") {
    assertParser(
      parser.parseContractingAuthorityName,
      contractingAuthorityName,
      Elems
    )
  }
  test("Parsing the Contracting Authority Country") {
    assertParser(parser.parseContractingAuthorityCountry, countries, Elems)
  }
  test("Parsing the Contracting Authority") {
    assertParser(
      parser.parseContractingAuthority,
      contractingAuthorities,
      Elems
    )
  }

  test("Parsing the Contracting Award contract/Lot ID") {
    assertListParser(parser.parseContractID, contractIDS, Elems)
  }
  test("Parsing the Contract Title") {
    assertListParser(parser.parseTenderLotTitle, titles, Elems)
  }
  test("Parsing the Contract Description") {
    assertListParser(parser.parseTenderLotDescription, description, Elems)
  }
  test("Parsing the Contract Value") {
    assertListParser(parser.parseTenderLotValue, values, Elems)
  }
  test("Parsing the Awarded Supplier Name") {
    assertListParser(
      parser.parseTenderLotAwardedSupplierName,
      awardedSupplerName,
      Elems
    )
  }
  test("Parsing the Awarded Supplier Country") {
    assertListParser(
      parser.parseTenderLotAwardedSupplierCountry,
      supplierCountry,
      Elems
    )
  }

}
