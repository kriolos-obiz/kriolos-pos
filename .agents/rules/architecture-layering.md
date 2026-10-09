# Architectural Layering and Independence Rules

## 1. Layer Hierarchy: Services Above DataLogics
- **Services are for the upper layer**:
  - Service interfaces and their implementations (e.g., `CatalogService`, `CatalogServiceImpl`) form the application/domain service layer sitting above data access.
  - A Service implementation coordinates domain use cases by accessing multiple focused, single-responsibility `DataLogic` components (for example, `CatalogServiceImpl` accesses `DataLogicProducts`, `DataLogicCategories`, and `DataLogicUom`).
  - Presentation and application layers (e.g., panels, editors, controllers) should consume Service interfaces rather than coupling directly to low-level DataLogics whenever possible.

---

## 2. Direct DataLogic Access by Upper Layer
- **Direct DataLogic calls permitted in specific situations**:
  - In certain situations (such as dedicated maintenance editors, low-level CRUD panels, or legacy Swing viewers), the upper layer may call a `DataLogic` directly.
- **Strictly Avoid Cross-Package Calls**:
  - Upper-layer components should avoid cross-package calls to DataLogics outside their domain boundary.
  - Cross-package calls must be minimized with very small exceptions only when there is no other immediate alternative, and must be tracked to be removed as modularization progresses.

---

## 3. No DataLogic-to-DataLogic Coupling
- **A DataLogic MUST NEVER call another DataLogic.**
- DataLogics are independent data-access layer components bound to a `Session` (JDBC connection).
- Each DataLogic is strictly responsible for its own domain entities and tables:
  - `DataLogicCategories` manages `categories`.
  - `DataLogicUom` manages `uom`.
  - `DataLogicProducts` manages `products`, `products_cat`, `products_com`, and `products_bundle`.
- Never compose, instantiate, or inject a DataLogic inside another DataLogic.
- If a DataLogic calls another DataLogic, the architectural boundary is violated.

---

## 4. No Service-to-Service Coupling
- **A Service MUST NEVER call another Service.**
- Services are top-level domain/application ports.
- If a service calls another service, something is architecturally wrong (cyclic dependencies, boundary leaks, or missing domain segregation).
- A Service communicates directly with its corresponding DataLogic(s) or repositories.
