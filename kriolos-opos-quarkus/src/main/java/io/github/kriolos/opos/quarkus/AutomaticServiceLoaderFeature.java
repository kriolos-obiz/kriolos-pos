package io.github.kriolos.opos.quarkus;

import io.quarkus.runtime.annotations.RegisterResources;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.RuntimeResourceAccess;

/**
 * GraalVM Native Image Feature that automatically discovers and registers SPI
 * service providers from {@code META-INF/services/*} and JasperReports extension
 * factories across application classpath JARs at build time.
 *
 * <p>Uses an {@code ALLOW_LIST} and {@code DISALLOW_LIST} to focus registration
 * on application domain, reporting, hardware, look-and-feel, and desktop components while
 * preventing framework-internal or optional providers (e.g. Hibernate, Quarkus internals)
 * from being erroneously registered for linking.
 *
 * @author KriolOS
 */
@RegisterResources(globs = {
    "META-INF/services/**"
})
public final class AutomaticServiceLoaderFeature implements Feature {

    /**
     * Package and class prefixes explicitly allowed for SPI reflection registration.
     */
    public static final List<String> DEFAULT_ALLOW_LIST = List.of(
        "com.openbravo.",
        "io.github.kriolos.",
        "net.sf.jasperreports.",
        "org.apache.poi.",
        "com.lowagie.",
        "com.github.librepdf.",
        "com.formdev.",
        "org.jdesktop.",
        "bsh.",
        "org.apache.velocity.",
        "gnu.io.",
        "org.usb4java.",
        "javax.usb.",
        "org.krysalis.barcode4j.",
        "com.google.zxing.",
        "com.ctc.wstx.stax.",
        "org.codehaus.stax2.",
        "com.fasterxml."
    );

    /**
     * Package and class prefixes explicitly disallowed from SPI reflection registration.
     * These represent framework internals or optional modules managed by Quarkus or GraalVM.
     */
    public static final List<String> DEFAULT_DISALLOW_LIST = List.of(
        "org.hibernate.",
        "jakarta.persistence.",
        "org.jboss.",
        "io.quarkus.",
        "com.oracle.svm.",
        "jdk.internal.",
        "sun.",
        "org.hsqldb.",
        "com.ctc.wstx.shaded.",
        "com.ctc.wstx.msv."
    );

    private final List<String> allowList = new ArrayList<>(DEFAULT_ALLOW_LIST);
    private final List<String> disallowList = new ArrayList<>(DEFAULT_DISALLOW_LIST);

    public AutomaticServiceLoaderFeature() {
        String extraAllow = System.getProperty("kriolos.native.spi.allow");
        if (extraAllow != null && !extraAllow.isBlank()) {
            Arrays.stream(extraAllow.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(allowList::add);
        }
        String extraDisallow = System.getProperty("kriolos.native.spi.disallow");
        if (extraDisallow != null && !extraDisallow.isBlank()) {
            Arrays.stream(extraDisallow.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(disallowList::add);
        }
    }

    @Override
    public String getDescription() {
        return "Automatic discovery and reflection registration for META-INF/services SPI providers";
    }

    public List<String> getAllowList() {
        return List.copyOf(allowList);
    }

    public List<String> getDisallowList() {
        return List.copyOf(disallowList);
    }

    public boolean isAllowed(String className) {
        if (className == null || className.isBlank()) {
            return false;
        }
        for (String disallowed : disallowList) {
            if (className.startsWith(disallowed)) {
                return false;
            }
        }
        if (allowList.isEmpty() || allowList.contains("*")) {
            return true;
        }
        for (String allowed : allowList) {
            if (className.startsWith(allowed)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDisallowedService(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            return true;
        }
        for (String disallowed : disallowList) {
            if (serviceName.startsWith(disallowed)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        // Defer ServiceLoader registry classes to runtime so providers are evaluated at execution time
        RuntimeClassInitialization.initializeAtRunTime(
            "com.openbravo.pos.spi.scripting.ScriptEngineManager",
            "com.openbravo.pos.ui.api.POSThemeManager",
            "com.openbravo.pos.spi.hardware.PeripheralManager"
        );

        int registeredProviders = 0;
        int skippedProviders = 0;
        int serviceFilesCount = 0;
        int jasperFactoriesCount = 0;
        Module module = AutomaticServiceLoaderFeature.class.getModule();

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

                    // 1. Process standard Java ServiceLoader descriptors: META-INF/services/*
                    if (name.startsWith("META-INF/services/") && !entry.isDirectory()) {
                        String serviceName = name.substring("META-INF/services/".length());
                        if (isDisallowedService(serviceName)) {
                            continue;
                        }
                        serviceFilesCount++;
                        try {
                            RuntimeResourceAccess.addResource(module, name);
                        } catch (Throwable ignored) {
                        }
                        registerClass(access, serviceName);
                        try (InputStream in = jar.getInputStream(entry);
                             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                line = line.trim();
                                int comment = line.indexOf('#');
                                if (comment >= 0) {
                                    line = line.substring(0, comment).trim();
                                }
                                if (!line.isEmpty()) {
                                    if (isAllowed(line)) {
                                        if (registerClass(access, line)) {
                                            registeredProviders++;
                                        }
                                    } else {
                                        skippedProviders++;
                                    }
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    }

                    // 2. Process JasperReports extension properties: jasperreports_extension.properties
                    if ("jasperreports_extension.properties".equals(name)) {
                        try (InputStream in = jar.getInputStream(entry)) {
                            Properties props = new Properties();
                            props.load(in);
                            for (String key : props.stringPropertyNames()) {
                                if (key.startsWith("net.sf.jasperreports.extension.")) {
                                    String rawVal = props.getProperty(key);
                                    if (rawVal != null && !rawVal.trim().isEmpty()) {
                                        for (String item : rawVal.split(",")) {
                                            String cls = item.trim();
                                            if (!cls.isEmpty() && isAllowed(cls)) {
                                                if (registerClass(access, cls)) {
                                                    jasperFactoriesCount++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        System.out.printf(
            "[AutomaticServiceLoaderFeature] Auto-discovered %d SPI providers (from %d service files, %d skipped by filters) and %d JasperReports extension factories.%n",
            registeredProviders, serviceFilesCount, skippedProviders, jasperFactoriesCount
        );
    }

    private boolean registerClass(BeforeAnalysisAccess access, String className) {
        try {
            Class<?> clazz = access.findClassByName(className);
            if (clazz == null) {
                return false;
            }
            // Verify class linkage and dependencies exist before registering for native reflection
            try {
                clazz.getDeclaredConstructors();
                clazz.getDeclaredMethods();
                clazz.getDeclaredFields();
            } catch (LinkageError e) {
                // Class references types not present on classpath; skip registering
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
                    if ("windowsGetSerialPortsFromRegistry".equals(method.getName())) {
                        continue;
                    }
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
