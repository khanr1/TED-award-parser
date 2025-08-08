package io.github.khanr1.tedawardparser
package repository
package file

import weaver.SimpleIOSuite
import fs2.io.file.{Files, Path}

import scala.xml.XML
import cats.effect.IO
import fs2.text
import scala.xml.Elem

import io.github.khanr1.tedawardparser.repository.*
import io.github.khanr1.tedawardparser.repository.file.*

import fs2.Pipe
import java.time.LocalDate
import io.github.khanr1.tedawardparser.Country.toDomain
import squants.market.Money
import squants.market.*

object XMLParserSuite extends SimpleIOSuite {

  given ordering[A]: Ordering[Either[ParsingError, A]] = Ordering.by(e =>
    e match
      case Left(value)  => (0, value.toString())
      case Right(value) => (1, value.toString())
  )

  val contractOJSIDs = List(
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
  )

  val dates = List(
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
  )

  val authorityNames = List(
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
  )

  val coutries = List(
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
  )

  val contractingAuthority =
    val contractingName = authorityNames.map(x => ContractingAuthorityName(x))
    val contractingCountries = coutries.map(toDomain(_))

    contractingName
      .zip(contractingCountries)
      .map((n, c) => ContractingAuthority(n, c))
      .map(Right(_))

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
    )
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
    List("Upgrade of 3K system to Cryogen-Free Dilution Refrigerator System."),
    List("Cryofree Dilution Refrigerator Systems"),
    List(
      "EV013-17 – Lieferung, Aufbau und Inbetriebnahme von 1 Stück 3He/4He-Mischungskühlmaschine.",
      "EO003-17 — delivery, construction and commissioning of a 3He / 4He dilution refrigerator."
    )
  )
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
  )
  val values: List[List[Either[ParsingError, Money]]] = List(
    List(Right(Money(6.1292e+5, GBP))),
    List(Left(ParsingError.NoValue)),
    List(Left(ParsingError.NoValue)),
    List(Left(ParsingError.NoValue)),
    List(Left(ParsingError.NoValue), Left(ParsingError.NoValue)),
    List(Right(Money(4.98e+5, EUR))),
    List(Left(ParsingError.NoValue)),
    List(Left(ParsingError.NoValue)),
    List(Right(Money(2.8e+5, EUR))),
    List(Right(Money(9.2451e+5, EUR)), Right(Money(9.2451e+5, EUR)))
  )
  val awardedSupplerName
      : List[List[Either[ParsingError, AwardedSupplierName]]] = List(
    List(
      Right(AwardedSupplierName("Oxford Instruments Nanotechnology Tools Ltd"))
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
  val path: Path = Path(
    "/Users/raphaelkhan/Developer/ted-award-parser/04-persistence/src/test/resources/r208"
  )
  val xmlFile = Files[IO].walk(path).filter(path => path.extName == ".xml")
  def fileToElem: Pipe[IO, Path, Elem] = s =>
    s.evalMap(path =>
      Files[IO]
        .readAll(path)
        .through(text.utf8.decode)
        .compile
        .string
        .map(content => XML.loadString(content))
    )
  val xmlElems = xmlFile.through(fileToElem)

  def assertParser[A](
      parser: Elem => IO[Either[ParsingError, A]],
      expectedIO: List[Either[ParsingError, A]]
  ) =
    xmlElems
      .evalMap(parser)
      .compile
      .toList
      .map(result => {
        // println(result)
        expect.same(result.sorted, expectedIO.sorted)
      })
  def assertListParser[A](
      parser: Elem => IO[List[Either[ParsingError, A]]],
      expectedIO: List[List[Either[ParsingError, A]]]
  ) =
    xmlElems
      .evalMap(parser)
      .compile
      .toList
      .map(result => {

        expect.same(
          result.map(l => l.sorted).sorted,
          expectedIO.map(l => l.sorted).sorted
        )
      })

  val parse = new TedExportR208[IO]

  test("Parsing the OJSNumber") {
    val expectedIO = contractOJSIDs.map(Right(_))

    assertParser(parse.parseOJSNoticeID, expectedIO)

  }
  test("Parsing the publication date") {
    val expectedIO =
      dates.map(x => Right(LocalDate.parse(x, parse.dateFormatter)))

    assertParser(parse.parsePublicationDate, expectedIO)

  }
  test("Parsing Contracting Authority Name") {
    val expectedIO = authorityNames.map(Right(_))

    assertParser(parse.parseContractingAuthorityName, expectedIO)

  }

  test("Parsing Contracting Authority Country") {
    val expectedIO = coutries.map(x => Right(Country.toDomain(x)))

    assertParser(parse.parseContractingAuthorityCountry, expectedIO)
  }

  test("Parsing ContractingAuthority") {
    val expectedIO = contractingAuthority

    assertParser(parse.parseContractingAuthority, expectedIO)
  }

  test("Parsing ContractID") {
    val expectedIO = contractIDS.map(l => l.map(s => Right(ContractID(s))))
    assertListParser(parse.parseContractID, expectedIO)
  }

  test("Parsing Titles") {
    val expectedIO = titles.map(l => l.map(s => Right(Title(s))))
    assertListParser(parse.parseTenderLotTitle, expectedIO)
  }

  test("Parsing Description") {
    val expectedIO = description.map(l => l.map(s => Right(Description(s))))
    assertListParser(parse.parseTenderLotDescription, expectedIO)
  }

  test("Parsing value") {
    val expectedIO = values
    assertListParser(parse.parseTenderLotValue, expectedIO)
  }
  test("Parsing Awarded Supplier Name") {
    val expectIO = awardedSupplerName
    assertListParser(parse.parseTenderLotAwardedSupplierName, expectIO)
  }

}
