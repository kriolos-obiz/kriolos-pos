# EPIC-TYPESAFE: Multi-Agent Architecture & Engineering Guide

## 1. Governance & Leadership
- **Lead Software Architect & Scrum Master**: `antigravity`
- **Participating Engineers**: Collaborating AI Agents (e.g., GitHub Copilot, Gemini)
- **Coordination Manifest**: `.agents/coordination.json`

> [!IMPORTANT]
> All AI agents working on this repository operate under the direction of **Antigravity**. 
> Before picking up any task or modifying code, agents **MUST** inspect `.agents/coordination.json`, respect `active_locks`, register their own locks, branch appropriately, and run the test suite prior to claiming task completion.

---

## 2. Core Architectural Goal: End-to-End Type Safety
Eliminate legacy `Object[]` array anti-patterns across Swing UI panels, editors, and the data persistence layer by converting all domain pipelines to strongly typed Java models:
- **UI Panels**: `JPanelView`, `JPanelTable2<T>`, `BrowsableEditableData<T>`
- **Record Editors**: `EditorRecord<T>`
- **Persistence Layer**: `TableDefinition<T>`, `SaveProvider<T>`, `SerializerRead<T>`, `SerializerWrite<T>`
- **Domain Service APIs**: Java 17 immutable `record` commands (`<Action><Entity>Command`) with invariant validation.

---

## 3. Standard Migration Pattern

### 3.1 Legacy Anti-Pattern (Before)
```java
// Anti-Pattern: raw Object[] array mapping
TableDefinition t = new TableDefinition(s, "TAXES", ...);
SaveProvider sp = new DefaultSaveProvider(t);
EditorRecord editor = new TaxEditor(); // untyped Object[]
Object[] row = (Object[]) editor.createValue();
```

### 3.2 Industrial Type-Safe Pattern (After)
```java
// 1. Model / DTO with RowMapper / ParamWriter
public class TaxCategoryInfo implements Serializable {
    public static SerializerRead<TaxCategoryInfo> getSerializerRead() {
        return dr -> new TaxCategoryInfo(dr.getString(1), dr.getString(2));
    }
    public static SerializerWrite<TaxCategoryInfo> getSerializerWrite() {
        return (dp, info) -> {
            dp.setString(1, info.getId());
            dp.setString(2, info.getName());
        };
    }
}

// 2. DataLogic / Persistence Layer
public TableDefinition<TaxCategoryInfo> getTableTaxCategories() {
    return new TableDefinition<>(
        s,
        "TAXCATEGORIES",
        new String[] {"ID", "NAME"},
        new String[] {"ID", AppLocal.getIntString("Label.Name")},
        new Datas[] {Datas.STRING, Datas.STRING},
        new Formats[] {Formats.STRING, Formats.STRING},
        new int[] {0},
        TaxCategoryInfo.getSerializerRead(),
        TaxCategoryInfo.getSerializerWrite()
    );
}

// 3. UI Editor implementing EditorRecord<T>
public class TaxCategoryEditor extends JPanel implements EditorRecord<TaxCategoryInfo> {
    @Override
    public TaxCategoryInfo createValue() throws BasicException {
        return new TaxCategoryInfo(m_id, m_jName.getText());
    }
    
    @Override
    public void writeValueEdit(TaxCategoryInfo value) {
        m_id = value.getId();
        m_jName.setText(value.getName());
    }
}

// 4. Panel using typed TableDefinition & SaveProvider
public class TaxCategoryPanel extends JPanelTable2<TaxCategoryInfo> {
    @Override
    public ListProvider<TaxCategoryInfo> getListProvider() {
        return new ListProviderCreator<>(m_dlSales.getTableTaxCategories());
    }
    
    @Override
    public SaveProvider<TaxCategoryInfo> getSaveProvider() {
        return new DefaultSaveProvider<>(m_dlSales.getTableTaxCategories());
    }
}
```

---

## 4. Multi-Agent Task Distribution & Instructions

### Active Task (Under Execution by Lead Architect `antigravity`)
- **TASK-030 (`KP-140`)**: `Type-Safe UOM (Unit of Measure) Vertical Pipeline`
  - *Branch*: `feature/KP-140-typesafe-uom-pipeline`
  - *Status*: IN PROGRESS by `antigravity`
  - *Locked Paths*:
    - `kriolos-opos-base/.../DefaultSaveProvider.java`
    - `kriolos-opos-app/.../com/openbravo/pos/inventory/Uom*`
    - `kriolos-opos-app/.../com/openbravo/pos/inventory/DataLogicUom.java`

### Recommended First Pick for Collaborating AI Agent (e.g., Copilot)
- **TASK-031 (`KP-141`)**: `Type-Safe Tax Category & Customer Tax Category Vertical Pipeline`
  - *Target Branch*: `feature/KP-141-typesafe-tax-categories` (branch from latest `develop`)
  - *Allowed Paths*:
    - `kriolos-opos-domain/src/main/java/com/openbravo/pos/ticket/*`
    - `kriolos-opos-app/src/main/java/com/openbravo/pos/panels/TaxCategory*.java`
    - `kriolos-opos-app/src/main/java/com/openbravo/pos/panels/TaxCustCategory*.java`
    - `kriolos-opos-domain/src/main/java/com/openbravo/pos/forms/DataLogicSales.java` (only TaxCategory table definitions)
  - *Claim Protocol*:
    1. Update `.agents/coordination.json`: move `TASK-031` to `in_progress`, set `"agent": "<your-agent-name>"`, add your file locks to `active_locks`.
    2. Create branch `feature/KP-141-typesafe-tax-categories` from `develop`.
    3. Implement `EditorRecord<TaxCategoryInfo>` and `EditorRecord<TaxCustCategoryInfo>`.
    4. Verify tests: `export JAVA_HOME=/home/dev/.sdkman/candidates/java/17.0.12-tem; export PATH=$JAVA_HOME/bin:$PATH; ./mvnw test -pl kriolos-opos-domain,kriolos-opos-app`.
    5. Commit with Conventional Commits: `feat(tax): implement end-to-end typesafe pipeline for tax categories`.
    6. Release locks in `.agents/coordination.json` and move task to `completed`.

---

## 5. Build & Quality Verification
All collaborating agents must ensure that their branch passes:
```bash
export JAVA_HOME=/home/dev/.sdkman/candidates/java/17.0.12-tem
export PATH=$JAVA_HOME/bin:$PATH
./mvnw test
```
*Zero compilation warnings in modernized files, zero test failures, zero breaking changes to existing POS operational flows.*

