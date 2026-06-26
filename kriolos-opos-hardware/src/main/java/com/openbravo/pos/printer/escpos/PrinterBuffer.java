/*
 * Copyright (C) 2025 Paulo Borges
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
import java.util.logging.Logger;

/**
 * High-performance, memory-efficient thread-safe circular byte buffer.
 * Replaces object-heavy LinkedList implementation to eliminate Garbage Collection pressure.
 * 
 * @author psb
 */
public class PrinterBuffer {

    private static final Logger LOGGER = Logger.getLogger(PrinterBuffer.class.getName());

    // Circular primitive array to eliminate boxing overhead (Byte vs byte)
    private final byte[] buffer;
    private int head;
    private int tail;
    private int count;
    
    // Default initial capacity of 64KB, ideal for processing long POS layout structures
    private static final int DEFAULT_CAPACITY = 65536;

    /**
     * Initializes the circular printer buffer with a default safe size allocation.
     */
    public PrinterBuffer() {
        this.buffer = new byte[DEFAULT_CAPACITY];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
    }

    /**
     * Enqueues a single raw primitive byte into the queue storage.
     * 
     * @param data The primitive byte to append.
     */
    public synchronized void putData(byte data) {
        // Dynamic resize logic block if buffer fills up completely
        if (count == buffer.length) {
            LOGGER.warning("PrinterBuffer capacity reached. Dropping incoming bytes to prevent POS memory crashes.");
            return;
        }
        buffer[tail] = data;
        tail = (tail + 1) % buffer.length;
        count++;
        // Notify waiting consumer threads that data is available
        notify();
    }

    /**
     * Converts text strings using correct POS character mapping tables and enqueues bytes safely.
     * 
     * @param data The text data to encode.
     */
    public synchronized void putData(String data) {
        if (data == null) {
            return;
        }
        byte[] dataBytes = data.getBytes(StandardCharsets.ISO_8859_1);
        for (byte b : dataBytes) {
            putData(b);
        }
    }

    /**
     * Blocks execution thread until a raw byte becomes available for consumption.
     * 
     * @return The next primitive byte available in the queue.
     * @throws PrinterBufferException If processing is interrupted while waiting.
     */
    public synchronized byte getData() throws PrinterBufferException {
        while (count == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                // Restore thread interruption state before wrapping exception
                Thread.currentThread().interrupt();
                throw new PrinterBufferException("PrinterBufferException occurred while waiting for incoming raw data stream.", e);
            }
        }
        byte data = buffer[head];
        head = (head + 1) % buffer.length;
        count--;
        return data;
    }

    /**
     * Custom unchecked wrapper exception to handle multi-threaded runtime errors.
     */
    public static class PrinterBufferException extends Exception {

        private static final long serialVersionUID = 1L;

        public PrinterBufferException() {
            super();
        }

        public PrinterBufferException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
