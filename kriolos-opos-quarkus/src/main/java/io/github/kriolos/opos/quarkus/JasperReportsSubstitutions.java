package io.github.kriolos.opos.quarkus;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * GraalVM Native Image substitutions for optional third-party dependencies.
 */
public final class JasperReportsSubstitutions {

    private JasperReportsSubstitutions() {
    }

    @TargetClass(className = "org.apache.batik.bridge.RhinoInterpreterFactory")
    static final class Target_RhinoInterpreterFactory {

        @Substitute
        public org.apache.batik.script.Interpreter createInterpreter(java.net.URL documentURL, boolean svg12, org.apache.batik.script.ImportInfo imports) {
            // Mozilla Rhino is an optional Batik scripting dependency not bundled in KriolOS POS
            return null;
        }
    }
}

