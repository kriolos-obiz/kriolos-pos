package io.github.kriolos.opos.quarkus;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.runtime.annotations.RegisterResourceBundle;
import io.quarkus.runtime.annotations.RegisterResources;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

/**
 * GraalVM Native Image Feature managing FlatLaf themes and SwingX look-and-feel delegates.
 *
 * @author KriolOS
 */
@RegisterResourceBundle(bundleName = "com.formdev.flatlaf.resources.Bundle")
@RegisterResources(globs = {
    "com/formdev/flatlaf/**",
    "org/jdesktop/swingx/**",
    "io/github/kriolos/opos/ui/flatlaf/**/*.properties"
})
@RegisterForReflection(
    targets = {
        com.formdev.flatlaf.FlatLaf.class,
        com.formdev.flatlaf.FlatDarkLaf.class,
        com.formdev.flatlaf.FlatLightLaf.class,
        com.formdev.flatlaf.FlatIntelliJLaf.class,
        com.formdev.flatlaf.FlatDarculaLaf.class
    },
    classNames = {
        "com.formdev.flatlaf.swingx.FlatSwingXDefaultsAddon",
        "io.github.kriolos.opos.ui.flatlaf.FlatPOSThemeFactory",
        "io.github.kriolos.opos.ui.flatlaf.FlatPOSThemeFactory$SpectrumDarkVariant",
        "io.github.kriolos.opos.ui.flatlaf.FlatPOSThemeFactory$SpectrumLightVariant",
        "io.github.kriolos.opos.ui.nativefallback.NativeThemeFactory"
    },
    methods = true,
    fields = true
)
public final class UIThemeFeature implements Feature {

    private static final String[] THEME_PACKAGES = {
        "com/formdev/flatlaf/",
        "org/jdesktop/swingx/"
    };

    public UIThemeFeature() {
        // Required default constructor for GraalVM Feature SPI
    }

    @Override
    public String getDescription() {
        return "FlatLaf and SwingX look-and-feel native image configuration";
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        // Defer FlatLaf native library loading to runtime
        RuntimeClassInitialization.initializeAtRunTime("com.formdev.flatlaf.util.NativeLibrary");

        int registeredCount = 0;
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
                            registeredCount++;
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        System.out.println("[UIThemeFeature] Registered " + registeredCount + " FlatLaf and SwingX theme classes for reflection.");
    }

    private boolean isMatch(String name) {
        for (String prefix : THEME_PACKAGES) {
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

