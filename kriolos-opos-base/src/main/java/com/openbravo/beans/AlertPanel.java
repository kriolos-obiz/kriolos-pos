package com.openbravo.beans;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * PosWarningPanel — KriolOS POS Modern Warning Alert Component
 *
 * <p>A touch-screen-optimised, Adobe Spectrum-aligned warning panel.
 * Zero external dependencies — pure {@code javax.swing.*} / {@code java.awt.*}.
 *
 * <h3>Design System Alignment:</h3>
 * <ul>
 *   <li>Adobe Spectrum "Notice/Warning" semantic color tokens</li>
 *   <li>Fitts's Law touch ergonomics — 56 px minimum interactive target height</li>
 *   <li>Cross-platform logical fonts only: {@link Font#SANS_SERIF} / {@link Font#DIALOG}</li>
 *   <li>Vector warning icon drawn via {@link Graphics2D} with full AA — no emoji, no images</li>
 *   <li>Responsive body text via {@link JTextArea} — line-wraps, zero scrollbars</li>
 *   <li>Premium button state feedback (hover → press) overrides OS L&F blocking</li>
 * </ul>
 *
 * <h3>UI/UX Architecture Note:</h3>
 * <p>Root panel intentionally omits {@code setPreferredSize}/{@code setMinimumSize}.
 * {@code PosUIModal} calls {@code pack()} which computes the natural size from children.
 * Fixing the root size is fragile: any padding/border change will clip content.
 *
 * <h3>URN Anchoring (Rule 7.2):</h3>
 * <p>All interactive children receive {@code setName()} URN identifiers in
 * {@code initDomainAdapters()}, enabling automated testing and future web bindings.
 *
 * @author KriolOS
 * @see com.openbravo.data.gui.modal.PosUIModal
 */
public class AlertPanel extends JPanel {

    // =========================================================================
    // Adobe Spectrum "Notice / Warning" Semantic Color Tokens
    // =========================================================================

    /** Warm cream-amber — highly desaturated background, minimal eye strain */
    private static final Color CLR_BG          = new Color(0xFF, 0xF8, 0xEE);
    /** Muted amber — 1 px structural outline */
    private static final Color CLR_BORDER      = new Color(0xE8, 0xA4, 0x4A);
    /** Deep amber — left accent stripe, warning icon fill */
    private static final Color CLR_ACCENT      = new Color(0xCB, 0x6F, 0x10);
    /** Near-black warm brown — maximum legibility on amber */
    private static final Color CLR_TITLE       = new Color(0x33, 0x1E, 0x00);
    /** Dark warm brown — body text, slightly softer than title */
    private static final Color CLR_BODY        = new Color(0x5A, 0x37, 0x0A);
    /** White — exclamation mark inside the warning triangle */
    private static final Color CLR_ICON_MARK   = Color.WHITE;

    // =========================================================================
    // Layout Metrics  (Adobe Spectrum Touch Scale)
    // =========================================================================

    private static final int CORNER_R    = 14;   // outer card corner radius (px)
    private static final int ACCENT_W    = 5;    // left accent stripe width (px)
    private static final int PAD_V       = 20;   // vertical content padding (px)
    private static final int PAD_H       = 20;   // horizontal content padding (px)
    private static final int ICON_SZ     = 34;   // warning icon bounding box (px)
    private static final int ICON_GAP    = 12;   // gap: icon ↔ title text (px)
    private static final int SECTION_GAP = 14;   // gap: header ↔ body ↔ buttons (px)
    static final int BTN_H               = 56;   // Spectrum touch minimum (px)
    static final int BTN_MIN_W           = 120;  // minimum button width (px)
    private static final int BTN_R       = 8;    // button corner radius (px)
    private static final int BTN_GAP     = 10;   // gap between buttons (px)
    /** Preferred width hint — keeps JTextArea wrapping sensibly without a fixed height */
    private static final int PREFERRED_W = 460;

    // =========================================================================
    // Construction
    // =========================================================================

    /**
     * Creates a warning panel with both DISMISS and CONFIRM actions.
     *
     * @param title     Short, bold warning title (≤ 60 characters recommended)
     * @param body      Detailed body message — wraps automatically
     * @param onConfirm Runnable for the primary "CONFIRM" button
     * @param onDismiss Runnable for the secondary "DISMISS" button
     *                  (pass {@code null} to hide the secondary button)
     */
    public AlertPanel(String title, String body, Runnable onConfirm, Runnable onDismiss) {
        setOpaque(false);
        setLayout(new BorderLayout(0, SECTION_GAP));
        // Content insets: left accounts for ACCENT_W stripe + breathing room
        setBorder(new EmptyBorder(PAD_V, PAD_H + ACCENT_W + 8, PAD_V, PAD_H));

        buildUI(title, body, onConfirm, onDismiss);
        initDomainAdapters();
    }

    /**
     * Called by Swing whenever the Look & Feel is swapped at runtime
     * (e.g. {@code UIManager.setLookAndFeel()} + {@code SwingUtilities.updateComponentTreeUI()}).
     *
     * <p>The LAF's {@code installUI()} resets {@code opaque} to {@code true} on JPanel.
     * We override here to enforce our custom painting contract regardless of which LAF
     * is active — Metal, Nimbus, FlatLaf Dark, FlatLaf Light, or any future LAF.
     */
    @Override
    public void updateUI() {
        super.updateUI();
        setOpaque(false); // LAF may reset this — re-enforce our transparent background
    }

    /** Convenience: confirm-only (no dismiss button). */
    public AlertPanel(String title, String body, Runnable onConfirm) {
        this(title, body, onConfirm, null);
    }

    // =========================================================================
    // URN Anchoring  (Rule 7.2)
    // =========================================================================

    private void initDomainAdapters() {
        setName("kriolos:alert:warning-panel");
    }

    // =========================================================================
    // UI Construction
    // =========================================================================

    private void buildUI(String title, String body, Runnable onConfirm, Runnable onDismiss) {

        // ── Header row: icon + title ──────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(ICON_GAP, 0));
        header.setOpaque(false);
        header.setName("kriolos:alert:header-row");

        WarningIcon icon = new WarningIcon(ICON_SZ);
        icon.setName("kriolos:alert:icon-warning");

        JLabel lblTitle = new JLabel(title);
        lblTitle.setName("kriolos:alert:lbl-title");
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        lblTitle.setForeground(CLR_TITLE);
        lblTitle.setVerticalAlignment(SwingConstants.CENTER);

        header.add(icon,     BorderLayout.WEST);
        header.add(lblTitle, BorderLayout.CENTER);

        // ── Body text — responsive wrapping, zero scrollbars ─────────────────
        //
        // JTextArea with lineWrap + wrapStyleWord is the only pure-Swing way
        // to get auto-wrapping without a JScrollPane. We set `columns` as a
        // preferred-width hint so Swing can compute a sensible minimum width
        // during layout (prevents the "one long line" collapse problem).
        //
        // Anonymous subclass: overrides updateUI() so that a runtime LAF swap
        // (SwingUtilities.updateComponentTreeUI) cannot reset our opaque/border
        // settings — JTextArea's installUI resets background and border.
        JTextArea txtBody = new JTextArea(body) {
            @Override
            public void updateUI() {
                super.updateUI();
                setOpaque(false);
                // Re-apply margin border so the body text stays aligned with
                // the title after the LAF reinstalls its own text-area border.
                setMargin(new Insets(0, ICON_SZ + ICON_GAP, 0, 0));
            }
        };
        txtBody.setName("kriolos:alert:txt-body");
        txtBody.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        txtBody.setForeground(CLR_BODY);
        txtBody.setOpaque(false);
        txtBody.setEditable(false);
        txtBody.setFocusable(false);
        txtBody.setLineWrap(true);
        txtBody.setWrapStyleWord(true);
        txtBody.setColumns(28); // width hint: ~28 chars at 13pt ≈ 280 px
        txtBody.setMargin(new Insets(0, ICON_SZ + ICON_GAP, 0, 0));

        // ── Button row ───────────────────────────────────────────────────────
        JPanel commandBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, BTN_GAP, 0));
        commandBar.setOpaque(false);
        commandBar.setName("kriolos:alert:cmd-bar");

        if (onDismiss != null) {
            SpectrumButton btnDismiss = new SpectrumButton("DISMISS", false);
            btnDismiss.setName("kriolos:alert:btn-dismiss");
            btnDismiss.addActionListener(e -> onDismiss.run());
            commandBar.add(btnDismiss);
        }

        SpectrumButton btnConfirm = new SpectrumButton("CONFIRM", true);
        btnConfirm.setName("kriolos:alert:btn-confirm");
        btnConfirm.addActionListener(e -> onConfirm.run());
        commandBar.add(btnConfirm);

        add(header, BorderLayout.NORTH);
        add(txtBody, BorderLayout.CENTER);
        add(commandBar, BorderLayout.SOUTH);
    }

    // =========================================================================
    // Root Panel Painting — Rounded Card + Left Accent Stripe
    // =========================================================================

    @Override
    protected void paintComponent(Graphics g) {
        // ── Step 1: LAF delegate runs first ──────────────────────────────────
        // For opaque=false panels this is a no-op (no background fill),
        // but we honour the Swing painting contract so any LAF housekeeping
        // (clip setup, Graphics state init) executes before we customise.
        super.paintComponent(g);

        // ── Step 2: Our custom card painting goes ON TOP ──────────────────────
        // Using a defensive g.create() copy — never mutate the shared Graphics.
        Graphics2D g2 = (Graphics2D) g.create();
        applyQualityHints(g2);

        int w = getWidth();
        int h = getHeight();

        // Rounded card shape (½px inset so 1px border lands fully inside bounds)
        RoundRectangle2D card = new RoundRectangle2D.Float(
                0.5f, 0.5f, w - 1f, h - 1f, CORNER_R, CORNER_R);

        // 1. Warm cream-amber card fill
        g2.setColor(CLR_BG);
        g2.fill(card);

        // 2. Left accent stripe — clipped to card shape so the rounded
        //    bottom-left corner has no sharp pixel bleed outside the card.
        Shape savedClip = g2.getClip();
        g2.clip(card);
        g2.setColor(CLR_ACCENT);
        g2.fillRect(0, 0, ACCENT_W, h);
        g2.setClip(savedClip);

        // 3. 1 px structural border
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(CLR_BORDER);
        g2.draw(card);

        g2.dispose();
        // Children are painted by Swing's paintChildren() — NOT by super.paintComponent()
    }

    /**
     * Returns a minimum preferred width so that JTextArea wrapping starts at a
     * sensible container width. Height remains fully computed from children —
     * this is NOT a fixed root size.
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(d.width, PREFERRED_W);
        return d;
    }

    // =========================================================================
    // Shared rendering utility
    // =========================================================================

    static void applyQualityHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,    RenderingHints.VALUE_STROKE_PURE);
    }

    // =========================================================================
    // Inner class: Warning Triangle Icon (vector, no emoji, no image resources)
    // =========================================================================

    /**
     * Paints an equilateral warning triangle (↑) with rounded corners and a
     * white exclamation mark, drawn entirely via {@link Path2D} bezier arcs.
     *
     * <p>Antialiased at all sizes. No raster images; no OS-specific emoji fonts.
     */
    private static final class WarningIcon extends JComponent {

        private final int size;

        WarningIcon(int size) {
            this.size = size;
            setPreferredSize(new Dimension(size, size));
            setOpaque(false);
            setName("kriolos:alert:icon-warning-gfx");
        }

        /** Re-enforce opaque=false after any runtime LAF swap. */
        @Override
        public void updateUI() {
            super.updateUI();
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);

            float w   = getWidth();
            float h   = getHeight();
            float pad = 1.5f;
            float cr  = w * 0.10f; // corner-rounding radius proportional to icon size

            // Three triangle vertices: apex (top-centre), bottom-left, bottom-right
            float apexX = w / 2f,  apexY = pad;
            float blX   = pad,     blY   = h - pad;
            float brX   = w - pad, brY   = h - pad;

            // Build a rounded-corner triangle using quadratic Bézier arcs.
            // At each vertex, we step back `cr` along both adjacent edges, then
            // use the vertex itself as the Bézier control point.
            Path2D.Float tri = buildRoundedTriangle(apexX, apexY, blX, blY, brX, brY, cr);

            // ── Fill triangle ──────────────────────────────────────────────────
            g2.setColor(CLR_ACCENT);
            g2.fill(tri);

            // ── Exclamation mark ───────────────────────────────────────────────
            // Bar (vertical line, round caps)
            g2.setColor(CLR_ICON_MARK);
            float cx       = w / 2f;
            float stemTop  = h * 0.26f;
            float stemBot  = h * 0.60f;
            float dotCy    = h * 0.75f;
            float sw       = Math.max(2.2f, w * 0.105f); // stroke proportional to size

            g2.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Float(cx, stemTop, cx, stemBot));

            // Dot (zero-length line renders as a round cap = perfect circle dot)
            g2.setStroke(new BasicStroke(sw * 1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Float(cx, dotCy, cx, dotCy));

            g2.dispose();
        }

        /**
         * Builds a {@link Path2D} triangle with rounded corners using quadratic Bézier.
         * Each corner is "cut" by moving {@code cr} pixels along both adjacent edges,
         * and the original vertex becomes the Bézier control point.
         */
        private static Path2D.Float buildRoundedTriangle(
                float ax, float ay,   // apex
                float bx, float by,   // bottom-left
                float cx, float cy,   // bottom-right
                float cr) {

            // Edge unit vectors: each "from" → "to"
            float[] abU = unit(ax, ay, bx, by); // apex → bottom-left
            float[] baU = unit(bx, by, ax, ay); // bottom-left → apex
            float[] bcU = unit(bx, by, cx, cy); // bottom-left → bottom-right
            float[] cbU = unit(cx, cy, bx, by); // bottom-right → bottom-left
            float[] caU = unit(cx, cy, ax, ay); // bottom-right → apex
            float[] acU = unit(ax, ay, cx, cy); // apex → bottom-right

            // Cut-points: `cr` units along each edge from each vertex
            float a1x = ax + acU[0]*cr, a1y = ay + acU[1]*cr; // apex going right
            float a2x = ax + abU[0]*cr, a2y = ay + abU[1]*cr; // apex going left

            float b1x = bx + baU[0]*cr, b1y = by + baU[1]*cr; // bl going up
            float b2x = bx + bcU[0]*cr, b2y = by + bcU[1]*cr; // bl going right

            float c1x = cx + cbU[0]*cr, c1y = cy + cbU[1]*cr; // br going left
            float c2x = cx + caU[0]*cr, c2y = cy + caU[1]*cr; // br going up

            Path2D.Float path = new Path2D.Float();
            // Start after the apex corner going right (toward bottom-right)
            path.moveTo(a1x, a1y);
            path.lineTo(c2x, c2y);                    // right edge
            path.quadTo(cx, cy, c1x, c1y);            // bottom-right corner
            path.lineTo(b2x, b2y);                    // bottom edge
            path.quadTo(bx, by, b1x, b1y);            // bottom-left corner
            path.lineTo(a2x, a2y);                    // left edge
            path.quadTo(ax, ay, a1x, a1y);            // apex corner
            path.closePath();
            return path;
        }

        /** Returns the unit vector from {@code (x1,y1)} to {@code (x2,y2)}. */
        private static float[] unit(float x1, float y1, float x2, float y2) {
            float dx = x2 - x1, dy = y2 - y1;
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            return new float[]{ dx / len, dy / len };
        }
    }

    // =========================================================================
    // Inner class: Spectrum Touch Button
    // =========================================================================

    /**
     * Touch-optimised button with Adobe Spectrum warning semantics.
     *
     * <p>Overrides ALL painting so that hover and pressed states are visible
     * regardless of the active OS Look & Feel (native L&Fs often suppress
     * custom {@link ButtonModel} state rendering on opaque buttons).
     *
     * <ul>
     *   <li><b>Primary</b> (filled amber): CONFIRM / primary action</li>
     *   <li><b>Secondary</b> (ghost outline): DISMISS / cancel action</li>
     * </ul>
     */
    static final class SpectrumButton extends JButton {

        // Primary filled-button palette
        private static final Color P_DEFAULT = new Color(0xCB, 0x6F, 0x10);
        private static final Color P_HOVER   = new Color(0xB5, 0x60, 0x0D);
        private static final Color P_PRESS   = new Color(0x96, 0x4E, 0x08);
        private static final Color P_TEXT    = Color.WHITE;

        // Secondary ghost-button palette
        private static final Color S_HOVER   = new Color(0xFF, 0xED, 0xD0);
        private static final Color S_PRESS   = new Color(0xFF, 0xD9, 0xA0);
        private static final Color S_BORDER  = new Color(0xCB, 0x6F, 0x10);
        private static final Color S_TEXT    = new Color(0xCB, 0x6F, 0x10);

        private final boolean primary;

        SpectrumButton(String label, boolean primary) {
            super(label); // label already uppercase from caller
            this.primary = primary;

            applyLafNeutralDefaults();
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Trigger repaint on every mouse-state transition so pressed/hover
            // visuals respond immediately without waiting for Swing's event queue.
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e)  { repaint(); }
                @Override public void mouseExited(MouseEvent e)   { repaint(); }
                @Override public void mousePressed(MouseEvent e)  { repaint(); }
                @Override public void mouseReleased(MouseEvent e) { repaint(); }
            });
        }

        /**
         * CRITICAL for LAF-agnosticism: every call to
         * {@code UIManager.setLookAndFeel()} + {@code SwingUtilities.updateComponentTreeUI()}
         * triggers {@code updateUI()} on every component in the hierarchy.
         * JButton's {@code BasicButtonUI.installUI()} (and FlatLaf's override)
         * unconditionally resets {@code contentAreaFilled}, {@code borderPainted},
         * {@code focusPainted}, and {@code opaque} to their LAF-mandated defaults.
         *
         * <p>We re-apply our rendering contract AFTER super runs so our custom
         * painting always wins, regardless of which LAF is active.
         */
        @Override
        public void updateUI() {
            super.updateUI(); // LAF installs its UI delegate first
            applyLafNeutralDefaults(); // then we override what the LAF reset
        }

        /** Central place to apply all LAF-neutralising settings. */
        private void applyLafNeutralDefaults() {
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            setFocusPainted(false);       // no focus ring — we paint state ourselves
            setBorderPainted(false);      // no LAF border — we draw our own
            setContentAreaFilled(false);  // no LAF background fill — we paint ours
            setOpaque(false);             // transparent — our paintComponent handles all
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityHints(g2);

            int w = getWidth(), h = getHeight();
            boolean pressed = getModel().isPressed();
            boolean hover   = getModel().isRollover();

            // 1px inset so the stroke border doesn't clip on the component edge
            RoundRectangle2D shape = new RoundRectangle2D.Float(
                    1f, 1f, w - 2f, h - 2f, BTN_R * 2, BTN_R * 2);

            if (primary) {
                // Filled amber button
                g2.setColor(pressed ? P_PRESS : hover ? P_HOVER : P_DEFAULT);
                g2.fill(shape);
                g2.setColor(P_TEXT);
            } else {
                // Ghost button — transparent fill + amber border
                g2.setColor(pressed ? S_PRESS : hover ? S_HOVER : new Color(0, 0, 0, 0));
                g2.fill(shape);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(S_BORDER);
                g2.draw(shape);
                g2.setColor(S_TEXT);
            }

            // Centred label
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), tx, ty);

            g2.dispose();
            // Do NOT call super.paintComponent — we handle everything
        }

        @Override
        public Dimension getPreferredSize() {
            // Enforce Spectrum touch minimums regardless of label length
            Dimension d = super.getPreferredSize();
            // Add horizontal padding around label text
            d.width  = Math.max(d.width + 32, BTN_MIN_W);
            d.height = Math.max(d.height, BTN_H);
            return d;
        }
    }

    // =========================================================================
    // Live Preview  (run PosWarningPanel directly to render in a JFrame)
    // =========================================================================

    /**
     * Self-contained preview entrypoint.
     *
     * <p>Renders the component against a dark POS dashboard background so the
     * amber card's contrast behaviour is immediately visible.
     * Run this class directly: {@code java PosWarningPanel}.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("KriolOS POS — PosWarningPanel Preview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Dark dashboard background simulates the real POS context
            JPanel canvas = new JPanel(new GridBagLayout());
            canvas.setBackground(new Color(0x1A, 0x1A, 0x2E));

            // ── Warning panel instance ────────────────────────────────────────
            AlertPanel warning = new AlertPanel(
                    "Session About to Expire",
                    "Your cashier session has been inactive for 8 minutes. " +
                    "Please confirm you are still present to continue processing " +
                    "transactions. Unattended sessions will automatically close " +
                    "after 2 further minutes of inactivity.",
                    () -> { System.out.println("[CONFIRM pressed]"); frame.dispose(); },
                    () -> { System.out.println("[DISMISS pressed]"); frame.dispose(); }
            );

            // Outer gap from canvas edges — do NOT use setPreferredSize on the panel
            JPanel margin = new JPanel(new BorderLayout());
            margin.setOpaque(false);
            margin.setBorder(new EmptyBorder(40, 40, 40, 40));
            margin.add(warning, BorderLayout.CENTER);

            canvas.add(margin);
            frame.setContentPane(canvas);

            // Let pack() compute the natural size from children
            frame.pack();
            // Enforce a minimum frame width so the preview looks like a real POS terminal
            Dimension ps = frame.getSize();
            frame.setSize(Math.max(ps.width, 640), Math.max(ps.height, 420));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
