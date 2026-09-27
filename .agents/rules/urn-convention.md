# Rule 7.2 — Component Identification & setName() URN Anchoring

## 1. Specification & Syntax

Every Swing component created or modified during development or refactoring must receive a unique identifier via `.setName(...)` following the **URN specification**:

```
kriolos:<domain>:<component-or-action>
```

### Formatting Rules
1. **Mandatory Prefix**: Must begin with `kriolos:` in lowercase.
2. **Domain Segment (`<domain>`)**: Module or functional domain identifier in lowercase alphanumeric (e.g., `auth`, `navigation`, `sales`, `editor`, `modal`, `config`, `admin`, `payment`, `window`).
3. **Component or Action Segment (`<component-or-action>`)**: Specific UI control, action, or container descriptor formatted in **kebab-case** (lowercase words separated by single hyphens).
   - Component prefixes or suffixes should be semantic: `btn-*`, `txt-*`, `combo-*`, `lbl-*`, `scroll-*`, `panel`, `badge`, `progress-bar`, `status-label`.
   - Never use camelCase, PascalCase, or underscores in the URN segments.

### Canonical Examples
| Component / Action | Valid Rule 7.2 URN | Invalid (Non-Compliant) |
| :--- | :--- | :--- |
| Database selector combo | `kriolos:auth:combo-databases` | `comboDatabases`, `kriolos:auth:comboDatabases` |
| Activate database button | `kriolos:auth:btn-select-database` | `btnSelectDatabase`, `kriolos:auth:btnSelectDatabase` |
| Configure database button | `kriolos:auth:btn-configure-database` | `btnOpenConfiguration`, `kriolos:auth:btnConfigure` |
| Active database badge | `kriolos:auth:active-database-badge` | `activeBadge`, `kriolos:auth:activeBadge` |
| Progress bar | `kriolos:auth:progress-bar` | `progressBar`, `kriolos:auth:progressBar` |
| Real-time status label | `kriolos:auth:status-label` | `statusLabel`, `kriolos:auth:statusLabel` |
| Authentication panel | `kriolos:auth:panel` | `authPanel`, `kriolos:auth:panel_root` |
| Operator user list scroll pane | `kriolos:auth:scroll-users` | `usersLisScrollPane`, `kriolos:auth:scrollUsers` |
| Barcode / keyboard input | `kriolos:auth:txt-barcode-keys` | `m_txtKeys`, `kriolos:auth:txtBarcodeKeys` |
| Navigation back button | `kriolos:navigation:back` | `btnNavBack`, `backButton` |
| Navigation forward button | `kriolos:navigation:forward` | `btnNavForward`, `forwardButton` |
| Navigation title label | `kriolos:navigation:title` | `contentTitleLabel`, `navTitle` |
| Workspace container panel | `kriolos:workspace:panel` | `principalApp`, `workspacePanel` |
| Application shell | `kriolos:app:shell` | `rootApp`, `applicationShell` |
| Desktop window shell | `kriolos:window:shell` | `rootFrame`, `windowShell` |
| Text editor OK button | `kriolos:editor:btn-ok` | `btnOk`, `jcmdOK` |
| Payment cash amount field | `kriolos:sales:cash-received-amount` | `txtCashReceived`, `cash_amount` |

---

## 2. Implementation Guidelines

### Never Edit Inside NetBeans Protected GEN Blocks
- Do **NOT** set `.setName(...)` inside NetBeans generated code blocks (`// GEN-BEGIN:initComponents` to `// GEN-END:initComponents`).
- Always apply `.setName(...)` in:
  - `initDomainAdapters()` (recommended for views and workspace panels)
  - `initPanel()` or helper initialization methods
  - Constructor immediately after `initComponents()`

### Method Structure Example
```java
// Right after initComponents() in constructor or dedicated init method:
private void initDomainAdapters() {
    setName("kriolos:auth:panel");
    comboDatabases.setName("kriolos:auth:combo-databases");
    btnSelectDatabase.setName("kriolos:auth:btn-select-database");
    btnConfigureDatabase.setName("kriolos:auth:btn-configure-database");
    lblActiveDatabaseBadge.setName("kriolos:auth:active-database-badge");
    progressBar.setName("kriolos:auth:progress-bar");
    lblStatus.setName("kriolos:auth:status-label");
}
```

---

## 3. Automation Module Integration (`kriolos-opos-automation`)

The `kriolos-opos-automation` module relies on Rule 7.2 URNs for component discovery with AssertJ-Swing:

```java
// Finding components using Rule 7.2 URN
JComboBox<?> combo = robot.finder().findByName(frame, "kriolos:auth:combo-databases", JComboBox.class);
JButton btnActivate = robot.finder().findByName(frame, "kriolos:auth:btn-select-database", JButton.class);
```

For backward compatibility during incremental migrations, helper lookups can check the Rule 7.2 URN first, falling back to legacy component names:
```java
private <T extends Component> T findComponentByName(Container root, String urnName, String legacyName, Class<T> type) {
    try {
        return robot.finder().findByName(root, urnName, type);
    } catch (ComponentLookupException e) {
        return robot.finder().findByName(root, legacyName, type);
    }
}
```
