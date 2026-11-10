/*
 * Copyright (C) 2022 KriolOS
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
package com.openbravo.pos.data;


import com.openbravo.basic.BasicException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 *
 * @author poolborges
 */
public class DBMigrator {

    private final static Logger LOGGER = Logger.getLogger(DBMigrator.class.getName());
    
    public static void main(String[] args) throws SQLException {
        //execDBMigration();
    }

    public static void execDBMigration(com.openbravo.data.loader.Session dbSession) throws BasicException {
        boolean res = false;
        LOGGER.info("Database Migration init");
        Connection conn = null;
        try {
            conn = dbSession.getConnection();
            //Liquibase
            JdbcConnection connliquibase = new JdbcConnection(conn);
            
            Database databse = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connliquibase);
            Liquibase liquibase = new Liquibase("/pos_liquidbase/db-changelog-master.xml", new ClassLoaderResourceAccessor(), databse);

            //Run basebase
            liquibase.update("pos-database-update");
            res = true;
        } catch (DatabaseException ex) {
            LOGGER.log(Level.SEVERE,"DB Migration Exception: " , ex);
            throw new BasicException("DB Migration Exception: ", ex);
        } catch (LiquibaseException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "DB Migration Exception: ", ex);
            throw new BasicException("DB Migration Exception: ", ex);
        } finally {
            if (conn != null) {
                try {
                    if (!conn.getAutoCommit()) {
                        conn.commit();
                        conn.setAutoCommit(true);
                    }
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Exception restoring connection autoCommit after migration: ", ex);
                }
            }
        }
    }
}
