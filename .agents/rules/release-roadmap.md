# KriolOS POS Release Roadmap & Major Version Milestone

## 20th Anniversary Major Release: Version 20.0.0

- **Target Timeline**: End of year (Year-End Release).
- **Version Identifier**: **`20.0.0`** (bumped from 1.x / SNAPSHOT).
- **Historical Significance**:
  - Marks **20 years of evolution and lineage** from the project's inception (TinaPOS -> LibrePOS -> Openbravo POS -> KriolOS POS).
  - Designed as the **BIGGEST release in the project's history**.

---

## Architectural & Modernization Goals for 20.0.0

1. **Hardware Decoupling & SPI Modernization**:
   - Clean hardware SPI providers (`io.github.kriolos.opos:kriolos-opos-spi`, `kriolos-opos-hardware`).
   - Decoupled fiscal printer, barcode scanner, weighing scale, and customer display drivers.

2. **Core Domain-Driven Architecture**:
   - Full deconstruction of legacy monolithic `DataLogic` classes into single-responsibility components (e.g., `DataLogicProducts`, `DataLogicCategories`, `DataLogicUom`).
   - Service layer (`*Service`, `*ServiceImpl`) sitting cleanly above data access, returning strongly-typed domain records and models.
   - Zero leakage of low-level `com.openbravo.data.*` primitives (`Sentence`, `Row`, `TableDefinition`, `SaveProvider`) through Service interfaces.

3. **Complete Deprecation & Cleanup Lifecycle**:
   - Removal of transition adapter classes marked with `@Deprecated(since = "10.0.0", forRemoval = true)` (e.g., legacy `DataLogicPIM` bridge, legacy `UomInfo` alias in `inventory`).

4. **Modern Java & Clean Code Standards**:
   - Strict ban on Hungarian prefixes (`m_sName`).
   - Modern Java 17/21 records, immutable value objects, enums, and constants replacing magic strings/numbers and raw `Object[]` arrays.
   - Standard `@author KriolOS` attribution across all touched components.
