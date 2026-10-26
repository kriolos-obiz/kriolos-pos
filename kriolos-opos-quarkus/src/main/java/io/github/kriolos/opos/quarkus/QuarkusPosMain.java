package io.github.kriolos.opos.quarkus;

import com.openbravo.pos.config.AppConfig;
import com.openbravo.pos.forms.SystemSettings;
import com.openbravo.pos.forms.WindowShell;
import io.quarkiverse.desktop.awt.DesktopStartupEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.WindowConstants;

/**
 * Quarkus Desktop entry point for KriolOS POS.
 *
 * <h2>Bootstrap Flow</h2>
 * <ol>
 *   <li>{@code Quarkus.run()} starts the CDI container (ArC) and fires {@link StartupEvent}.</li>
 *   <li>{@link #onStartup(StartupEvent)} runs <strong>off the EDT</strong> — safe for config loading,
 *       datasource validation, Liquibase migrations, background services.</li>
 *   <li>{@code quarkus-desktop-swing} fires {@link DesktopStartupEvent} <strong>on the AWT EDT</strong>,
 *       after all {@link StartupEvent} observers complete.</li>
 *   <li>{@link #onDesktopReady(DesktopStartupEvent)} creates and shows the {@link WindowShell} —
 *       <em>all Swing code runs here, strictly on the EDT</em>.</li>
 * </ol>
 *
 * <p>No {@code @QuarkusMain} is needed — {@code quarkus-desktop-swing} fires {@link DesktopStartupEvent}
 * automatically. The LookAndFeel is set by the extension before this event fires, via
 * {@code quarkus.desktop.swing.look-and-feel} in {@code application.properties}.
 *
 * <p>The original {@link com.openbravo.pos.forms.BootstrapPOS} entry point in {@code kriolos-opos-app}
 * remains untouched for plain-JVM execution.
 *
 * @author KriolOS
 */
@ApplicationScoped
public class QuarkusPosMain {

    private static final Logger LOGGER = Logger.getLogger(QuarkusPosMain.class.getName());

    /** Resolved after {@link #onStartup} runs. Passed to {@link WindowShell} on the EDT. */
    private AppConfig config;

    /**
     * Runs off the EDT during Quarkus startup, before the Swing window opens.
     *
     * <p>Safe to perform: config loading, DB connection validation, Liquibase migrations,
     * background thread pool initialization.
     *
     * @param event Quarkus startup lifecycle event
     */
    void onStartup(@Observes StartupEvent event) {
        LOGGER.log(Level.INFO, "KriolOS POS — Quarkus startup (off EDT)");

        String sysPropConfig = System.getProperty("pos.config.file");
        if (sysPropConfig != null && !sysPropConfig.isBlank()) {
            config = AppConfig.getInstance(new File(sysPropConfig));
        } else {
            config = AppConfig.getInstance();
        }
        config.load();

        LOGGER.log(Level.INFO, "Config loaded. Waiting for EDT to open the window...");
    }

    /**
     * Called by {@code quarkus-desktop-swing} <strong>on the AWT Event Dispatch Thread</strong>
     * once the Quarkus container is fully started.
     *
     * <p>All Swing component construction and window setup happens here.
     *
     * @param event Quarkus Desktop AWT/Swing startup event
     */
    void onDesktopReady(@Observes DesktopStartupEvent event) {
        LOGGER.log(Level.INFO, "KriolOS POS — Desktop ready (on EDT), opening main window...");

        SystemSettings.applySystemProperties(config);

        final WindowShell shell = new WindowShell(config);

        // MUST use DISPOSE_ON_CLOSE — EXIT_ON_CLOSE calls System.exit() on the EDT,
        // which terminates the Quarkus dev-mode hot-reload loop.
        // quarkus.desktop.awt.exit-on-last-window-closed=true handles clean shutdown.
        shell.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        shell.initFrame();

        LOGGER.log(Level.INFO, "KriolOS POS window opened.");
    }
}

