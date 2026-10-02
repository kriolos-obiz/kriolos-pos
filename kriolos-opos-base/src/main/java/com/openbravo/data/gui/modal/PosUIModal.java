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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Unified modal orchestrator and fluent builder for presenting panel-first modals.
 * <p>
 * Decouples dialog UI components from direct {@link JDialog} inheritance, allowing
 * panels to be hosted either in native top-level dialogs or in-frame overlays
 * via pluggable {@link ModalStrategy} implementations.
 * </p>
 *
 * @author KriolOS
 */
public class PosUIModal {

    private static volatile ModalMode defaultMode = ModalMode.NATIVE_DIALOG;

    private final Component parent;
    private final JComponent content;
    private String title = "";
    private boolean modal = true;
    private boolean resizable = false;
    private Dimension preferredSize;
    private ModalMode mode = defaultMode;
    private Runnable onClosed;
    private Object result;

    private JDialog dialog;
    private ModalStrategy activeStrategy;

    public PosUIModal(Component parent, JComponent content) {
        this.parent = parent;
        this.content = Objects.requireNonNull(content, "Content component cannot be null");
    }

    /**
     * Creates a new {@link PosUIModal} fluent builder.
     *
     * @param parent the candidate parent component or window, may be {@code null}
     * @param content the UI component / panel to be displayed
     * @return a new builder instance
     */
    public static PosUIModal create(Component parent, JComponent content) {
        return new PosUIModal(parent, content);
    }

    /**
     * Creates a new {@link PosUIModal} fluent builder with no parent.
     *
     * @param content the UI component / panel to be displayed
     * @return a new builder instance
     */
    public static PosUIModal create(JComponent content) {
        return new PosUIModal(null, content);
    }

    /**
     * Sets the global default {@link ModalMode}.
     *
     * @param mode the new default presentation mode
     */
    public static void setDefaultMode(ModalMode mode) {
        defaultMode = Objects.requireNonNull(mode, "Default ModalMode cannot be null");
    }

    /**
     * Gets the global default {@link ModalMode}.
     *
     * @return the active default mode
     */
    public static ModalMode getDefaultMode() {
        return defaultMode;
    }

    /**
     * Resolves the appropriate parent {@link Window} hierarchy for a modal.
     * <p>
     * Prevents orphan windows, broken transient-for hints under Wayland,
     * and guards against {@link NullPointerException} when {@code parent == null}.
     * </p>
     *
     * @param parent the candidate parent component, or {@code null}
     * @return the resolved ancestor {@link Window}, or {@link JOptionPane#getRootFrame()} fallback
     */
    public static Window resolveWindowOwner(Component parent) {
        if (parent != null) {
            if (parent instanceof Frame || parent instanceof Dialog) {
                return (Window) parent;
            }
            Window w = SwingUtilities.getWindowAncestor(parent);
            if (w != null) {
                return w;
            }
        }
        for (Frame f : Frame.getFrames()) {
            if (f.isShowing()) {
                return f;
            }
        }
        return JOptionPane.getRootFrame();
    }

    /**
     * Sets the title for this modal.
     *
     * @param title the dialog/overlay title
     * @return this builder
     */
    public PosUIModal setTitle(String title) {
        this.title = title != null ? title : "";
        return this;
    }

    /**
     * Sets whether this modal blocks user input to other windows.
     *
     * @param modal true if modal
     * @return this builder
     */
    public PosUIModal setModal(boolean modal) {
        this.modal = modal;
        return this;
    }

    /**
     * Sets whether this modal can be resized by the user.
     *
     * @param resizable true if resizable
     * @return this builder
     */
    public PosUIModal setResizable(boolean resizable) {
        this.resizable = resizable;
        return this;
    }

    /**
     * Sets an explicit preferred size for the modal window/card.
     *
     * @param preferredSize the size dimension
     * @return this builder
     */
    public PosUIModal setPreferredSize(Dimension preferredSize) {
        this.preferredSize = preferredSize;
        return this;
    }

    /**
     * Overrides the presentation {@link ModalMode} for this specific modal instance.
     *
     * @param mode the modal presentation mode
     * @return this builder
     */
    public PosUIModal setMode(ModalMode mode) {
        this.mode = Objects.requireNonNull(mode, "ModalMode cannot be null");
        return this;
    }

    /**
     * Registers a callback to be executed when the modal is dismissed/closed.
     *
     * @param onClosed the callback runnable
     * @return this builder
     */
    public PosUIModal onClosed(Runnable onClosed) {
        this.onClosed = onClosed;
        return this;
    }

    /**
     * Sets a result object associated with this modal's outcome.
     *
     * @param result the result payload
     * @return this builder
     */
    public PosUIModal setResult(Object result) {
        this.result = result;
        return this;
    }

    private ModalStrategy customStrategy;

    /**
     * Sets a custom presentation strategy (useful for testing or specialized embedding).
     *
     * @param strategy the custom modal strategy
     * @return this builder
     */
    public PosUIModal setStrategy(ModalStrategy strategy) {
        this.customStrategy = strategy;
        return this;
    }

    /**
     * Displays this modal using the configured strategy.
     */
    public void show() {
        if (customStrategy != null) {
            activeStrategy = customStrategy;
        } else {
            activeStrategy = switch (mode) {
                case IN_FRAME_OVERLAY -> new InFrameOverlayModalStrategy();
                case NATIVE_DIALOG -> new NativeDialogModalStrategy();
            };
        }
        activeStrategy.display(this);
    }

    /**
     * Closes this modal and releases resources.
     */
    public void close() {
        if (activeStrategy != null) {
            activeStrategy.close(this);
            activeStrategy = null;
        }
    }

    /**
     * Internal callback to execute the registered {@code onClosed} action.
     */
    public void notifyClosed() {
        if (onClosed != null) {
            Runnable callback = onClosed;
            onClosed = null;
            callback.run();
        }
    }

    // Accessors
    public Component getParent() {
        return parent;
    }

    public JComponent getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public boolean isModal() {
        return modal;
    }

    public boolean isResizable() {
        return resizable;
    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public ModalMode getMode() {
        return mode;
    }

    public Object getResult() {
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T getResult(Class<T> type) {
        if (type != null && type.isInstance(result)) {
            return (T) result;
        }
        return null;
    }

    public JDialog getDialog() {
        return dialog;
    }

    void setDialog(JDialog dialog) {
        this.dialog = dialog;
    }
}
