# Javadoc, License Header, and Author Rules

## 1. Author Tag Policy
- The `@author` tag must strictly and exclusively be:
  ```java
  @author KriolOS
  ```
- Never use personal names, handles, or variations like "KriolOS Team", "Jack G", "Adrian", etc.

---

## 2. Minimum Javadoc Standards

Every Java component created or modified must maintain meaningful, non-boilerplate Javadoc documentation:

### A. `package-info.java`
- Every domain and service package must contain a `package-info.java` file.
- Provide a clear, architectural summary of the package's responsibilities, key domain entities, and role in the system.

### B. Classes, Interfaces, Records, and Enums
- Every type must have a top-level Javadoc block before the declaration.
- Must explain:
  - The purpose and core responsibility of the type.
  - Its role in the domain/architecture (e.g., service port, data access component, domain model, DTO).
  - Strictly use `@author KriolOS`.

### C. Public and Protected Methods
- Every `public` and `protected` method must have a Javadoc block describing:
  - What the method does (behavior and intent).
  - `@param` for each parameter explaining its meaning.
  - `@return` explaining the return value (if not `void`).
  - `@throws` explaining any checked or unchecked domain exceptions thrown.
- Avoid trivial or purely redundant repetition; document edge cases and invariants when present.

---

## 3. License Headers Policy

- **NO License Headers on New Files**: Do not add GPL or any file-level license header comment blocks to newly created files.
- **Opportunistic Removal**: Whenever editing, refactoring, or touching existing files, strip out legacy file-level license headers (e.g., standard GPL header comments at the top of the file).
- The repository-level license file (`LICENSE` / `COPYING`) serves as the legal notice for the project.
