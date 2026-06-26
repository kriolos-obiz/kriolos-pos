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

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base class for asynchronous printer writers.
 * Uses a single-threaded executor to offload slow I/O operations from the main POS thread.
 * 
 * @author JG uniCenta
 */
public abstract class PrinterWritter {
    
    private static final Logger LOGGER = Logger.getLogger(PrinterWritter.class.getName());
    
    // volatile ensures thread visibility across CPU cores
    private volatile boolean initialized = false;
    private final ExecutorService exec;

    /**
     * Initializes the asynchronous single-threaded printer executor worker.
     */
    public PrinterWritter() {
        this.exec = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Internal implementation for writing raw byte data to the physical device or file.
     * 
     * @param data The byte payload to process.
     */
    protected abstract void internalWrite(byte[] data);

    /**
     * Internal implementation to flush pending hardware or stream data.
     */
    protected abstract void internalFlush();

    /**
     * Internal implementation to explicitly close underlying physical printer handles.
     */
    protected abstract void internalClose();
    
    /**
     * Initializes the printer with a specific command byte sequence if not already initialized.
     * 
     * @param data The initialization command bytes.
     */
    public synchronized void init(final byte[] data) {
        if (!initialized) {
            write(data);
            initialized = true;
        }
    }
       
    /**
     * Converts a string to bytes using standard ISO-8859-1 charset and schedules a write task.
     * 
     * @param sValue The text message to write.
     */
    public void write(String sValue) {
        if (sValue != null) {
            write(sValue.getBytes(StandardCharsets.ISO_8859_1));
        }
    }

    /**
     * Enqueues a raw byte array printing task into the asynchronous worker thread pool.
     * Includes a safety guard to prevent crashes if the writer is already shut down.
     * 
     * @param data The byte payload array.
     */
    public void write(final byte[] data) {
        if (data == null || exec.isShutdown()) {
            return;
        }
        try {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    internalWrite(data);
                }
            });
        } catch (RejectedExecutionException ex) {
            LOGGER.log(Level.WARNING, "Write task rejected. Printer writer executor is shutting down.", ex);
        }
    }
    
    /**
     * Enqueues an asynchronous flush task to push pending streams to the physical printer.
     */
    public void flush() {
        if (exec.isShutdown()) {
            return;
        }
        try {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    internalFlush();
                }
            });
        } catch (RejectedExecutionException ex) {
            LOGGER.log(Level.WARNING, "Flush task rejected. Printer writer executor is shutting down.", ex);
        }
    }
    
    /**
     * Enqueues an asynchronous close task and initiates a graceful shutdown of the executor.
     * Waits up to 5 seconds for pending printing tickets to finish before forcing a termination.
     */
    public void close() {
        if (exec.isShutdown()) {
            return;
        }
        try {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    internalClose();
                }
            });
        } catch (RejectedExecutionException ex) {
            LOGGER.log(Level.WARNING, "Close task execution rejected during active shutdown sequence.", ex);
        } finally {
            exec.shutdown();
            try {
                // Wait for ongoing print jobs to finish safely before exiting completely
                if (!exec.awaitTermination(5, TimeUnit.SECONDS)) {
                    exec.shutdownNow();
                }
            } catch (InterruptedException e) {
                exec.shutdownNow();
                Thread.currentThread().interrupt(); // Restore interrupted status flag
            }
        }
    }
}
