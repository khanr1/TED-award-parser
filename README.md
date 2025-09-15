# TED Award Parser

Parse award information from European public procurement notices (TED exports) and produce a normalized, CSV‑friendly stream of results.

This project focuses on extracting award data from TED XML exports (legacy R2.0.8 and newer R2.0.9) and, in the future, UBL. It provides a small XML navigation DSL, robust parsers for TED exports, and utilities to turn parsed results into CSV.

Useful reference: https://github.com/OP-TED/eForms-SDK/blob/develop/notice-types/notice-types.json

## Overview

The parser builds a stream of “partial” notices where each field can either carry a parsed value or a structured error explaining what was missing or malformed. This makes the pipeline resilient to heterogeneous source data while keeping errors visible in the output (e.g., CSV).

What we extract per notice and lot:

- Notice ID: OJS notice number.
- Publication date: `yyyyMMdd`.
- Contracting authority: name and ISO country code.
- Lot/Contract ID: contract or lot identifier.
- Title: lot or contract title.
- Description: short description.
- Value + Currency: awarded amount and currency.
- Awarded supplier: supplier name and country code (optional).
- Justification: when applicable (e.g., negotiated without prior publication/competition).

Supported inputs:

- TED Export R2.0.8 (legacy) — implemented.
- TED Export R2.0.9 (current) — implemented.
- UBL — scaffolding present, parsing to be implemented.

## Architecture

This repository follows a modular (clean architecture) layout:

- `01-domain`: Pure domain types (e.g., `Notice`, `TenderLot`, `ContractingAuthority`, `Country`). No I/O.
- `02-core`: Application services and models for working with partial/streamed data and CSV transformation.
- `03-delivery`: Adapters for exposing the application (currently a placeholder).
- `04-persistence`: Input adapters and parsers for XML files.
  - XML DSL: `XMLPath`, `XMLPathUtils`.
  - Parsers: `parsers.r208.TedExportR208`, `parsers.r209.TedExportR209`, `parsers.ubl.UBLParser` (TBD).
  - Repository: `repository.file.XMLFileRepository` to stream XML files from a directory and parse them.
- `05-main`: App entrypoint (currently a minimal placeholder).
- `06-frontend`: Scala.js frontend placeholder.

Key libraries: Cats Effect, FS2, Scala XML, Squants (money), Weaver (tests), Scala 3.3.6, SBT 1.11.0.

## Getting Started

Prerequisites:

- JDK 17+
- SBT 1.11+

Install and verify:

1) Launch SBT at project root:

```
sbt
```

2) Compile all modules:

```
compile
```

3) Run the test suite (parsers are well‑covered with sample TED XML fixtures):

```
test
```

## Quickstart: Parse a folder and write CSV

You can stream any folder containing TED XML files and export a CSV.

Example (run inside `sbt console` or as a small `Main`):

```scala
import cats.effect.*
import cats.syntax.all.*
import fs2.io.file.*
import fs2.text
import io.github.khanr1.tedawardparser.repository.file.XMLFileRepository
import io.github.khanr1.tedawardparser.service.NoticeService
import io.github.khanr1.tedawardparser.models.NoticeToCSV

val inDir  = Path("04-persistence/src/main/resources/TED_10-09-2025")
val outCsv = Path("test.csv")

val program = for {
  repo    <- IO.pure(XMLFileRepository.make[IO](inDir))
  service <- IO.pure(NoticeService.make[IO](repo))
  _ <- service.getAll
        .through(NoticeToCSV.toCSV)
        .intersperse("\n")
        .through(text.utf8.encode)
        .through(Files[IO].writeAll(outCsv))
        .compile
        .drain
} yield ()

program.unsafeRunSync()
```

This produces `test.csv` with a header and one row per lot (or a single row with placeholders when a notice has no lots).

Notes:

- The repository streams all `*.xml` files under the given directory using FS2 and processes them one by one.
- Parser selection is automatic: R2.0.9 when the root is `TED_EXPORT` with a `VERSION` attribute, otherwise R2.0.8; non‑TED roots fall back to a no‑op parser that returns structured errors.

## CSV Format

Header columns (in order):

1. Notice ID
2. Publication date
3. Contracting authority name
4. Contracting authority country code
5. Contract/Lot ID
6. Title
7. Description
8. Value (numeric amount)
9. Currency (ISO code)
10. Awarded supplier
11. Awarded supplier country code
12. Justification

Error handling: For each field the CSV shows either the parsed value or a human‑readable error message produced by the domain error (`Missing`, `Invalid`, `Unexpected`). This preserves observability across imperfect sources.

## Testing

Run all tests:

```
sbt test
```

The suites under `04-persistence/src/test/scala/...` validate TED parsers against multiple real‑world samples (R2.0.8 and R2.0.9), covering:

- OJS notice ID
- Publication date and its expected format
- Contracting authority (name, country)
- Lot/Contract IDs
- Titles and short descriptions
- Award values and currencies (with fallback paths where needed)
- Awarded supplier name and country
- Justification extraction

Sample inputs are located in:

- `04-persistence/src/test/resources/r208/`
- `04-persistence/src/test/resources/r209/`
- Additional larger sample set: `04-persistence/src/main/resources/TED_10-09-2025/`

## Module Details

- XML DSL: `04-persistence/src/main/scala/.../parsers/XMLPath.scala` and `XMLPathUtils.scala` implement a minimal, composable path representation with element, attribute, index, and simple attribute‑equals predicate segments.
- R2.0.8 Parser: `.../parsers/r208/TedExportR208.scala` with path constants in `r208/R208Path.scala`.
- R2.0.9 Parser: `.../parsers/r209/TedExportR209.scala` with path constants in `r209/R209Path.scala`.
- Parser selection: `.../repository/file/ParserSelect.scala` chooses the parser from the XML root.
- File repository: `.../repository/file/XMLFileRepository.scala` streams files and builds `PartialNotice` values.
- CSV: `02-core/src/main/scala/.../models/NoticeToCSV.scala` turns `PartialNotice` into CSV rows.

## Roadmap

- Implement UBL parsing (`parsers/ubl/UBLParser.scala`).
- Add a CLI in `05-main` to accept input/output paths and flags (e.g., schema hints, fail‑fast vs. tolerant).
- Enrich error reporting and metrics.
- Frontend exploration in `06-frontend`.

## Contributing

Issues and PRs are welcome. Please keep changes focused and aligned with the existing module layout. Run formatting and tests before submitting.

## License

No license specified yet.
