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
package com.openbravo.pos.printer.escpos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writer implementation that redirects raw printer bytes into a local file.
 * Thread-safe implementation designed to handle high-concurrency POS printing.
 * 
 * @author JG uniCenta
 */
public class PrinterWritterFile extends PrinterWritter {

    private static final Logger LOGGER = Logger.getLogger(PrinterWritterFile.class.getName());
    private final String m_sFilePrinter;
    private OutputStream m_out;
    
    // Dedicated lock object to ensure atomicity and eliminate cross-method race conditions
    private final Object lock = new Object();

    /**
     * Initializes the file writer with the target printer file path.
     * 
     * @param sFilePrinter The absolute or relative path to the destination file.
     */
    public PrinterWritterFile(String sFilePrinter) {
        this.m_sFilePrinter = sFilePrinter;
        this.m_out = null;
    }

    /**
     * Writes raw byte arrays to the designated printer file.
     * Thread-safe block prevents data interleaving from concurrent sales.
     * 
     * @param data The raw byte array payload to write.
     */
    @Override
    protected void internalWrite(byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }

        synchronized (lock) {
            try {
                if (m_out == null) {
                    File file = new File(m_sFilePrinter);
                    File parentDir = file.getParentFile();
                    
                    if (parentDir != null && !parentDir.exists()) {
                        if (!parentDir.mkdirs()) {
                            LOGGER.log(Level.WARNING, "Failed to create directories for path: {0}", parentDir.getAbsolutePath());
                        }
                    }
                    
                    m_out = new FileOutputStream(file);
                }
                m_out.write(data);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Exception occurred while writing data to file: " + m_sFilePrinter, e);
            }
        }
    }

    /**
     * Flushes buffered data and explicitly releases file system locks.
     */
    @Override
    protected void internalFlush() {
        synchronized (lock) {
            closeFileResources();
        }
    }

    /**
     * Closes the active file output stream and cleans up resources safely.
     */
    @Override
    protected void internalClose() {
        synchronized (lock) {
            closeFileResources();
        }
    }

    /**
     * Helper method to centralize stream flushing, closing, and nullification.
     * Must always be invoked within a synchronized block holding the lock.
     */
    private void closeFileResources() {
        if (m_out != null) {
            try {
                m_out.flush();
                m_out.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Exception occurred while closing file resources for: " + m_sFilePrinter, e);
            } finally {
                m_out = null; // Ensured to happen even if close() throws an exception
            }
        }
    }
}
