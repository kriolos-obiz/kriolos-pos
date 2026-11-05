/*
 * Copyright (C) 2026 KriolOS POS
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
package com.openbravo.pos.cash;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CashManagementService Port Test Suite")
class CashManagementServiceTest {

    @Test
    @DisplayName("CloseCashCommand should enforce invariants and defensive copying")
    void shouldEnforceCloseCashCommandInvariants() {
        Date now = new Date();

        assertThrows(NullPointerException.class,
                () -> new CloseCashCommand(null, 1, "money-1", now, 0),
                "Null host should throw NullPointerException");
        assertThrows(IllegalArgumentException.class,
                () -> new CloseCashCommand("", 1, "money-1", now, 0),
                "Empty host should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                () -> new CloseCashCommand("   ", 1, "money-1", now, 0),
                "Blank host should throw IllegalArgumentException");

        assertThrows(NullPointerException.class,
                () -> new CloseCashCommand("host-1", 1, null, now, 0),
                "Null money should throw NullPointerException");
        assertThrows(IllegalArgumentException.class,
                () -> new CloseCashCommand("host-1", 1, "", now, 0),
                "Empty money should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                () -> new CloseCashCommand("host-1", 1, "   ", now, 0),
                "Blank money should throw IllegalArgumentException");

        assertThrows(IllegalArgumentException.class,
                () -> new CloseCashCommand("host-1", 1, "money-1", now, -1),
                "Negative noSales should throw IllegalArgumentException");

        // Factory and getters
        CloseCashCommand cmd = CloseCashCommand.of("term-1", 5, "m-42", now, 2);
        assertEquals("term-1", cmd.host());
        assertEquals(5, cmd.sequence());
        assertEquals("m-42", cmd.money());
        assertEquals(now, cmd.dateEnd());
        assertEquals(2, cmd.noSales());

        // Defensive copy: mutating passed date does not alter record
        long time = now.getTime();
        now.setTime(time + 60000);
        assertEquals(time, cmd.dateEnd().getTime());

        // Defensive copy: mutating returned date does not alter record
        cmd.dateEnd().setTime(time + 120000);
        assertEquals(time, cmd.dateEnd().getTime());

        // Null dateEnd defaults to non-null current timestamp
        CloseCashCommand defaultDateCmd = CloseCashCommand.of("term-1", 1, "m-1", 0);
        assertNotNull(defaultDateCmd.dateEnd());
    }

    @Test
    @DisplayName("CashManagementServiceImpl should execute closeCash command and contract methods on database")
    void testCashManagementServiceDatabaseOperations() throws Exception {
        String dbName = "cash_mgmt_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try (Statement stmt = session.getConnection().createStatement()) {
            stmt.executeUpdate("CREATE TABLE closedcash ("
                    + "MONEY VARCHAR(255) PRIMARY KEY, "
                    + "HOST VARCHAR(255) NOT NULL, "
                    + "HOSTSEQUENCE INT NOT NULL, "
                    + "DATESTART TIMESTAMP NOT NULL, "
                    + "DATEEND TIMESTAMP, "
                    + "NOSALES INT DEFAULT 0)");
        }

        CashManagementService service = new CashManagementServiceImpl(session);

        Date startDate = new Date(System.currentTimeMillis() - 3600000);
        CashRegister cash = new CashRegister();
        cash.setMoney("money-session-1");
        cash.setHost("host-pos-1");
        cash.setHostsequence(1);
        cash.setStartDate(startDate);
        service.addCloseCash(cash);

        assertTrue(service.isCashActive("money-session-1"), "Cash session should be active when dateEnd is null");
        assertEquals(1, service.getCloseCashSequenceByHost("host-pos-1"));

        CashRegister openReg = service.getCloseCashByMoney("money-session-1");
        assertNotNull(openReg);
        assertEquals("host-pos-1", openReg.getHost());
        assertNull(openReg.getEndDate());

        // Execute closeCash command
        Date endDate = new Date();
        CloseCashCommand cmd = CloseCashCommand.of("host-pos-1", 1, "money-session-1", endDate, 4);
        service.closeCash(cmd);

        assertFalse(service.isCashActive("money-session-1"), "Cash session should not be active after closing");

        CashRegister closedReg = service.getCloseCashByMoney("money-session-1");
        assertNotNull(closedReg);
        assertNotNull(closedReg.getEndDate());
        assertEquals(4, closedReg.getNoSales());

        // Test legacy overload delegation with second session
        Date startDate2 = new Date();
        CashRegister cash2 = new CashRegister();
        cash2.setMoney("money-session-2");
        cash2.setHost("host-pos-1");
        cash2.setHostsequence(2);
        cash2.setStartDate(startDate2);
        service.addCloseCash(cash2);

        Date endDate2 = new Date();
        service.closeCash("host-pos-1", 2, "money-session-2", endDate2, 1);

        CashRegister closedReg2 = service.getCloseCashBySequence("host-pos-1", 2);
        assertNotNull(closedReg2);
        assertEquals(1, closedReg2.getNoSales());
    }
}
