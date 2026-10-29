/**
 * Quarkus Desktop Swing bootstrap and native-image configuration module for KriolOS POS.
 *
 * <p>This package integrates {@code kriolos-opos-app} with {@code quarkus-desktop-swing}
 * and ArC CDI, providing the {@link io.github.kriolos.opos.quarkus.QuarkusPosMain}
 * application lifecycle observer and modular GraalVM native image features:
 * {@link io.github.kriolos.opos.quarkus.KriolosPOSFeature},
 * {@link io.github.kriolos.opos.quarkus.AutomaticServiceLoaderFeature},
 * {@link io.github.kriolos.opos.quarkus.JasperReportsFeature},
 * {@link io.github.kriolos.opos.quarkus.HardwareJNIFeature},
 * {@link io.github.kriolos.opos.quarkus.ScriptingFeature}, and
 * {@link io.github.kriolos.opos.quarkus.UIThemeFeature}.
 *
 * @author KriolOS
 */
package io.github.kriolos.opos.quarkus;

