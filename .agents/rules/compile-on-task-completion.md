# Rule: Mandatory Full Compilation at Task Completion

## Core Requirement
**At the end of implementing any task, refactoring, feature, or bugfix, you MUST compile the entire project / reactor before concluding work.**

1. **Reactor-Wide Compilation**:
   - Always run `mvn test-compile` (or `mvn clean test-compile`) across the workspace reactor to guarantee that changes in upstream modules (such as `kriolos-opos-domain` or `kriolos-opos-spi`) compile cleanly against all downstream modules (such as `kriolos-opos-app`, `kriolos-opos-hardware`, etc.).
2. **Zero Compilation Failures**:
   - Never conclude a task, create a commit, or return a completion message if any module in the reactor has compilation errors.
3. **Verify Before Declaring Done**:
   - Ensure both main sources and test sources (`test-compile`) are verified.
