package io.github.khanr1.tedawardparser
package repository
package file
package r209

import cats.effect.IO
import cats.syntax.all.*
import Country.*
import fs2.{text, Pipe}
import fs2.io.file.{Path, Files}

import java.time.LocalDate
import scala.xml.{Elem, XML}
import squants.market.*
import weaver.SimpleIOSuite
import io.github.khanr1.tedawardparser.repository.parsers.XMLPathUtils.showAltPath
import parsers.r209.TedExportR209
import parsers.ParserError

object TedExportR209Suite extends SimpleIOSuite {

  // path to the files we are using to test
  val path: Path = Path(
    "/Users/raphaelkhan/Developer/ted-award-parser/04-persistence/src/test/resources/r209"
  )
  // Our Stram of files
  val Elems = xmlElems(path)

  // Parser to be tested
  val parser = new TedExportR209[IO]

  // files expected value

  // OjsID
  val contractOJSIDs: List[Either[ParserError, OJSNoticeID]] = List(
    "2021/S 005-004810",
    "2018/S 007-011086",
    "2023/S 013-032722",
    "2018/S 042-091924",
    "2021/S 230-603596"
  ).map(x => Right(OJSNoticeID(x)))
  // Publication Date
  val dates: List[Right[ParserError, LocalDate]] = List(
    "20210108",
    "20180111",
    "20230118",
    "20180301",
    "20211126"
  ) map (x => Right(LocalDate.parse(x, parser.dateFormatter)))
  // Contracting Authority Name
  val contractingAuthorityName
      : List[Either[ParserError, ContractingAuthorityName]] = List(
    "Békéscsabai Szakképzési Centrum",
    "CEA Grenoble",
    "Georg-August-Universität Göttingen, Zentraler Einkauf GM 23",
    "Universiteit Leiden",
    "Centre National de la Recherche Scientifique"
  ).map(s => Right(ContractingAuthorityName(s)))
  // Contracting Authority Countntry
  val countries: List[Either[ParserError, Country]] = List(
    "HU",
    "FR",
    "DE",
    "NL",
    "FR"
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
      List("1", "2", "3", "4", "5", "6", "7", "8"),
      List("4000780446"),
      List("1"),
      List("1"),
      List("2024919", "2024920", "2024922", "2024923")
    ) map (l => l.map(s => Right(ContractID(s))))
  // title
  val titles =
    List(
      List(
        "Termelő gépek",
        "Mérő és vizsgáló eszközök",
        "Oktató gépek, oktató szoftverek",
        "Asztali és kézi szerszámgépek, kéziszerszámok",
        "Automatizált gyártósor kialakítás",
        "Épületautomatizálás oktatócsomag",
        "Felületkezelés tanműhely és laboratórium",
        "Bútorok"
      ),
      List("Fourniture d’un réfrigérateur à dilution de type cryogenfree"),
      List("Beschaffung eines mK-Mischkryostaten mit Vektormagnet"),
      List("Low Temperature STM"),
      List(
        "Cryostat 4K à circuit fermé d’hélium pour l’optomécanique GHz pour le C2N",
        "Cryostat 4K à circuit fermé d’hélium pour l’optomécanique pour le C2N",
        "Cryostat 4K à circuit fermé d’hélium pour microscopie optique en champ magnétique pour le C2N",
        "Acquisition, livraison, installation et mise en service d’un cryostat à dilution sans hélium liquide pour le laboratoire de Physique des Solides (LPS)"
      )
    ) map (l => l.map(s => Right(Title(s))))
  // Description
  val description =
    List(
      List(
        TenderShortDescriptions.lot1ShortDescr.replaceAll("\\s+", " ").trim,
        TenderShortDescriptions.lot2ShortDescr.replaceAll("\\s+", " ").trim,
        TenderShortDescriptions.lot3ShortDescr.replaceAll("\\s+", " ").trim,
        TenderShortDescriptions.lot4ShortDescr.replaceAll("\\s+", " ").trim,
        TenderShortDescriptions.lot5ShortDescr.replaceAll("\\s+", " ").trim,
        TenderShortDescriptions.lot6ShortDescr.replaceAll("\\s+", " ").trim,
        TenderShortDescriptions.lot7ShortDescr.replaceAll("\\s+", " ").trim,
        TenderShortDescriptions.lot8ShortDescr.replaceAll("\\s+", " ").trim
      ),
      List(
        "Fourniture d'un réfrigérateur à dilution de type cryogen-free pour l'étude de bits quantiques de spin couplés à des circuits supraconducteurs quantiques dans le régime micro-ondes comprenant les options suivantes: Option no 1: fourniture d'un câblage électrique pour la mesure des échantillons; Option no 2: extension de garantie d'un an minimum; Option no 3: fourniture d'un mélange He3/He4, pour le fonctionnement du réfrigérateur; Option no 4: fourniture d'un châssis; Option no 5: fourniture d'un deuxième réfrigérateur à dilution de type cryogen-free."
      ),
      List("Beschaffung eines mK-Mischkryostaten mit Vektormagnet"),
      List(
        "Development and delivery of a low-temperature STM in 2 phases: design phase and production phase."
      ),
      List(
        "Cryostat 4K à circuit fermé d’hélium pour l’optomécanique GHz pour le C2N",
        "Cryostat 4K à circuit fermé d’hélium pour l’optomécanique pour le C2N",
        "Cryostat 4K à circuit fermé d’hélium pour microscopie optique en champ magnétique pour le C2N",
        "Acquisition, livraison, installation et mise en service d’un cryostat à dilution sans hélium liquide pour le laboratoire de Physique des Solides (LPS)"
      )
    ) map (l =>
      l.map(s =>
        if (s.isEmpty)
          Left(
            ParserError.MissingField(
              "Description",
              Some(
                List(
                  parsers.r209.R209Path.ContractAwardInfo,
                  parsers.r209.R209Path.VeatAwardInfo
                )
                  .map(_.show)
                  .mkString("|")
              )
            )
          )
        else
          Right(Description(s))
      )
    )
  // Money
  val values: List[List[Either[ParserError, Money]]] = List(
    List(
      Left(
        ParserError.MissingField(
          "Amount",
          Some(
            (List(
              parsers.r209.R209Path.ContractAwardInfo,
              parsers.r209.R209Path.VeatAwardInfo
            )).showAltPath()
          )
        )
      )
    ),
    List(Right(EUR(0.90))),
    List(Right(EUR(558000.00))),
    List(
      Right(EUR(117275.9)),
      Right(EUR(120875.9)),
      Right(EUR(153318.45)),
      Right(EUR(2.5e+5))
    ),
    List(Right(EUR(3.2926e+5)))
  )
  // Awarded Supplier
  val awardedSupplerName: List[List[Either[ParserError, AwardedSupplierName]]] =
    List(
      List(Right(AwardedSupplierName("Bluefors cryogenics Oy LTD"))),
      List(Right(AwardedSupplierName("Fa. Bluefors Oy"))),
      List(),
      List(
        Right(AwardedSupplierName("My cryofirm")),
        Right(AwardedSupplierName("MY CRYOFIRM")),
        Right(AwardedSupplierName("my cryofirm")),
        Right(AwardedSupplierName("BLUEFORS OY"))
      ),
      List(Right(AwardedSupplierName("BWI bv")))
    )

  // Awarded Supplier Country
  val supplierCountry = List(
    List(Right(Country.FI)),
    List(Right(Country.FI)),
    List(),
    List(
      Right(Country.FR),
      Right(Country.FR),
      Right(Country.FR),
      Right(Country.FI)
    ),
    List(Right(Country.NL))
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

    expectMissingField(io, "OjsID", parsers.r209.R209Path.OjsID)
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
    expectMissingField(
      io,
      "OjsID",
      parsers.r209.R209Path.OjsID,
      " for empty value"
    )
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

    expectMissingField(
      io,
      "Publication Date",
      parsers.r209.R209Path.PublicationDate
    )
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

    expectMissingField(
      io,
      "Publication Date",
      parsers.r209.R209Path.PublicationDate
    )
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
      parsers.r209.R209Path.PublicationDate
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
