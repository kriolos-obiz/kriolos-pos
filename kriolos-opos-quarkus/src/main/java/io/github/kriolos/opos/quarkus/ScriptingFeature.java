package io.github.kriolos.opos.quarkus;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.runtime.annotations.RegisterResources;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

/**
 * Configuration and Feature for BeanShell and Velocity script engine runtime reflection.
 *
 * @author KriolOS
 */
@RegisterResources(globs = {
    "bsh/**",
    "org/apache/velocity/**"
})
@RegisterForReflection(
    targets = {
        java.lang.System.class,
        java.io.PrintStream.class,
        java.lang.Math.class,
        javax.swing.JOptionPane.class,
        javax.swing.UIManager.class,
        javax.swing.JLabel.class,
        javax.swing.JDialog.class,
        javax.swing.plaf.FontUIResource.class,
        java.awt.Font.class,
        java.awt.Dimension.class,
        java.awt.Window.class,
        com.openbravo.pos.payment.JPaymentCashPos.class,
        com.openbravo.pos.payment.JPaymentCashPos.ScriptPaymentCash.class,
        com.openbravo.pos.payment.JPaymentSelect.class,
        com.openbravo.pos.payment.JPaymentSelectReceipt.class,
        com.openbravo.pos.payment.JPaymentSelectRefund.class,
        com.openbravo.pos.payment.JPaymentSelectCustomer.class,
        com.openbravo.format.Formats.class,
        com.openbravo.beans.DateUtils.class,
        com.openbravo.data.loader.Datas.class,
        com.openbravo.pos.spi.scripting.ScriptEngineManager.class,
        com.openbravo.pos.spi.scripting.ScriptEngineProvider.class,
        com.openbravo.pos.spi.scripting.ScriptEngineDefinition.class,
        com.openbravo.pos.spi.scripting.ScriptEngineType.class,
        com.openbravo.pos.scripting.ScriptFactory.class,
        com.openbravo.pos.scripting.ScriptEngine.class,
        com.openbravo.pos.scripting.ScriptException.class
    },
    classNames = {
        "io.github.kriolos.opos.scripting.bsvelocity.BsVelocityScriptEngineProvider",
        "io.github.kriolos.opos.scripting.bsvelocity.BsVelocityScriptEngine",
        "bsh.Interpreter",
        "bsh.XThis",
        "bsh.This",
        "bsh.NameSpace",
        "bsh.BshMethod",
        "bsh.BshClassManager",
        "bsh.Name",
        "bsh.Primitive",
        "bsh.Variable",
        "bsh.CallStack",
        "bsh.SimpleNode",
        "bsh.BSHBlock",
        "bsh.Reflect",
        "org.apache.velocity.app.VelocityEngine",
        "org.apache.velocity.runtime.RuntimeInstance",
        "org.apache.velocity.runtime.resource.ResourceManagerImpl",
        "org.apache.velocity.runtime.resource.ResourceCacheImpl",
        "org.apache.velocity.runtime.resource.loader.FileResourceLoader",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader",
        "org.apache.velocity.runtime.resource.loader.StringResourceLoader",
        "org.apache.velocity.util.introspection.UberspectImpl",
        "org.apache.velocity.runtime.parser.StandardParser"
    },
    methods = true,
    fields = true
)
public final class ScriptingFeature implements Feature {

    private static final Class<?>[] BEANSHELL_RUNTIME_TARGETS = {
        java.lang.System.class,
        java.io.PrintStream.class,
        java.lang.Math.class,
        javax.swing.JOptionPane.class,
        javax.swing.UIManager.class,
        javax.swing.JLabel.class,
        javax.swing.JDialog.class,
        javax.swing.plaf.FontUIResource.class,
        java.awt.Font.class,
        java.awt.Dimension.class,
        java.awt.Window.class,
        com.openbravo.pos.payment.JPaymentCashPos.class,
        com.openbravo.pos.payment.JPaymentCashPos.ScriptPaymentCash.class,
        com.openbravo.format.Formats.class,
        com.openbravo.beans.DateUtils.class,
        com.openbravo.data.loader.Datas.class,
        com.openbravo.pos.spi.scripting.ScriptEngineManager.class,
        com.openbravo.pos.spi.scripting.ScriptEngineProvider.class,
        com.openbravo.pos.spi.scripting.ScriptEngineDefinition.class,
        com.openbravo.pos.spi.scripting.ScriptEngineType.class,
        com.openbravo.pos.scripting.ScriptFactory.class,
        com.openbravo.pos.scripting.ScriptEngine.class,
        com.openbravo.pos.scripting.ScriptException.class
    };

    private static final String[] SCRIPTING_PACKAGES = {
        "bsh/",
        "org/apache/velocity/",
        "io/github/kriolos/opos/scripting/"
    };

    public ScriptingFeature() {
        // Required default constructor for GraalVM Feature SPI
    }

    @Override
    public String getDescription() {
        return "BeanShell and Velocity scripting runtime reflection configuration";
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        for (Class<?> clazz : BEANSHELL_RUNTIME_TARGETS) {
            RuntimeReflection.register(clazz);
            try {
                RuntimeReflection.register(clazz.getConstructors());
                RuntimeReflection.register(clazz.getDeclaredConstructors());
            } catch (Throwable ignored) {
            }
            try {
                RuntimeReflection.register(clazz.getFields());
                RuntimeReflection.register(clazz.getDeclaredFields());
            } catch (Throwable ignored) {
            }
            try {
                for (Method method : clazz.getMethods()) {
                    RuntimeReflection.register(method);
                }
                for (Method method : clazz.getDeclaredMethods()) {
                    RuntimeReflection.register(method);
                }
            } catch (Throwable ignored) {
            }
        }

        // Dynamically discover and register all BeanShell, Velocity, and scripting engine classes
        int scannedCount = registerClasspathComponents(access);
        System.out.printf("[ScriptingFeature] Registered %d JDK/POS targets, scanned %d BeanShell and Velocity classes for reflection.%n",
            BEANSHELL_RUNTIME_TARGETS.length, scannedCount);
    }

    private int registerClasspathComponents(BeforeAnalysisAccess access) {
        int count = 0;
        for (Path path : access.getApplicationClassPath()) {
            File file = path.toFile();
            if (!file.isFile() || !file.getName().endsWith(".jar")) {
                continue;
            }
            try (JarFile jar = new JarFile(file)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    boolean isAnonymous = name.matches(".*\\$\\d+\\.class");
                    if (name.endsWith(".class") && !isAnonymous && isMatch(name)) {
                        String className = name.substring(0, name.length() - 6).replace('/', '.');
                        if (registerClass(access, className)) {
                            count++;
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return count;
    }

    private boolean isMatch(String name) {
        // Exclude obsolete AWT/Servlet utilities from BeanShell that reference removed JDK methods (e.g. Component.getPeer())
        if (name.startsWith("bsh/util/") || name.startsWith("bsh/servlet/")) {
            return false;
        }
        for (String prefix : SCRIPTING_PACKAGES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean registerClass(BeforeAnalysisAccess access, String className) {
        try {
            Class<?> clazz = access.findClassByName(className);
            if (clazz == null) {
                return false;
            }
            RuntimeReflection.register(clazz);
            try {
                RuntimeReflection.register(clazz.getConstructors());
                RuntimeReflection.register(clazz.getDeclaredConstructors());
            } catch (Throwable ignored) {
            }
            try {
                RuntimeReflection.register(clazz.getFields());
                RuntimeReflection.register(clazz.getDeclaredFields());
            } catch (Throwable ignored) {
            }
            try {
                for (Method method : clazz.getMethods()) {
                    RuntimeReflection.register(method);
                }
                for (Method method : clazz.getDeclaredMethods()) {
                    RuntimeReflection.register(method);
                }
            } catch (Throwable ignored) {
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
