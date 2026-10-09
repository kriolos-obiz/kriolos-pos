# Modern Java Conventions Rule

## 1. Naming Conventions (STRICT: No Hungarian Notation or Legacy Prefixes)
- **NO Hungarian Notation**: Never use prefixes such as `m_`, `m_s`, `m_b`, `m_d`, `m_i`, `s_`, `m_j`, `p_`, or similar Hungarian/legacy prefixes.
  - **Forbidden**: `m_sName`, `m_sID`, `m_bActive`, `m_dPrice`, `m_jText`
  - **Required**: `name`, `id`, `active`, `price`, `text`
- **Standard Identifier Casing**:
  - **Classes, Records, Interfaces, Enums**: `PascalCase` (e.g., `CategoryInfo`, `DataLogicProducts`, `CatalogService`).
  - **Methods, Fields, Parameters, Local Variables**: `camelCase` (e.g., `getProductInfo`, `categoryStockList`, `productId`).
  - **Constants (`static final`)**: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_WIDTH`, `PRODUCTS_ROW`).
- **Standard JavaBean Accessors**:
  - Getters: `getId()`, `getName()`, `isActive()` (for boolean).
  - Setters: `setId(String id)`, `setName(String name)`, `setActive(boolean active)`.
  - Legacy compatibility accessors (e.g., `getID()`, `setID(String id)`) may be provided as deprecated aliases only when preserving backward compatibility with legacy callers.

## 2. Modern Java Language Features (Java 17 / 21+)
- **Immutability & Records**:
  - Prefer Java `record` for immutable data transfer objects, query projections, tuples, and scan results.
- **Lambdas & Method References**:
  - Prefer lambda expressions and method references over verbose anonymous inner classes (e.g., `(DataRead dr) -> new UomInfo(dr.getString(1), dr.getString(2))`).
- **Standard Collections**:
  - Use `List.of()`, `Set.of()`, `Map.of()`, or `Collections.unmodifiableList(...)` instead of manually creating and mutating temporary collections when static data is needed.
- **Null Safety & Objects Utility**:
  - Use `Objects.requireNonNull(...)`, `Objects.equals(...)`, and `Objects.hash(...)`.
  - Use modern pattern matching (`instanceof` pattern matching and pattern switches) where applicable.
- **Logging**:
  - Use standard `System.Logger` or `java.util.logging.Logger` with modern conventions.

## 3. Domain & Package Cohesion (PIM & Catalog)
- **Product Information Management (PIM)**:
  - All domain entities, metadata, and data access logic that characterize a product (such as Categories, Units of Measure [UOM], Variants, and Core Item definitions) must reside in `com.openbravo.pos.pim`.
- **Single Responsibility**:
  - Decompose large monolithic DataLogic classes into focused, single-responsibility components (e.g., `DataLogicCategories`, `DataLogicUom`, `DataLogicProducts`) behind service interfaces.
