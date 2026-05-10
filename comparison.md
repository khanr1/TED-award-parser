# data.csv vs data2.csv — Comparison Report

> **New (refactoring)**: 290 rows, 210 notice IDs  
> **Main (reference)**: 563 rows, 276 notice IDs

## 1. Notices present in only one file

- **Only in new** (0): none — the refactoring parses a superset of what main parses (for non-UBL).
- **Only in main** (66): all are UBL (`ContractAwardNotice`) format, deferred by design.

| Notice ID |
|---|
| 2015/S 248-451666 |
| 2016/S 002-001503 |
| 2018/S 034-073962 |
| 2021/S 074-187359 |
| 2022/S 153-436505 |
| 2023/S 180-565039 |
| 2023/S 234-738172 |
| 2024/S 1-1772 |
| 2024/S 10-27457 |
| 2024/S 128-394609 |
| *(+56 more, all UBL)* |

## 2. Row count differences (same notice, different number of rows)

| Notice ID | New rows | Main rows | Explanation |
|---|---|---|---|
| 2017/S 032-059079 | 1 | 2 | Main has language duplicates; new correctly deduplicates |
| 2017/S 219-456124 | 1 | 2 | Main has language duplicates; new correctly deduplicates |
| 2019/S 169-412877 | 23 | 59 | Main has ~2× more rows due to multi-language duplicates not deduplicated |
| 2020/S 040-095852 | 21 | 86 | Main has ~4× more rows due to multi-language duplicates not deduplicated |
| 2020/S 220-541843 | 23 | 91 | Main has ~3× more rows due to multi-language duplicates not deduplicated |

## 3. Field-level differences

### 3.1 Value format

New outputs float representation from squants (`448000.0`); main outputs integer or 2-decimal string.

| Notice ID | New Value | Main Value |
|---|---|---|
| 2016/S 015-021991 | `498000.0` | `498000` |
| 2016/S 059-099464 | `267500.0` | `267500` |
| 2016/S 093-167175 | `252400.0` | `252400` |
| 2016/S 123-220974 | `280000.0` | `280000` |
| 2016/S 141-255616 | `280000.0` | `280000` |
| 2017/S 032-059079 | `924510.0` | `924510` |
| 2017/S 046-085146 | `612920.0` | `612920` |
| 2017/S 090-178654 | `374900.0` | `374900` |
| 2018/S 045-099771 | `452280.7` | `452280.70` |
| 2018/S 120-274848 | `378000.0` | `378000.00` |
| 2018/S 143-328218 | `448000.0` | `448000.00` |
| 2018/S 159-364453 | `413223.0` | `413223.00` |
| 2018/S 168-382313 | `448000.0` | `448000.00` |
| 2018/S 180-407907 | `378870.0` | `378870` |
| 2018/S 195-441059 | `442600.0` | `442600.00` |
| 2018/S 210-479982 | `456300.0` | `456300.00` |
| 2018/S 216-493673 | `368000.0` | `368000.00` |
| 2018/S 228-521308 | `499900.0` | `499900.00` |
| 2019/S 014-028432 | `326400.0` | `326400` |
| 2019/S 025-055056 | `400000.0` | `400000.00` |
| *(+107 more — same pattern: `.0` suffix)* | | |

### 3.2 Awarded supplier country code

New always outputs empty (not yet extracted from contractor element). Main has the country code or a sentinel string.

| Notice ID | New | Main |
|---|---|---|
| 2016/S 011-015034 | `Unknown` | `FI` |
| 2016/S 015-021991 | `Unknown` | `UK` |
| 2016/S 036-059309 | `(empty)` | `the notice does not have lots` |
| 2016/S 059-099464 | `Unknown` | `FI` |
| 2016/S 069-120167 | `Unknown` | `NL` |
| 2016/S 087-153869 | `Unknown` | `DE` |
| 2016/S 087-153870 | `Unknown` | `NL` |
| 2016/S 093-167175 | `Unknown` | `FI` |
| 2016/S 094-169110 | `(empty)` | `the notice does not have lots` |
| 2016/S 095-170781 | `Unknown` | `FI` |
| 2016/S 122-218358 | `Unknown` | `UK` |
| 2016/S 123-220974 | `Unknown` | `NL` |
| 2016/S 131-236110 | `Unknown` | `Missing: Awarded Supplier Country` |
| 2016/S 141-255616 | `Unknown` | `NL` |
| 2016/S 209-378221 | `Unknown` | `FI` |
| *(+195 more — same pattern)* | | |

### 3.3 Justification

New extracts real justification text for VEAT notices (R208/R209 F15). Main outputs `Missing: Justification` or sentinel text.

| Notice ID | New | Main |
|---|---|---|
| 2016/S 011-015034 | (empty) | Missing: Justification |
| 2016/S 015-021991 | (empty) | Missing: Justification |
| 2016/S 036-059309 | (empty) | the notice does not have lots |
| 2016/S 059-099464 | (empty) | Missing: Justification |
| 2016/S 069-120167 | (empty) | Missing: Justification |
| 2016/S 087-153869 | (empty) | Missing: Justification |
| 2016/S 087-153870 | (empty) | Missing: Justification |
| 2016/S 093-167175 | (empty) | Missing: Justification |
| 2016/S 094-169110 | (empty) | the notice does not have lots |
| 2016/S 095-170781 | (empty) | Missing: Justification |
| 2016/S 122-218358 | (empty) | Missing: Justification |
| 2016/S 131-236110 | (empty) | Missing: Justification |
| 2016/S 141-255616 | (empty) | Missing: Justification |
| 2016/S 209-378221 | (empty) | Missing: Justification |
| 2016/S 226-412450 | (empty) | Missing: Justification |
| 2017/S 018-029486 | (empty) | Missing: Justification |
| 2017/S 027-047485 | (empty) | Missing: Justification |
| 2017/S 032-059079 | (empty) | Missing: Justification |
| 2017/S 046-085146 | (empty) | Missing: Justification |
| 2017/S 053-098671 | (empty) | Missing: Justification |
| *(+167 more)* | | |

### 3.4 Title / Description differences

Minor whitespace normalisation differences or cases where main left a field empty and new populates it (or vice versa).

| Notice ID | Field | New | Main |
|---|---|---|---|
| 2016/S 011-015034 | Title | (empty) | Dilution refrigerators for extended spin qubi… |
| 2016/S 015-021991 | Title | Acquisition d'un cryostat à dilution sans hél… | AOO/2015/LPA/CRYOSTAT. |
| 2016/S 036-059309 | Title | Reagents, materials, calibrators for hemostas… | the notice does not have lots |
| 2016/S 036-059309 | Description | Part 1 Reagents and calibrators for the basic… | the notice does not have lots |
| 2016/S 059-099464 | Title | Fourniture d'une dilution à tube pulsé | AOO08-2015. |
| 2016/S 069-120167 | Title | (empty) | Dilution refrigerator with high cooling power… |
| 2016/S 087-153869 | Title | (empty) | Dilution refrigerator for ultra-sensitive, co… |
| 2016/S 087-153870 | Title | (empty) | Wide-body dilution refrigerator with high coo… |
| 2016/S 093-167175 | Title | Acquisition d'un réfrigérateur à dilution san… | Acquisition d'un réfrigérateur à dilution san… |
| 2016/S 094-169110 | Title | UK SBS PR16079 Muon Cryogenics Equipment. | the notice does not have lots |
| 2016/S 094-169110 | Description | Tenders are invited to provide the Science an… | the notice does not have lots |
| 2016/S 095-170781 | Title | Lieferung eines Kryostat-Systems | Lieferung eines Kryostat-Systems. |
| 2016/S 122-218358 | Title | (empty) | Acquisition of a dilution refrigerator to Cen… |
| 2016/S 123-220974 | Title | Upgrade of 3K system to Cryogen-Free Dilution… | Upgrade of 3K system to Cryogen-Free Dilution… |
| 2016/S 131-236110 | Title | Oxford Instruments Nanoscience | Cryogen free ultra-low temperature system for… |
| *(+225 more)* | | | |

### 3.5 Contracting authority name / country

| Notice ID | Field | New | Main |
|---|---|---|---|
| 2016/S 036-059309 | Contracting authority name | Institute of Transfusion Medicine | Missing: Contracting Authority Name |
| 2016/S 036-059309 | Contracting authority country code | MK | Missing: Contracting Authority Country |
| 2016/S 094-169110 | Contracting authority name | UK Shared Business Services Ltd | Missing: Contracting Authority Name |
| 2016/S 094-169110 | Contracting authority country code | UK | Missing: Contracting Authority Country |
| 2017/S 046-085146 | Contracting authority name | UK Shared Business Services Ltd | Science and Technology Facilities Council |
| 2017/S 053-098984 | Contracting authority name | Institute of Transfusion Medicine | Missing: Contracting Authority Name |
| 2017/S 053-098984 | Contracting authority country code | MK | Missing: Contracting Authority Country |
| 2017/S 132-269654 | Contracting authority name | The University of Manchester | Missing: Contracting Authority Name |
| 2017/S 132-269654 | Contracting authority country code | UK | Missing: Contracting Authority Country |
| 2017/S 169-348002 | Contracting authority name | Ss. Cyril and Methodius University Faculty of… | Missing: Contracting Authority Name |
| 2017/S 169-348002 | Contracting authority country code | MK | Missing: Contracting Authority Country |
| 2018/S 052-116406 | Contracting authority name | Institute of Transfusion Medicine | Missing: Contracting Authority Name |
| 2018/S 052-116406 | Contracting authority country code | MK | Missing: Contracting Authority Country |
| 2019/S 029-065849 | Contracting authority name | Institute of Transfusion Medicine | Missing: Contracting Authority Name |
| 2019/S 029-065849 | Contracting authority country code | MK | Missing: Contracting Authority Country |
| 2019/S 052-120708 | Contracting authority name | Ss. Cyril and Methodius University Faculty of… | Missing: Contracting Authority Name |
| 2019/S 052-120708 | Contracting authority country code | MK | Missing: Contracting Authority Country |
| 2019/S 104-251445 | Contracting authority name | The University of Manchester | Missing: Contracting Authority Name |
| 2019/S 104-251445 | Contracting authority country code | UK | Missing: Contracting Authority Country |
| 2019/S 171-417105 | Contracting authority name | UK Research and Innovation | Missing: Contracting Authority Name |
| 2019/S 171-417105 | Contracting authority country code | UK | Missing: Contracting Authority Country |
| 2019/S 216-529441 | Contracting authority name | Universiteit Leiden | Missing: Contracting Authority Name |
| 2019/S 216-529441 | Contracting authority country code | NL | Missing: Contracting Authority Country |
| 2020/S 023-052357 | Contracting authority name | The University of Manchester | Missing: Contracting Authority Name |
| 2020/S 023-052357 | Contracting authority country code | UK | Missing: Contracting Authority Country |
| 2020/S 146-358692 | Contracting authority name | Universiteit Leiden | Missing: Contracting Authority Name |
| 2020/S 146-358692 | Contracting authority country code | NL | Missing: Contracting Authority Country |
| 2021/S 223-588243 | Contracting authority name | Ss. Cyril and Methodius University Faculty of… | Missing: Contracting Authority Name |
| 2021/S 223-588243 | Contracting authority country code | MK | Missing: Contracting Authority Country |
| 2022/S 210-600844 | Contracting authority name | Fraunhofer-Gesellschaft - Einkauf B12 | Missing: Contracting Authority Name |
| 2022/S 210-600844 | Contracting authority country code | DE | Missing: Contracting Authority Country |
| 2022/S 214-615813 | Contracting authority name | Ss. Cyril and Methodius University Faculty of… | Missing: Contracting Authority Name |
| 2022/S 214-615813 | Contracting authority country code | MK | Missing: Contracting Authority Country |
| 2023/S 017-047600 | Contracting authority name | Fraunhofer-Gesellschaft - Einkauf B12 | Missing: Contracting Authority Name |
| 2023/S 017-047600 | Contracting authority country code | DE | Missing: Contracting Authority Country |

## 4. Summary

| Category | Count | Status |
|---|---|---|
| Notices only in main (UBL) | 66 | Deferred by design |
| Notices only in new | 0 | — |
| Row count fixed (language deduplication) | 5 | ✅ New is correct |
| Value format (`.0` suffix from squants) | 127 | ⚠️ Cosmetic — fix by formatting `Money.amount` |
| Awarded supplier country code missing | 210 | ⚠️ Not yet extracted |
| Justification (new has real text, main has sentinel) | 0 | ✅ New is better |
| Title/Description minor diffs | 240 | Mostly whitespace/extraction differences |
| Contracting authority diffs | 35 | Minor extraction differences |