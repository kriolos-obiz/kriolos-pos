# Domain-Driven Design & Type-Safety Rules

## 1. Service Layer Signature Contracts
- **Domain Types Only**: The Service layer must receive and return domain models, and **NEVER raw `Object` or `Object[]`**.
- **No Persistence Leakage**: Service interfaces and implementations must **NOT return classes from `com.openbravo.data.*`** (such as `Sentence`, `SentenceList`, `SentenceExec`, `Row`, `TableDefinition`, `SaveProvider`, `ListProvider`).
  - Those persistence primitives belong strictly inside the low-level data access / DataLogic implementation layer.
  - Expose clean domain queries and commands on Services returning domain collections (e.g., `List<ProductInfoExt>`, `List<CategoryInfo>`, `Optional<ProductInfoExt>`).

---

## 2. Parameter Objects & Records
- **Parameter Records**: Any method with a large number of parameters (or receiving untyped `Object[]`) must define a strongly typed Java `record` (or domain class) to encapsulate the parameter data.
- **No Object Arrays**: Eliminate `Object[]` parameter arrays across services and modernized data logic APIs.

---

## 3. Base Class Modernization
- **Replace Raw Object/Object Array**: Legacy base classes or abstract abstractions using `Object` or `Object[]` (such as legacy data loader row representations or generic arrays) must be modernized to use proper generic types, domain classes, or Java `record`s.

---

## 4. Type Safety in DataLogic & Migration
- **Type-Safe DataLogics**: When migrating and refactoring DataLogics, change return types and method parameters to be strictly type-safe rather than `Object` or raw types.
- **Generics**: Use specific types like `List<ProductInfoExt>` instead of raw `List` or `List<Object>`.

---

## 5. Elimination of Magic Strings & Numbers
- **Constants and Enums**: Avoid the use of magic strings and magic numbers. Define proper `static final` constants, enums, or configuration classes with descriptive names.

