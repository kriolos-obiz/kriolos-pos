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

package com.openbravo.data.loader;

import com.openbravo.basic.BasicException;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transaction Lifecycle & Universal Rollback Unit Tests")
class TransactionTest {

    private Session session;

    @BeforeEach
    void setUp() {
        session = Mockito.mock(Session.class);
    }

    @Test
    @DisplayName("Successful transaction executes begin, transact, and commit")
    void testCommitOnSuccess() throws BasicException, SQLException {
        Mockito.when(session.isTransaction()).thenReturn(false);

        Transaction<String> tx = new Transaction<>(session) {
            @Override
            protected String transact() throws BasicException {
                return "SUCCESS";
            }
        };

        String result = tx.execute();
        assertEquals("SUCCESS", result);

        InOrder inOrder = Mockito.inOrder(session);
        inOrder.verify(session).isTransaction();
        inOrder.verify(session).begin();
        inOrder.verify(session).commit();
        Mockito.verify(session, Mockito.never()).rollback();
    }

    @Test
    @DisplayName("BasicException triggers rollback and preserves original exception")
    void testRollbackOnBasicException() throws SQLException {
        Mockito.when(session.isTransaction()).thenReturn(false, true);

        BasicException expected = new BasicException("Domain validation failure");

        Transaction<Void> tx = new Transaction<>(session) {
            @Override
            protected Void transact() throws BasicException {
                throw expected;
            }
        };

        BasicException thrown = assertThrows(BasicException.class, tx::execute);
        assertSame(expected, thrown);

        InOrder inOrder = Mockito.inOrder(session);
        inOrder.verify(session).begin();
        inOrder.verify(session).rollback();
        Mockito.verify(session, Mockito.never()).commit();
    }

    @Test
    @DisplayName("RuntimeException triggers rollback and is rethrown directly")
    void testRollbackOnRuntimeException() throws SQLException {
        Mockito.when(session.isTransaction()).thenReturn(false, true);

        NullPointerException expected = new NullPointerException("Simulated unchecked NPE");

        Transaction<Void> tx = new Transaction<>(session) {
            @Override
            protected Void transact() throws BasicException {
                throw expected;
            }
        };

        NullPointerException thrown = assertThrows(NullPointerException.class, tx::execute);
        assertSame(expected, thrown);

        InOrder inOrder = Mockito.inOrder(session);
        inOrder.verify(session).begin();
        inOrder.verify(session).rollback();
        Mockito.verify(session, Mockito.never()).commit();
    }

    @Test
    @DisplayName("Error (e.g. AssertionError) triggers rollback and is rethrown directly")
    void testRollbackOnError() throws SQLException {
        Mockito.when(session.isTransaction()).thenReturn(false, true);

        AssertionError expected = new AssertionError("Simulated fatal error");

        Transaction<Void> tx = new Transaction<>(session) {
            @Override
            protected Void transact() throws BasicException {
                throw expected;
            }
        };

        AssertionError thrown = assertThrows(AssertionError.class, tx::execute);
        assertSame(expected, thrown);

        InOrder inOrder = Mockito.inOrder(session);
        inOrder.verify(session).begin();
        inOrder.verify(session).rollback();
        Mockito.verify(session, Mockito.never()).commit();
    }

    @Test
    @DisplayName("SQLException during commit triggers rollback and wraps in BasicException")
    void testRollbackOnCommitSQLException() throws SQLException {
        Mockito.when(session.isTransaction()).thenReturn(false, true);
        SQLException sqlEx = new SQLException("Connection lost during commit");
        Mockito.doThrow(sqlEx).when(session).commit();

        Transaction<Void> tx = new Transaction<>(session) {
            @Override
            protected Void transact() throws BasicException {
                return null;
            }
        };

        BasicException thrown = assertThrows(BasicException.class, tx::execute);
        assertEquals("Exception on Transaction execute", thrown.getMessage());
        assertSame(sqlEx, thrown.getCause());

        InOrder inOrder = Mockito.inOrder(session);
        inOrder.verify(session).begin();
        inOrder.verify(session).commit();
        inOrder.verify(session).rollback();
    }

    @Test
    @DisplayName("Rollback failure attaches suppressed exception to primary cause")
    void testRollbackFailureAttachesSuppressed() throws SQLException {
        Mockito.when(session.isTransaction()).thenReturn(false, true);
        SQLException rollbackSqlEx = new SQLException("Rollback failed due to broken socket");
        Mockito.doThrow(rollbackSqlEx).when(session).rollback();

        IllegalStateException primary = new IllegalStateException("Primary business failure");

        Transaction<Void> tx = new Transaction<>(session) {
            @Override
            protected Void transact() throws BasicException {
                throw primary;
            }
        };

        IllegalStateException thrown = assertThrows(IllegalStateException.class, tx::execute);
        assertSame(primary, thrown);
        assertEquals(1, thrown.getSuppressed().length);
        assertSame(rollbackSqlEx, thrown.getSuppressed()[0]);
    }

    @Test
    @DisplayName("Nested transaction participates without calling nested begin, commit, or rollback")
    void testNestedTransactionBehavior() throws BasicException, SQLException {
        Mockito.when(session.isTransaction()).thenReturn(true);

        Transaction<Integer> nestedTx = new Transaction<>(session) {
            @Override
            protected Integer transact() throws BasicException {
                return 42;
            }
        };

        int value = nestedTx.execute();
        assertEquals(42, value);

        Mockito.verify(session, Mockito.never()).begin();
        Mockito.verify(session, Mockito.never()).commit();
        Mockito.verify(session, Mockito.never()).rollback();
    }
}

