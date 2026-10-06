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
import com.openbravo.pos.ticket.TicketInfo;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalesScriptCoordinator Unit Tests")
class SalesScriptCoordinatorTest {

    @Test
    @DisplayName("evalScript returns warning MessageInf when resource is null or missing")
    void testEvalScript_MissingResource() {
        SalesScriptCoordinator coordinator = new SalesScriptCoordinator(key -> null, () -> null);

        Object resultNull = coordinator.evalScript(null, null);
        assertInstanceOf(MessageInf.class, resultNull);

        Object resultMissing = coordinator.evalScript(null, "missing.script");
        assertInstanceOf(MessageInf.class, resultMissing);
    }

    @Test
    @DisplayName("evalScript executes valid BeanShell script and returns evaluation value")
    void testEvalScript_Success() {
        Map<String, String> resources = new HashMap<>();
        resources.put("calc.script", "return 10 * 5;");

        SalesScriptCoordinator coordinator = new SalesScriptCoordinator(resources::get, () -> null);

        Object result = coordinator.evalScript(null, "calc.script");
        assertEquals(50, result);
    }

    @Test
    @DisplayName("evalScript injects ScriptArg bindings into script scope")
    void testEvalScript_WithArguments() {
        Map<String, String> resources = new HashMap<>();
        resources.put("greeting.script", "return \"Hello \" + user + \"! Code: \" + code;");

        SalesScriptCoordinator coordinator = new SalesScriptCoordinator(resources::get, () -> null);

        Object result = coordinator.evalScript(
                null,
                "greeting.script",
                new ScriptArg("user", "Dev"),
                new ScriptArg("code", 42)
        );

        assertEquals("Hello Dev! Code: 42", result);
    }

    @Test
    @DisplayName("evalScript catches ScriptException gracefully and returns MessageInf")
    void testEvalScript_SyntaxError() {
        Map<String, String> resources = new HashMap<>();
        resources.put("broken.script", "invalid java syntax {{{ ");

        SalesScriptCoordinator coordinator = new SalesScriptCoordinator(resources::get, () -> null);

        Object result = coordinator.evalScript(null, "broken.script");
        assertInstanceOf(MessageInf.class, result);
    }

    @Test
    @DisplayName("executeEvent returns null when button config is missing or event unmapped")
    void testExecuteEvent_Unmapped() {
        SalesScriptCoordinator coordinator = new SalesScriptCoordinator(key -> null, () -> null);

        assertNull(coordinator.executeEvent(null, "ticket.show"));
    }

    @Test
    @DisplayName("evalScriptForExternalButton binds standard parameters and calls onAfterEval")
    void testEvalScriptForExternalButton() {
        Map<String, String> resources = new HashMap<>();
        resources.put("btn.script", "ticket.setTicketId(99); return true;");

        SalesScriptCoordinator coordinator = new SalesScriptCoordinator(resources::get, () -> null);
        TicketInfo ticket = new TicketInfo();

        boolean[] refreshed = new boolean[]{false};

        coordinator.evalScriptForExternalButton(
                null,
                "btn.script",
                ticket,
                "UserDev",
                this,
                () -> refreshed[0] = true
        );

        assertEquals(99, ticket.getTicketId());
        assertTrue(refreshed[0]);
    }
}
