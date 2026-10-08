//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.sales;

import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.ResourceService;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.TicketInfo;
import java.awt.Component;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Coordinates external script execution and dynamic toolbar events (Beanshell scripting).
 */
public class SalesScriptCoordinator {

    private static final Logger LOGGER = System.getLogger(SalesScriptCoordinator.class.getName());

    private final Function<String, String> xmlResourceResolver;
    private final Supplier<JPanelButtons> buttonConfigSupplier;

    public SalesScriptCoordinator(ResourceService resourceService, Supplier<JPanelButtons> buttonConfigSupplier) {
        this(resourceService != null ? resourceService::getResourceAsXML : resource -> null, buttonConfigSupplier);
    }

    @Deprecated
    public SalesScriptCoordinator(DataLogicSystem dlSystem, Supplier<JPanelButtons> buttonConfigSupplier) {
        this((ResourceService) dlSystem, buttonConfigSupplier);
    }

    public SalesScriptCoordinator(Function<String, String> xmlResourceResolver,
                                  Supplier<JPanelButtons> buttonConfigSupplier) {
        this.xmlResourceResolver = Objects.requireNonNull(xmlResourceResolver, "xmlResourceResolver cannot be null");
        this.buttonConfigSupplier = buttonConfigSupplier;
    }

    /**
     * Evaluates a script from a given XML resource with provided arguments.
     *
     * @param parent UI component for displaying alerts
     * @param resource XML resource identifier containing the Beanshell script
     * @param args variable arguments to bind into the script environment
     * @return script evaluation result, or a warning {@link MessageInf} on error
     */
    public Object evalScript(Component parent, String resource, ScriptArg... args) {
        if (resource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"));
            if (parent != null) {
                msg.show(parent);
            }
            return msg;
        }

        String scriptContent = xmlResourceResolver.apply(resource);
        if (scriptContent == null || scriptContent.isBlank()) {
            LOGGER.log(Level.WARNING, "Resource XML not found for script: " + resource);
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"));
            if (parent != null) {
                msg.show(parent);
            }
            return msg;
        }

        try {
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
            if (args != null) {
                for (ScriptArg arg : args) {
                    if (arg != null && arg.key() != null) {
                        script.put(arg.key(), arg.value());
                    }
                }
            }
            return script.eval(scriptContent);
        } catch (ScriptException ex) {
            LOGGER.log(Level.WARNING, "Exception on executing script with resource id: " + resource, ex);
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"), ex);
            if (parent != null) {
                msg.show(parent);
            }
            return msg;
        }
    }

    /**
     * Executes an event configured in the ticket toolbar (e.g., ticket.show, ticket.change, ticket.total, ticket.save).
     *
     * @param parent UI component for alerts
     * @param eventKey event key configured in XML
     * @param args script variable bindings
     * @return result of evaluation, or null if event is not configured
     */
    public Object executeEvent(Component parent, String eventKey, ScriptArg... args) {
        JPanelButtons btnConfig = buttonConfigSupplier != null ? buttonConfigSupplier.get() : null;
        if (btnConfig == null) {
            return null;
        }
        String resource = btnConfig.getEvent(eventKey);
        if (resource == null) {
            return null;
        }
        return evalScript(parent, resource, args);
    }

    /**
     * Evaluates a script triggered by an external button and executes callback.
     *
     * @param parent parent UI component for alerts
     * @param resource script resource ID
     * @param ticket active ticket
     * @param user current logged in user
     * @param salesContext sales screen facade
     * @param onAfterEval callback invoked after evaluation
     * @return evaluation result
     */
    public Object evalScriptForExternalButton(Component parent, String resource, TicketInfo ticket,
                                             Object user, Object salesContext, Runnable onAfterEval) {
        ScriptArg sa1 = new ScriptArg("ticket", ticket);
        ScriptArg sa2 = new ScriptArg("user", user);
        ScriptArg sa3 = new ScriptArg("sales", salesContext);

        Object result = evalScript(parent, resource, sa1, sa2, sa3);
        if (onAfterEval != null) {
            onAfterEval.run();
        }
        return result;
    }
}
