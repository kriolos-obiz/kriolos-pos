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
package com.openbravo.data.gui.modal;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure Java JUnit test suite for {@link PosUIModal}, {@link ModalMode}, and presentation contracts.
 * <p>
 * Validates builder state, window owner resolution, modality contracts, and lifecycle callbacks
 * without spawning interactive OS-level windows.
 * </p>
 */
class PosUIModalTest {

    @BeforeEach
    void setUp() {
        PosUIModal.setDefaultMode(ModalMode.NATIVE_DIALOG);
    }

    @AfterEach
    void tearDown() {
        PosUIModal.setDefaultMode(ModalMode.NATIVE_DIALOG);
    }

    @Test
    @DisplayName("PosUIModal fluent builder correctly sets and exposes all properties")
    void testFluentBuilderProperties() {
        Component dummyParent = new JPanel();
        JPanel content = new JPanel();
        Dimension size = new Dimension(500, 350);
        AtomicBoolean closedCalled = new AtomicBoolean(false);

        PosUIModal modal = PosUIModal.create(dummyParent, content)
                .setTitle("Customer Search")
                .setModal(true)
                .setResizable(false)
                .setPreferredSize(size)
                .setMode(ModalMode.IN_FRAME_OVERLAY)
                .onClosed(() -> closedCalled.set(true))
                .setResult("SELECTED_ID_123");

        assertSame(dummyParent, modal.getParent());
        assertSame(content, modal.getContent());
        assertEquals("Customer Search", modal.getTitle());
        assertTrue(modal.isModal());
        assertFalse(modal.isResizable());
        assertEquals(size, modal.getPreferredSize());
        assertEquals(ModalMode.IN_FRAME_OVERLAY, modal.getMode());
        assertEquals("SELECTED_ID_123", modal.getResult());
        assertEquals("SELECTED_ID_123", modal.getResult(String.class));
        assertNull(modal.getResult(Integer.class));

        modal.notifyClosed();
        assertTrue(closedCalled.get());
    }

    @Test
    @DisplayName("PosUIModal handles null parent gracefully without NullPointerException")
    void testNullParentHandling() {
        JPanel content = new JPanel();
        PosUIModal modal = PosUIModal.create(content);

        assertNull(modal.getParent());
        assertSame(content, modal.getContent());

        Window owner = PosUIModal.resolveWindowOwner(null);
        assertNotNull(owner, "resolveWindowOwner(null) must return a safe fallback window, never null");
    }

    @Test
    @DisplayName("PosUIModal rejects null content panel")
    void testNullContentThrowsException() {
        assertThrows(NullPointerException.class, () -> PosUIModal.create(new JPanel(), null));
        assertThrows(NullPointerException.class, () -> PosUIModal.create(null));
    }

    @Test
    @DisplayName("Global default mode can be modified and inherited by new instances")
    void testDefaultModeSwitching() {
        assertEquals(ModalMode.NATIVE_DIALOG, PosUIModal.getDefaultMode());

        PosUIModal.setDefaultMode(ModalMode.IN_FRAME_OVERLAY);
        assertEquals(ModalMode.IN_FRAME_OVERLAY, PosUIModal.getDefaultMode());

        PosUIModal modal = PosUIModal.create(new JPanel());
        assertEquals(ModalMode.IN_FRAME_OVERLAY, modal.getMode());

        assertThrows(NullPointerException.class, () -> PosUIModal.setDefaultMode(null));
    }

    @Test
    @DisplayName("resolveWindowOwner correctly identifies ancestor Windows without throwing")
    void testResolveWindowOwnerHierarchy() {
        JFrame frame = new JFrame();
        assertSame(frame, PosUIModal.resolveWindowOwner(frame));

        JDialog dialog = new JDialog(frame);
        assertSame(dialog, PosUIModal.resolveWindowOwner(dialog));

        JPanel child = new JPanel();
        frame.getContentPane().add(child);
        assertSame(frame, PosUIModal.resolveWindowOwner(child));

        frame.dispose();
        dialog.dispose();
    }

    @Test
    @DisplayName("Lifecycle execution delegates properly to ModalStrategy and notifies once")
    void testStrategyLifecycleAndCallbackIdempotency() {
        AtomicInteger displayCalls = new AtomicInteger(0);
        AtomicInteger closeCalls = new AtomicInteger(0);
        AtomicInteger onClosedCalls = new AtomicInteger(0);

        ModalStrategy mockStrategy = new ModalStrategy() {
            @Override
            public void display(PosUIModal modal) {
                displayCalls.incrementAndGet();
            }

            @Override
            public void close(PosUIModal modal) {
                closeCalls.incrementAndGet();
                modal.notifyClosed();
            }
        };

        PosUIModal modal = PosUIModal.create(new JLabel("Test"))
                .setTitle("Mock Modal")
                .setStrategy(mockStrategy)
                .onClosed(onClosedCalls::incrementAndGet);

        assertEquals(0, displayCalls.get());
        modal.show();
        assertEquals(1, displayCalls.get());

        assertEquals(0, closeCalls.get());
        assertEquals(0, onClosedCalls.get());
        modal.close();
        assertEquals(1, closeCalls.get());
        assertEquals(1, onClosedCalls.get());

        // Verify idempotency: calling notifyClosed again should not re-trigger the callback
        modal.notifyClosed();
        assertEquals(1, onClosedCalls.get(), "onClosed callback must be executed at most once");
    }

    @Test
    @DisplayName("ModalMode enum contains expected architectural modes")
    void testModalModeEnumValues() {
        assertEquals(2, ModalMode.values().length);
        assertEquals(ModalMode.NATIVE_DIALOG, ModalMode.valueOf("NATIVE_DIALOG"));
        assertEquals(ModalMode.IN_FRAME_OVERLAY, ModalMode.valueOf("IN_FRAME_OVERLAY"));
    }
}
