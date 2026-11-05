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

/**
 * Executes a unit of work inside a managed database transaction.
 *
 * <p>Guarantees that any checked exception, runtime exception, or error
 * triggers a session rollback, ensuring connection state and table locks
 * are never leaked.</p>
 *
 * @author adrianromero Created on 26 de febrero de 2007, 21:50
 * @author KriolOS Team
 * @param <T> Transaction return type
 */
public abstract class Transaction<T> {

    private final Session session;

    /**
     * Creates a new instance of Transaction.
     *
     * @param session Database session to manage
     */
    public Transaction(Session session) {
        this.session = session;
    }

    /**
     * Executes the transaction with automatic begin, commit, and rollback.
     *
     * @return Result of the transactional execution
     * @throws BasicException If a business or database error occurs
     */
    public final T execute() throws BasicException {
        if (session.isTransaction()) {
            return transact();
        }

        boolean committed = false;
        try {
            session.begin();
            T result = transact();
            session.commit();
            committed = true;
            return result;
        } catch (Throwable t) {
            try {
                if (session.isTransaction()) {
                    session.rollback();
                }
            } catch (SQLException rollbackEx) {
                t.addSuppressed(rollbackEx);
            }

            if (t instanceof BasicException be) {
                throw be;
            } else if (t instanceof RuntimeException re) {
                throw re;
            } else if (t instanceof Error err) {
                throw err;
            } else if (t instanceof SQLException se) {
                throw new BasicException("Exception on Transaction execute", se);
            } else {
                throw new BasicException("Exception on Transaction execute", t);
            }
        } finally {
            if (!committed && session.isTransaction()) {
                try {
                    session.rollback();
                } catch (SQLException ignored) {
                    // Fail-safe cleanup
                }
            }
        }
    }

    /**
     * Executes the transactional operations.
     *
     * @return Result of the execution
     * @throws BasicException If a business or database error occurs
     */
    protected abstract T transact() throws BasicException;
}
