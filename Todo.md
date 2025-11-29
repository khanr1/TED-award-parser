
- [ ] Move TED-source-specific types out of `01-domain` into persistence (keep domain source-agnostic).
- [ ] Clarify “partial” models: keep generic partials in `02-core` and versioned parser outputs under `04-persistence/tedexport/<version>`.
- [ ] Extract shared logic between r208 `f03` and `f15` decoders into `r208/common` to reduce duplication.
- [ ] Split `04-persistence` into XML DSL/decoder utilities vs. file I/O/parser selection to allow reuse in other transports.
- [ ] Add a small persistence testkit module for fixtures/helpers so integration tests don’t depend on full parsers.
- [ ] Implement UBL parser scaffold with a minimal happy-path test.
- [ ] Add a CLI in `05-main` (input/output paths, schema hints, fail-fast flag).
- [ ] Improve build ergonomics with aliases for fast module-scoped test runs (domain/core/persistence).
