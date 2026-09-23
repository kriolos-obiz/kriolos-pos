# KriolOS POS Development & SDLC Rules

## 1. Component Identification Specification (URNs)
- **Mandatory Prefix**: All Swing components with a name (`setName(...)`) MUST start with `kriolos:`.
- **Format**: `kriolos:<domain>:<component-id>`
- **Examples**:
  - `kriolos:navigation:back`
  - `kriolos:navigation:forward`
  - `kriolos:navigation:title`
  - `kriolos:navigation:buttons-panel`

## 2. Swing UI & Lifecycle Guidelines
- **NetBeans Protected Blocks**: NEVER edit `// GEN-BEGIN:initComponents` or `// GEN-END:initComponents` manually.
- **View Transitions**: When activating views (e.g. CardLayout), always invoke `viewPanel.activate()` to ensure data loaders and subcomponents synchronize properly.
- **Global Title Panel**: Do not hide navigation or header panels based on empty titles; keep them permanently accessible unless explicitly configured otherwise.

## 3. UI Automation & Integration Testing (`kriolos-opos-automation`)
- **Autonomous Execution**: All tests must be 100% autonomous. NEVER expect manual user clicks during test runs.
- **Modal Dialog Handling**:
  - Modal dialogs (e.g., `JOptionPane`, `JMessageDialog`, warning dialogs) must be handled by dedicated Action Drivers using `invokeLater(button::doClick)` or `dispose()`.
  - Always run dialog triggers on a background thread when testing modal dialogs directly, allowing the AssertJ-Swing robot to find, screenshot, and dismiss them.
- **Multi-Instance Isolation**:
  - `machine.uniqueinstance` defaults to `false` in `BasePosRobotIT` so automation runs never conflict with background instances (e.g. IDE/NetBeans).
  - Single-instance behavior is explicitly tested in `PosInstanceManagerIT` using `InstanceManagerAction`.
- **Exit Action Protection**:
  - Use `StartPOS.setExitAction(Runnable)` during tests to intercept `System.exit` without killing the test runner JVM.

## 4. Agile Trunk-Based Integration & Atomic PRs (Rule 14.3)
- **CRITICAL CONSTRAINT - NEVER COMMIT OR PUSH DIRECTLY TO MAIN**:
  - Direct commits and direct pushes to `main` (or `master`) are STRICTLY FORBIDDEN.
  - All changes, fixes, and documentation must originate from a dedicated feature or bugfix branch (`feature/<name>` or `fix/<name>`).
  - Merging into `main` must ALWAYS happen via a reviewed GitHub Pull Request.
- **Never Stack Branches**: Complete one feature atomically.
- **Standard Flow**:
  1. Develop and verify locally on `feature/<feature-name>`.
  2. Push branch to remote: `git push -u origin feature/<feature-name>`.
  3. Create Pull Request and Merge into `main` on GitHub.
  4. Switch back to main and pull latest: `git checkout main && git pull origin main`.
  5. Create a fresh branch from updated main for the next task: `git checkout -b feature/<next-feature>`.

## 5. Bug Tracking
- Any platform-specific or deferred bugs (such as Wayland black dialog rendering `BUG-001`) must be documented in `docs/modules/guide-devel/pages/troubleshooting-known-issues.adoc` before moving to the next task.

## 6. Java Version Compatibility & CI/CD Matrix
- **Minimum JDK Baseline**: The minimum supported and compiled Java version is **JDK 17** (`<maven.compiler.release>17</maven.compiler.release>`).
- **CI/CD Matrix**: GitHub Actions verifies across both the **Minimum (JDK 17)** and the **Latest available JDK** (e.g. JDK 26).
- **Local Dev Trap Avoidance**:
  - Dev machines often run newer JDKs (e.g. JDK 21, JDK 25).
  - **NEVER** use language features, classes, or APIs introduced after Java 17 (e.g. `SequencedCollection`, string enhancements, post-17 pattern matching).
  - Code must strictly compile against Java 17 bytecode and API specifications (`--release 17`).
  - Before pushing to PR, verify that all added or modified code complies with JDK 17 to prevent CI/CD build failures.
