/*
 * Copyright (C) 2026 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.kriolos.opos.scripting.bsvelocity;

import bsh.Interpreter;
import com.openbravo.pos.spi.scripting.ScriptEngineDefinition;
import com.openbravo.pos.spi.scripting.ScriptEngineException;
import com.openbravo.pos.spi.scripting.ScriptEngineProvider;
import com.openbravo.pos.spi.scripting.ScriptEngineType;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ScriptEngineProvider} implementation that bundles BeanShell (for script execution)
 * and Apache Velocity (for template rendering) into a single provider.
 *
 * <p>This is the <em>current</em> production implementation registered via
 * {@code META-INF/services}. Future providers (QuickJS, Qute) will follow the same
 * {@link ScriptEngineProvider} contract without modifying any caller code.</p>
 *
 * <p>Registered engine IDs:</p>
 * <ul>
 *   <li>{@code "beanshell"} — {@link ScriptEngineType#SCRIPT}, extensions: {@code .bsh}</li>
 *   <li>{@code "velocity"}  — {@link ScriptEngineType#TEMPLATE}, extensions: {@code .vm}</li>
 * </ul>
 *
 * @author KriolOS
 * @since 1.0.0
 */
public final class BsVelocityScriptEngineProvider implements ScriptEngineProvider {

    private static final Logger LOGGER = Logger.getLogger(BsVelocityScriptEngineProvider.class.getName());

    // Engine IDs
    static final String ENGINE_BEANSHELL = "beanshell";
    static final String ENGINE_VELOCITY  = "velocity";

    // Supported extensions per engine
    private static final Set<String> BEANSHELL_EXTENSIONS = Set.of(".bsh");
    private static final Set<String> VELOCITY_EXTENSIONS  = Set.of(".vm");

    // Engine definition registry
    private static final Map<String, ScriptEngineDefinition> ENGINE_MAP = new LinkedHashMap<>();

    static {
        ENGINE_MAP.put(ENGINE_BEANSHELL, new ScriptEngineDefinition(
                ENGINE_BEANSHELL,
                "BeanShell 2.x",
                "Java-syntax scripting engine for POS button events and business rules.",
                ScriptEngineType.SCRIPT));

        ENGINE_MAP.put(ENGINE_VELOCITY, new ScriptEngineDefinition(
                ENGINE_VELOCITY,
                "Apache Velocity 2.x",
                "Template engine for receipt and report rendering using .vm templates.",
                ScriptEngineType.TEMPLATE));
    }

    // Lazily initialized Velocity engine (thread-safe via double-checked locking)
    private volatile VelocityEngine velocityEngine;

    // -------------------------------------------------------------------------
    // ScriptEngineProvider contract
    // -------------------------------------------------------------------------

    @Override
    public boolean hasEngine(String engineId) {
        return engineId != null && ENGINE_MAP.containsKey(engineId.toLowerCase());
    }

    @Override
    public Collection<ScriptEngineDefinition> getAvailableEngines() {
        return Collections.unmodifiableCollection(ENGINE_MAP.values());
    }

    @Override
    public Set<String> getSupportedExtensions(String engineId) {
        if (ENGINE_BEANSHELL.equalsIgnoreCase(engineId)) return BEANSHELL_EXTENSIONS;
        if (ENGINE_VELOCITY.equalsIgnoreCase(engineId))  return VELOCITY_EXTENSIONS;
        return Set.of();
    }

    @Override
    public Object execute(String engineId, String script, Map<String, Object> bindings)
            throws ScriptEngineException {
        requireEngine(engineId, ScriptEngineType.SCRIPT);
        return executeBeanShell(script, bindings);
    }

    @Override
    public String render(String engineId, String templateName, Map<String, Object> model)
            throws ScriptEngineException {
        requireEngine(engineId, ScriptEngineType.TEMPLATE);
        return renderVelocityTemplate(templateName, model);
    }

    @Override
    public String renderInline(String engineId, String templateSource, Map<String, Object> model)
            throws ScriptEngineException {
        requireEngine(engineId, ScriptEngineType.TEMPLATE);
        return renderVelocityInline(templateSource, model);
    }

    // -------------------------------------------------------------------------
    // BeanShell execution
    // -------------------------------------------------------------------------

    private Object executeBeanShell(String script, Map<String, Object> bindings)
            throws ScriptEngineException {
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.set("out", System.out);
            interpreter.set("err", System.err);
            if (bindings != null) {
                for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                    interpreter.set(entry.getKey(), entry.getValue());
                }
            }
            Object result = interpreter.eval(script);
            if (bindings != null) {
                try {
                    for (String varName : interpreter.getNameSpace().getVariableNames()) {
                        Object val = interpreter.get(varName);
                        if (val != null) {
                            bindings.put(varName, val);
                        }
                    }
                } catch (UnsupportedOperationException ignored) {
                    // bindings map was unmodifiable
                }
            }
            LOGGER.log(Level.FINE, "BeanShell script executed successfully.");
            return result;
        } catch (Exception ex) {
            throw new ScriptEngineException("BeanShell execution failed: " + ex.getMessage(), ex);
        }
    }

    // -------------------------------------------------------------------------
    // Velocity template rendering
    // -------------------------------------------------------------------------

    private VelocityEngine getVelocityEngine() {
        if (velocityEngine == null) {
            synchronized (this) {
                if (velocityEngine == null) {
                    VelocityEngine engine = new VelocityEngine();
                    engine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
                    engine.setProperty("resource.loader.classpath.class",
                            ClasspathResourceLoader.class.getName());
                    engine.init();
                    velocityEngine = engine;
                    LOGGER.log(Level.INFO, "Apache Velocity engine initialized.");
                }
            }
        }
        return velocityEngine;
    }

    private String renderVelocityTemplate(String templateName, Map<String, Object> model)
            throws ScriptEngineException {
        try {
            VelocityContext context = buildContext(model);
            StringWriter writer = new StringWriter();
            getVelocityEngine().mergeTemplate(templateName, "UTF-8", context, writer);
            return writer.toString();
        } catch (Exception ex) {
            throw new ScriptEngineException(
                    "Velocity template rendering failed for '" + templateName + "': " + ex.getMessage(), ex);
        }
    }

    private String renderVelocityInline(String templateSource, Map<String, Object> model)
            throws ScriptEngineException {
        try {
            VelocityContext context = buildContext(model);
            StringWriter writer = new StringWriter();
            getVelocityEngine().evaluate(context, writer, "inline", templateSource);
            return writer.toString();
        } catch (Exception ex) {
            throw new ScriptEngineException(
                    "Velocity inline rendering failed: " + ex.getMessage(), ex);
        }
    }

    private static VelocityContext buildContext(Map<String, Object> model) {
        VelocityContext context = new VelocityContext();
        if (model != null) {
            model.forEach(context::put);
        }
        return context;
    }

    // -------------------------------------------------------------------------
    // Guard helpers
    // -------------------------------------------------------------------------

    private void requireEngine(String engineId, ScriptEngineType requiredType)
            throws ScriptEngineException {
        if (!hasEngine(engineId)) {
            throw new ScriptEngineException(
                    "Engine '" + engineId + "' is not managed by BsVelocityScriptEngineProvider.");
        }
        ScriptEngineDefinition def = ENGINE_MAP.get(engineId.toLowerCase());
        if (def.type() != requiredType) {
            throw new ScriptEngineException(
                    "Engine '" + engineId + "' is of type " + def.type()
                    + " but operation requires " + requiredType + ".");
        }
    }
}
