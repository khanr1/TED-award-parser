# TED Award Parser

Parse award information from European public procurement notices (TED XML exports) and produce a normalised, CSV-ready stream of results.

Supports both the legacy **R2.0.8** and the current **R2.0.9** TED schemas. Each field in the output is either a successfully parsed value or a structured error, so the pipeline is resilient to heterogeneous or partially-complete source files.

---

## Table of contents

1. [What it does](#what-it-does)
2. [Architecture](#architecture)
3. [Parsing pipeline](#parsing-pipeline)
4. [Form format detection](#form-format-detection)
5. [Path reference](#path-reference)
   - [Coded Data Section (all formats)](#coded-data-section-all-formats)
   - [R2.0.8 — Form F03 (Contract Award Notice)](#r208--form-f03-contract-award-notice)
   - [R2.0.8 — Form F15 (VEAT Notice)](#r208--form-f15-veat-notice)
   - [R2.0.9 — Form F03 (Contract Award Notice)](#r209--form-f03-contract-award-notice)
   - [R2.0.9 — Form F15 (VEAT Notice)](#r209--form-f15-veat-notice)
6. [XML Path DSL](#xml-path-dsl)
7. [Error model](#error-model)
8. [CSV output format](#csv-output-format)
9. [Getting started](#getting-started)
10. [Roadmap](#roadmap)

---

## What it does

Given a directory of TED XML files, the parser:

1. Detects the schema version (R2.0.8 / R2.0.9) and the form type (F03 / F15 / F02) from each file's XML structure.
2. Extracts all the fields listed below from the `CODED_DATA_SECTION` and `FORM_SECTION` blocks.
3. Expands each notice into one row per awarded contract / lot.
4. Writes a CSV where every field is either the parsed value or a human-readable error message.

**Extracted fields per notice:**

| Group | Fields |
|---|---|
| OJ Reference | Journal series, Journal number, Publication date |
| Notice Metadata | OJS notice number, Document URIs, Language(s), Buyer country, Buyer URL, CPV code, Reference notice |
| Coded Indicators | Dispatch date, Authority type, Document type, Contract nature, Procedure type, Regulation scope, Bid type, Award criterion, Main activity, Heading code, Initiator code, EU directive |
| Form | Form type (R208F03, R209F15, …) |
| Contracting Authority | Name, National ID, Address, Town, Postal code, Country, Point of contact, Phone, Email |
| Object of Contract | Title, Description, Total value, Currency |
| Complementary Information | Additional info, EU project reference, Notice dispatch date, Appeal body name, Justification |
| Award Contract (one row per lot) | Contract number, Lot number, Contract title, Award date, Contractor name/ID/address/town/postal code/country/contact/phone/email, Contract value, Currency |

---

## Architecture

```
ted-award-parser/
├── 01-domain/          Pure domain models — no I/O
├── 02-core/            Application services, CSV transformation
├── 03-delivery/        Adapter scaffolding (placeholder)
├── 04-persistence/     XML parsing, file repository
│   ├── repository/
│   │   ├── xml/        XML DSL, decoders, assembler, mapper
│   │   ├── xpath/      Path constants per schema version
│   │   ├── file/       FS2-based file streaming
│   │   └── csv/        CSV serialisation
├── 05-main/            Application entry point
└── 06-frontend/        Scala.js frontend (placeholder)
```

Key source files:

| File | Role |
|---|---|
| `repository/xml/XMLPath.scala` | Lightweight XPath-like path DSL (`Segment`, `QName`, `XMLPath`) |
| `repository/xml/XMLPathUtils.scala` | Extension methods on `Elem`: `textAt`, `attrAt`, `nodesAt`, `firstText`, etc. |
| `repository/xml/XMLDecoder.scala` | `XMLDecoder[A]` typeclass + `Raw` marker trait |
| `repository/xml/ParserError.scala` | Structured error ADT: `MissingField`, `InvalidFormat`, `MultipleValues`, `UnexpectedNode`, `Unknown` |
| `repository/xml/NoticeAssembler.scala` | Format detection + top-level decode entry point → `ParsedNotice` |
| `repository/xml/NoticeMapper.scala` | `ParsedNotice` → domain `Notice` |
| `repository/xml/decoders/` | Concrete `XMLDecoder` instances for each section and schema version |
| `repository/xpath/FormSectionPath.scala` | Path traits (contracts for all path objects) |
| `repository/xpath/FormSectionPathR208.scala` | R2.0.8 path constants (F03, F15, F02) |
| `repository/xpath/FormSectionPathR209.scala` | R2.0.9 path constants (F03, F15, F02) |
| `repository/xpath/CodedDataSectionPath.scala` | Coded-data paths (version-independent) |
| `repository/file/XMLFileRepository.scala` | Streams `*.xml` files from a directory |

---

## Parsing pipeline

```
Directory of *.xml files
        │
        ▼
  XMLFileRepository          (FS2 stream over files)
        │  scala.xml.Elem
        ▼
  NoticeAssembler.detectFormat
        │  TedFormat (R208F03 | R208F15 | R208F02 | R209F03 | R209F15 | R209F02)
        ▼
  NoticeAssembler.decode
        │
        ├─── CodedDataSectionDecoder  ──► CodedDataSectionRaw
        │         (version-independent; same paths for all formats)
        │
        ├─── ContractingAuthorityDecoder208/209  ──► ContractingAuthorityRaw
        │         (dispatches on whether F03 or F15 root is present)
        │
        ├─── ContractAwardObjectInformationDecoder208/209  ──► ContractAwardObjectInformationRaw
        │
        ├─── AwardContractDecoder208/209  (one per AWARD_OF_CONTRACT element)
        │         ──► List[AwardContractRaw]
        │
        └─── ComplementaryInformationDecoder  ──► ComplementaryInformationRaw
                  (variant per R208F03 | R208F15 | R209F03 | R209F15)
        │
        ▼
    ParsedNotice  (all Raw fields: Either[ParserError, String] per field)
        │
        ▼
    NoticeMapper.toDomain
        │
        ▼
    Notice  (domain model; missing values become empty strings in CSV)
        │
        ▼
    Notice.toCsvRows  ──► List[List[String]]  (one row per lot)
        │
        ▼
    CSV file
```

### Award-contract iteration

For **R2.0.8** notices the assembler takes only the **first** `FD_CONTRACT_AWARD` (or `FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE`) block to avoid duplicating rows when a notice is published in multiple languages. Each child `AWARD_OF_CONTRACT` (F03) or `AWARD_OF_CONTRACT_DEFENCE` (F15) element becomes one `AwardContractRaw`.

For **R2.0.9** notices every `AWARD_CONTRACT` child of the form root becomes one `AwardContractRaw`.

When an award element contains `NO_AWARDED_CONTRACT` or has no child elements, a stub row is produced with `contractorName = "No awarded contract"` and all other award fields as `MissingField` errors.

---

## Form format detection

`NoticeAssembler.detectFormat` inspects the `FORM_SECTION` children in priority order:

| Detected element | Format |
|---|---|
| `FORM_SECTION/F03_2014` | `R209F03` |
| `FORM_SECTION/F15_2014` | `R209F15` |
| `FORM_SECTION/F02_2014` | `R209F02` |
| `FORM_SECTION/CONTRACT_AWARD/FD_CONTRACT_AWARD` | `R208F03` |
| `FORM_SECTION/VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE/FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE` | `R208F15` |
| `FORM_SECTION/CONTRACT/FD_CONTRACT` | `R208F02` |
| *(none of the above)* | `None` — file is skipped |

Within R2.0.8 the contracting-authority and object-information decoders additionally check the label of the matched node (`FD_CONTRACT_AWARD` → F03 paths, anything else → F15 paths).

---

## Path reference

All paths are expressed as slash-separated element names from the **document root**. Paths ending with `@ATTR` select an attribute. Paths marked *relative* are applied to each individual award element, not the document root.

### Coded Data Section (all formats)

These paths are version-independent and apply to every TED notice.

#### `CODED_DATA_SECTION/CODIF_DATA` — Coded indicator fields

| Field | Path |
|---|---|
| Dispatch date | `CODED_DATA_SECTION/CODIF_DATA/DS_DATE_DISPATCH` |
| Authority type | `CODED_DATA_SECTION/CODIF_DATA/AA_AUTHORITY_TYPE` |
| Document type | `CODED_DATA_SECTION/CODIF_DATA/TD_DOCUMENT_TYPE` |
| Contract nature | `CODED_DATA_SECTION/CODIF_DATA/NC_CONTRACT_NATURE` |
| Procedure type | `CODED_DATA_SECTION/CODIF_DATA/PR_PROC` |
| Regulation scope | `CODED_DATA_SECTION/CODIF_DATA/RP_REGULATION` |
| Bid type | `CODED_DATA_SECTION/CODIF_DATA/TY_TYPE_BID` |
| Award criterion | `CODED_DATA_SECTION/CODIF_DATA/AC_AWARD_CRIT` |
| Main activity | `CODED_DATA_SECTION/CODIF_DATA/MA_MAIN_ACTIVITIES` |
| Heading code | `CODED_DATA_SECTION/CODIF_DATA/HEADING` |
| Initiator code | `CODED_DATA_SECTION/CODIF_DATA/INITIATOR` |
| Directive | `CODED_DATA_SECTION/CODIF_DATA/DIRECTIVE/@VALUE` |

#### `CODED_DATA_SECTION/NOTICE_DATA` — Notice metadata

| Field | Path |
|---|---|
| OJS notice number | `CODED_DATA_SECTION/NOTICE_DATA/NO_DOC_OJS` |
| Document URIs (parent) | `CODED_DATA_SECTION/NOTICE_DATA/URI_LIST` |
| Language | `CODED_DATA_SECTION/NOTICE_DATA/LG_ORIG` |
| Buyer country | `CODED_DATA_SECTION/NOTICE_DATA/ISO_COUNTRY/@VALUE` |
| Buyer URL | `CODED_DATA_SECTION/NOTICE_DATA/IA_URL_GENERAL` |
| CPV code | `CODED_DATA_SECTION/NOTICE_DATA/ORIGINAL_CPV` |
| Values (parent) | `CODED_DATA_SECTION/NOTICE_DATA/VALUES_LIST/VALUES` |
| Amount *(relative to each VALUES child)* | `SINGLE_VALUE/VALUE` |
| Currency *(relative to each VALUES child)* | `SINGLE_VALUE/VALUE/@CURRENCY` |
| Reference notice | `CODED_DATA_SECTION/NOTICE_DATA/REF_NOTICE/NO_DOC_OJS` |

#### `CODED_DATA_SECTION/REF_OJS` — Official Journal reference

| Field | Path |
|---|---|
| Journal series | `CODED_DATA_SECTION/REF_OJS/COLL_OJ` |
| Journal number | `CODED_DATA_SECTION/REF_OJS/NO_OJ` |
| Publication date | `CODED_DATA_SECTION/REF_OJS/DATE_PUB` |

---

### R2.0.8 — Form F03 (Contract Award Notice)

Form root: `FORM_SECTION/CONTRACT_AWARD/FD_CONTRACT_AWARD`

#### Contracting Authority

Base path: `{form root}/CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD/NAME_ADDRESSES_CONTACT_CONTRACT_AWARD/CA_CE_CONCESSIONAIRE_PROFILE`

| Field | Path (relative to base) |
|---|---|
| Official name | `ORGANISATION/OFFICIALNAME` |
| National ID | `ORGANISATION/NATIONALID` |
| Address | `ADDRESS` |
| Town | `TOWN` |
| Postal code | `POSTAL_CODE` |
| Country | `COUNTRY/@VALUE` |
| Point of contact | `CONTACT_POINT` |
| Phone | `PHONE` |
| Email | `E_MAILS/E_MAIL` |

#### Activity and Type (Authority type / Activity)

Base path: `{form root}/CONTRACTING_AUTHORITY_INFORMATION_CONTRACT_AWARD/TYPE_AND_ACTIVITIES_AND_PURCHASING_ON_BEHALF`

| Field | Path (relative to base) |
|---|---|
| Authority type | `TYPE_AND_ACTIVITIES/TYPE_OF_CONTRACTING_AUTHORITY/@VALUE` |
| Authority type (other) | `TYPE_AND_ACTIVITIES/TYPE_OF_CONTRACTING_AUTHORITY_OTHER/@VALUE` |
| Activity | `TYPE_AND_ACTIVITIES/TYPE_OF_ACTIVITY/@VALUE` |
| Activity (other) | `TYPE_AND_ACTIVITIES/TYPE_OF_ACTIVITY_OTHER/@VALUE` |
| On-behalf info root | `PURCHASING_ON_BEHALF/PURCHASING_ON_BEHALF_YES` |
| On-behalf contact data | `PURCHASING_ON_BEHALF/PURCHASING_ON_BEHALF_YES/CONTACT_DATA_OTHER_BEHALF_CONTRACTING_AUTORITHY` |

#### Object of Contract

Base path: `{form root}/OBJECT_CONTRACT_INFORMATION_CONTRACT_AWARD_NOTICE`

| Field | Path (relative to base) |
|---|---|
| Title | `DESCRIPTION_AWARD_NOTICE_INFORMATION/TITLE_CONTRACT` |
| Description | `DESCRIPTION_AWARD_NOTICE_INFORMATION/SHORT_CONTRACT_DESCRIPTION` |
| Total value | `TOTAL_FINAL_VALUE/COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE/VALUE_COST` |
| Currency | `TOTAL_FINAL_VALUE/COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE/@CURRENCY` |

#### Award Contract *(paths are relative to each `AWARD_OF_CONTRACT` element)*

| Field | Path |
|---|---|
| Contract number | `AWARD_OF_CONTRACT/CONTRACT_NUMBER` |
| Contract title | `AWARD_OF_CONTRACT/CONTRACT_TITLE/P` |
| Lot number | `AWARD_OF_CONTRACT/LOT_NUMBER` |
| Award date | `AWARD_OF_CONTRACT/CONTRACT_AWARD_DATE` |
| Contractor base | `AWARD_OF_CONTRACT/ECONOMIC_OPERATOR_NAME_ADDRESS/CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME` |
| Contractor name | `{contractor base}/ORGANISATION/OFFICIALNAME` |
| Contractor National ID | `{contractor base}/ORGANISATION/NATIONALID` |
| Contractor address | `{contractor base}/ADDRESS` |
| Contractor town | `{contractor base}/TOWN` |
| Contractor postal code | `{contractor base}/POSTAL_CODE` |
| Contractor country | `{contractor base}/COUNTRY/@VALUE` |
| Contractor point of contact | `{contractor base}/CONTACT_POINT` |
| Contractor phone | `{contractor base}/PHONE` |
| Contractor email | `{contractor base}/E_MAILS/E_MAIL` |
| Contract value parent | `AWARD_OF_CONTRACT/CONTRACT_VALUE_INFORMATION/COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE` |
| Contract amount | `{value parent}/VALUE_COST` |
| Contract currency | `{value parent}/@CURRENCY` |

> **Note — FMTVAL fallback.** The assembler first tries the `FMTVAL` attribute on `VALUE_COST` (canonical numeric value without thousands separators) before falling back to the element text.

#### Complementary Information

Base path: `{form root}/COMPLEMENTARY_INFORMATION_CONTRACT_AWARD`

| Field | Path (relative to base) |
|---|---|
| Additional information | `ADDITIONAL_INFORMATION` |
| EU project | `RELATES_TO_EU_PROJECT_YES/P` |
| Notice dispatch date | `NOTICE_DISPATCH_DATE` |
| Appeal body name | `PROCEDURES_FOR_APPEAL/LODGING_INFORMATION_FOR_SERVICE/CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME/ORGANISATION/OFFICIALNAME` |
| Justification | *(not applicable for F03; empty path)* |

---

### R2.0.8 — Form F15 (VEAT Notice)

Form root: `FORM_SECTION/VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE/FD_VOLUNTARY_EX_ANTE_TRANSPARENCY_NOTICE`

#### Contracting Authority

Base path: `{form root}/CONTRACTING_AUTHORITY_VEAT/NAME_ADDRESSES_CONTACT_VEAT/CA_CE_CONCESSIONAIRE_PROFILE`

Same leaf fields as R2.0.8 F03 (see above).

#### Activity and Type

Base path: `{form root}/CONTRACTING_AUTHORITY_VEAT/TYPE_AND_ACTIVITIES_OR_CONTRACTING_ENTITY_AND_PURCHASING_ON_BEHALF`

Same leaf paths as R2.0.8 F03.

#### Object of Contract

Base path: `{form root}/OBJECT_VEAT`

| Field | Path (relative to base) |
|---|---|
| Title | `DESCRIPTION_VEAT/TITLE_CONTRACT` |
| Description | `DESCRIPTION_VEAT/SHORT_CONTRACT_DESCRIPTION` |
| Total value | `TOTAL_FINAL_VALUE/COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE/VALUE_COST` |
| Currency | `TOTAL_FINAL_VALUE/COSTS_RANGE_AND_CURRENCY_WITH_VAT_RATE/@CURRENCY` |

#### Award Contract *(relative to each `AWARD_OF_CONTRACT_DEFENCE` element)*

Same fields as R2.0.8 F03 award contract, with identical XPath structure.

#### Complementary Information

Base path: `{form root}/COMPLEMENTARY_INFORMATION_VEAT`

| Field | Path (relative to base) |
|---|---|
| Additional information | `ADDITIONAL_INFORMATION` |
| EU project | `RELATES_TO_EU_PROJECT_YES/P` |
| Notice dispatch date | `NOTICE_DISPATCH_DATE` |
| Appeal body name | `PROCEDURES_FOR_APPEAL/LODGING_INFORMATION_FOR_SERVICE/CONTACT_DATA_WITHOUT_RESPONSIBLE_NAME/ORGANISATION/OFFICIALNAME` |
| Justification | `{form root}/PROCEDURE_DEFINITION_VEAT/TYPE_OF_PROCEDURE_DEF_F15/F15_PT_NEGOTIATED_WITHOUT_COMPETITION/ANNEX_D_F15/ANNEX_D1/REASON_CONTRACT_LAWFUL` |

---

### R2.0.9 — Form F03 (Contract Award Notice)

Form root: `FORM_SECTION/F03_2014`

#### Contracting Authority

Base path: `{form root}/CONTRACTING_BODY/ADDRESS_CONTRACTING_BODY`

| Field | Path (relative to base) |
|---|---|
| Official name | `OFFICIALNAME` |
| National ID | `NATIONALID` |
| Address | `ADDRESS` |
| Town | `TOWN` |
| Postal code | `POSTAL_CODE` |
| Country | `COUNTRY/@VALUE` |
| Point of contact | `CONTACT_POINT` |
| Phone | `PHONE` |
| Email | `E_MAIL` |

#### Activity and Type

Paths directly under `{form root}` (no sub-container):

| Field | Path |
|---|---|
| Authority type | `{form root}/CA_TYPE/@VALUE` |
| Authority type (other) | `{form root}/CA_TYPE_OTHER/@VALUE` |
| Activity | `{form root}/CA_ACTIVITY/@VALUE` |
| Activity (other) | `{form root}/CA_ACTIVITY_OTHER/@VALUE` |

> R2.0.9 does not include a purchasing-on-behalf block; those paths are left empty.

#### Object of Contract

Base path: `{form root}/OBJECT_CONTRACT`

| Field | Path (relative to base) |
|---|---|
| Title | `TITLE` |
| Description | `SHORT_DESCR` |
| Total value | `VAL_TOTAL` |
| Currency | `VAL_TOTAL/@CURRENCY` |

#### Award Contract *(relative to each `AWARD_CONTRACT` element)*

| Field | Path |
|---|---|
| Contract number | `AWARD_CONTRACT/CONTRACT_NO` |
| Contract title | `AWARD_CONTRACT/TITLE/P` |
| Lot number | `AWARD_CONTRACT/LOT_NO` |
| Award date | `AWARD_CONTRACT/AWARDED_CONTRACT/DATE_CONCLUSION_CONTRACT` |
| Contractor base (primary) | `AWARD_CONTRACT/AWARDED_CONTRACT/CONTRACTORS/CONTRACTOR/ADDRESS_CONTRACTOR` |
| Contractor base (fallback) | `AWARD_CONTRACT/AWARDED_CONTRACT/CONTRACTOR/ADDRESS_CONTRACTOR` |
| Contractor name | `{contractor base}/OFFICIALNAME` |
| Contractor National ID | `{contractor base}/NATIONALID` |
| Contractor address | `{contractor base}/ADDRESS` |
| Contractor town | `{contractor base}/TOWN` |
| Contractor postal code | `{contractor base}/POSTAL_CODE` |
| Contractor country | `{contractor base}/COUNTRY/@VALUE` |
| Contractor point of contact | `{contractor base}/CONTACT_POINT` |
| Contractor phone | `{contractor base}/PHONE` |
| Contractor email | `{contractor base}/E_MAIL` |
| Contract value parent | `AWARD_CONTRACT/AWARDED_CONTRACT/VALUES` |
| Contract amount | `AWARD_CONTRACT/AWARDED_CONTRACT/VALUES/VAL_TOTAL` |
| Contract currency | `AWARD_CONTRACT/AWARDED_CONTRACT/VALUES/VAL_TOTAL/@CURRENCY` |

> **Contractor fallback.** Some R2.0.9 files wrap contractors inside a `CONTRACTORS/CONTRACTOR` element; others use a bare `CONTRACTOR`. Both paths are tried via `firstTextOrError` / `firstAttr`.

#### Complementary Information

Base path: `{form root}/COMPLEMENTARY_INFO`

| Field | Path (relative to base) |
|---|---|
| Additional information | `INFO_ADD` |
| EU project | *(not extracted for R2.0.9)* |
| Notice dispatch date | `DATE_DISPATCH_NOTICE` |
| Appeal body name | `ADDRESS_REVIEW_BODY/OFFICIALNAME` |
| Justification | *(empty path — not applicable for F03)* |

---

### R2.0.9 — Form F15 (VEAT Notice)

Form root: `FORM_SECTION/F15_2014`

#### Contracting Authority

Base path: `{form root}/CONTRACTING_BODY/ADDRESS_CONTRACTING_BODY`

Same leaf fields as R2.0.9 F03 (see above).

#### Activity and Type

Same paths directly under `{form root}` as R2.0.9 F03.

#### Object of Contract

Base path: `{form root}/OBJECT_CONTRACT`

Same leaf fields as R2.0.9 F03.

#### Award Contract *(relative to each `AWARD_CONTRACT` element)*

Same structure as R2.0.9 F03 except the contract value paths differ:

| Field | Path |
|---|---|
| Contract value parent | `AWARD_CONTRACT/AWARDED_CONTRACT` |
| Contract amount | `AWARD_CONTRACT/AWARDED_CONTRACT/VAL_TOTAL` |
| Contract currency | `AWARD_CONTRACT/AWARDED_CONTRACT/VAL_TOTAL/@CURRENCY` |

#### Complementary Information

Base path: `{form root}/COMPLEMENTARY_INFO`

| Field | Path |
|---|---|
| Additional information | `{base}/INFO_ADD` |
| Notice dispatch date | `{base}/DATE_DISPATCH_NOTICE` |
| Appeal body name | `{base}/ADDRESS_REVIEW_BODY/OFFICIALNAME` |
| Justification | `{form root}/PROCEDURE/DIRECTIVE_2014_24_EU/PT_NEGOTIATED_WITHOUT_PUBLICATION/D_JUSTIFICATION` |

---

## XML Path DSL

The `XMLPath` DSL in `repository/xml/XMLPath.scala` lets you compose paths in Scala code. All path constants in the `xpath/` package use this DSL.

### Building paths

```scala
import io.github.khanr1.tedawardparser.repository.xml.{XMLPath, QName}

// Simple element path
val p = XMLPath("root", "items", "item")          // root/items/item

// Append with /
val p2 = XMLPath("root") / "items" / "item"       // root/items/item

// Attribute (trailing step)
val pAttr = XMLPath("root") / "item" attr "id"    // root/item/@id

// Namespaced names
val ns = XMLPath("r") / QName("cac", "Party") / QName("cbc", "Title")

// Parse from a string
val p3 = XMLPath.parse("root/items/item/@id")     // parsed segments

// Index (0-based)
val first = (XMLPath("root") / "item").idx(0)     // root/item/[0]

// Attribute-equals predicate
val filtered = (XMLPath("root") / "item").whereAttr("type", "main")
                                                  // root/item[type='main']
```

### Navigating XML

All methods are extension methods on `scala.xml.Elem`, provided by `XMLPathUtils.*`:

```scala
import io.github.khanr1.tedawardparser.repository.xml.XMLPathUtils.*

val xml = <root><a><b id="7">hello</b></a></root>

// Read element text
xml.textAt(XMLPath.parse("root/a/b"))           // Some("hello")

// Read attribute
xml.attrAt(XMLPath.parse("root/a/b/@id"))       // Some("7")

// Read text, fail with ParserError if absent
xml.textAtOrError(XMLPath.parse("root/a/b"), "b-value")
// Right("hello")

// First matching among alternatives
xml.firstText(List(XMLPath.parse("root/x"), XMLPath.parse("root/a/b")))
// Some("hello")

// All children of a node
xml.childrenAt(XMLPath.parse("root/a"))         // List(<b id="7">hello</b>)

// Namespace-aware navigation
val ns = Ns(Map("cbc" -> "urn:example:cbc"))
val nsPath = XMLPath.parse("root/cbc:Title")
xml.textAt(nsPath, ns)
```

---

## Error model

`ParserError` (in `repository/xml/ParserError.scala`) is a sealed enum extending `NoStackTrace`:

| Variant | When |
|---|---|
| `MissingField(field, at)` | Element or attribute not found at the expected path |
| `InvalidFormat(field, expected, found, at)` | Value present but cannot be converted to the expected type |
| `MultipleValues(field, at)` | More than one value where exactly one was expected |
| `UnexpectedNode(node, at)` | An element was found that the parser does not know how to handle |
| `Unknown(why, at)` | Catch-all for other parse-related failures |

All `Raw` case class fields are typed as `Either[ParserError, T]`. The mapper (`NoticeMapper`) converts each to a `String` via `.getOrElse("")`, so every error is visible in the CSV output rather than silently dropped.

---

## CSV output format

`Notice.toCsvRows` produces one row per `AwardContract` (or a single placeholder row when the notice has no lots). Each row has the following columns **in order**:

| # | Column name |
|---|---|
| 1 | Journal Series |
| 2 | Journal Number |
| 3 | Publication Date |
| 4 | Notice ID |
| 5 | Document URIs |
| 6 | Languages |
| 7 | Buyer Country |
| 8 | Buyer URL |
| 9 | CPV |
| 10 | Reference Notice |
| 11 | Dispatch Date |
| 12 | Authority Type |
| 13 | Document Type |
| 14 | Contract Nature |
| 15 | Procedure Type |
| 16 | Regulation Scope |
| 17 | Bid Type |
| 18 | Award Criterion |
| 19 | Main Activity |
| 20 | Heading Code |
| 21 | Initiator Code |
| 22 | Directive |
| 23 | Form Type |
| 24 | CA Name |
| 25 | CA National ID |
| 26 | CA Address |
| 27 | CA Town |
| 28 | CA Postal Code |
| 29 | CA Country |
| 30 | CA Point of Contact |
| 31 | CA Phone |
| 32 | CA Email |
| 33 | Object Title |
| 34 | Object Description |
| 35 | Object Total Value |
| 36 | Object Total Currency |
| 37 | Additional Information |
| 38 | Relates to EU Project |
| 39 | Notice Dispatch Date |
| 40 | Appeal Body Name |
| 41 | Justification |
| 42 | Contract Number |
| 43 | Lot Number |
| 44 | Contract Title |
| 45 | Award Date |
| 46 | Contractor Name |
| 47 | Contractor National ID |
| 48 | Contractor Address |
| 49 | Contractor Town |
| 50 | Contractor Postal Code |
| 51 | Contractor Country |
| 52 | Contractor Point of Contact |
| 53 | Contractor Phone |
| 54 | Contractor Email |
| 55 | Contract Value |
| 56 | Contract Currency |

Values containing commas, quotes, or newlines are RFC-4180 quoted. Missing or failed fields render as their `ParserError` message (e.g. `Missing required field: Award Date at Some(…)`).

---

## Getting started

**Prerequisites:** JDK 17+, SBT 1.11+.

```bash
# Compile
sbt compile

# Run tests
sbt test

# Interactive REPL (useful for ad-hoc parsing)
sbt console
```

### Parsing a folder and writing CSV

```scala
import cats.effect.IO
import fs2.io.file.*
import fs2.text
import io.github.khanr1.tedawardparser.repository.file.XMLFileRepository
import io.github.khanr1.tedawardparser.repository.csv.NoticeToCSV

val inDir  = Path("path/to/ted-xml-files")
val outCsv = Path("output.csv")

XMLFileRepository
  .stream[IO](inDir)
  .through(NoticeToCSV.toCsvRows)
  .intersperse("\n")
  .through(fs2.text.utf8.encode)
  .through(Files[IO].writeAll(outCsv))
  .compile
  .drain
  .unsafeRunSync()
```

---

## Roadmap

- Implement UBL parsing.
- Add a CLI in `05-main` (input/output paths, fail-fast flag).
- Enrich error reporting with counts and per-file summaries.
- Frontend exploration in `06-frontend`.

---

## License

No license specified yet.
