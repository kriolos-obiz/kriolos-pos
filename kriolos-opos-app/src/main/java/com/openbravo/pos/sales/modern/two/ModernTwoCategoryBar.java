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

package com.openbravo.pos.sales.modern.two;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.pim.CategoryInfo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;

/**
 * Touch-optimized horizontal category bar for ModernTwo.
 * Features prominent rounded category pills, touch swiping, mouse wheel,
 * and "<" / ">" navigation controls.
 *
 * @author KriolOS Team
 */
public class ModernTwoCategoryBar extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int PILL_HEIGHT = 48;

    private final JPanel pillsContainer;
    private final JScrollPane scrollPane;
    private final JButton btnPrev;
    private final JButton btnNext;
    private final ButtonGroup buttonGroup;
    private final Consumer<CategoryInfo> onCategorySelected;
    private final List<JToggleButton> categoryButtons = new ArrayList<>();

    public ModernTwoCategoryBar(Consumer<CategoryInfo> onCategorySelected) {
        this.onCategorySelected = onCategorySelected;
        this.buttonGroup = new ButtonGroup();

        setLayout(new BorderLayout(8, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        setPreferredSize(new Dimension(0, 64));
        setMinimumSize(new Dimension(0, 64));

        // "<" Previous scroll button
        btnPrev = createNavButton("<", "kriolos:sales:modern-two:cat-prev");
        btnPrev.addActionListener(e -> scrollCategories(-1));
        add(btnPrev, BorderLayout.LINE_START);

        // Center Pills Container
        pillsContainer = new JPanel();
        pillsContainer.setLayout(new BoxLayout(pillsContainer, BoxLayout.LINE_AXIS));
        pillsContainer.setOpaque(false);
        pillsContainer.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        scrollPane = new JScrollPane(pillsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Mouse wheel scroll support
        scrollPane.addMouseWheelListener(e -> {
            JViewport vp = scrollPane.getViewport();
            Point pos = vp.getViewPosition();
            int amount = e.getUnitsToScroll() * 24;
            int max = Math.max(0, pillsContainer.getWidth() - vp.getWidth());
            int targetX = Math.max(0, Math.min(pos.x + amount, max));
            vp.setViewPosition(new Point(targetX, 0));
        });

        // Touch drag swipe support
        MouseAdapter dragScroll = new MouseAdapter() {
            private Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                origin = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null) {
                    JViewport vp = scrollPane.getViewport();
                    Point pos = vp.getViewPosition();
                    int deltaX = origin.x - e.getX();
                    int max = Math.max(0, pillsContainer.getWidth() - vp.getWidth());
                    int targetX = Math.max(0, Math.min(pos.x + deltaX, max));
                    vp.setViewPosition(new Point(targetX, 0));
                }
            }
        };
        pillsContainer.addMouseListener(dragScroll);
        pillsContainer.addMouseMotionListener(dragScroll);

        add(scrollPane, BorderLayout.CENTER);

        // ">" Next scroll button
        btnNext = createNavButton(">", "kriolos:sales:modern-two:cat-next");
        btnNext.addActionListener(e -> scrollCategories(1));
        add(btnNext, BorderLayout.LINE_END);

        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        pillsContainer.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
    }

    private JButton createNavButton(String text, String name) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 18f));
        btn.setFocusPainted(false);
        Dimension navDim = new Dimension(44, PILL_HEIGHT);
        btn.setPreferredSize(navDim);
        btn.setMinimumSize(navDim);
        btn.setMaximumSize(navDim);
        btn.setMargin(new Insets(2, 4, 2, 4));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JComponent.roundRect", true);
        btn.setName(name);
        return btn;
    }

    private void scrollCategories(int direction) {
        JViewport vp = scrollPane.getViewport();
        Point pos = vp.getViewPosition();
        int extent = vp.getWidth();
        int scrollAmount = (extent > 0) ? (int) (extent * 0.75) : 250;
        int max = Math.max(0, pillsContainer.getWidth() - extent);
        int targetX = Math.max(0, Math.min(pos.x + (direction * scrollAmount), max));
        vp.setViewPosition(new Point(targetX, 0));
    }

    public void setCategories(List<CategoryInfo> categories) {
        pillsContainer.removeAll();
        categoryButtons.clear();

        // "(All)" pill button
        String allLabel = AppLocal.getIntString("label.all");
        if (allLabel == null || allLabel.startsWith("label.")) {
            allLabel = "Todos";
        }
        JToggleButton allButton = createPillButton(allLabel, null);
        allButton.setSelected(true);
        allButton.putClientProperty("FlatLaf.styleClass", "accent");
        buttonGroup.add(allButton);
        categoryButtons.add(allButton);
        pillsContainer.add(allButton);

        // Category pill buttons
        if (categories != null) {
            for (CategoryInfo cat : categories) {
                pillsContainer.add(Box.createHorizontalStrut(8));
                JToggleButton catButton = createPillButton(cat.getName(), cat);
                buttonGroup.add(catButton);
                categoryButtons.add(catButton);
                pillsContainer.add(catButton);
            }
        }

        pillsContainer.revalidate();
        pillsContainer.repaint();
    }

    private JToggleButton createPillButton(String text, CategoryInfo category) {
        JToggleButton button = new JToggleButton(text);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
        button.setMargin(new Insets(6, 18, 6, 18));

        Dimension pref = button.getPreferredSize();
        int btnWidth = Math.max(pref.width + 30, 90);
        Dimension fixedDim = new Dimension(btnWidth, PILL_HEIGHT);
        button.setPreferredSize(fixedDim);
        button.setMinimumSize(fixedDim);
        button.setMaximumSize(fixedDim);
        button.setAlignmentY(Component.CENTER_ALIGNMENT);

        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("category", category);
        button.setName("kriolos:sales:modern-two:category:" + (category != null ? category.getID() : "all"));

        button.addChangeListener(e -> {
            if (button.isSelected()) {
                button.putClientProperty("FlatLaf.styleClass", "accent");
            } else {
                button.putClientProperty("FlatLaf.styleClass", null);
            }
            button.repaint();
        });

        button.addActionListener(e -> {
            button.scrollRectToVisible(new Rectangle(0, 0, button.getWidth(), button.getHeight()));
            if (onCategorySelected != null) {
                onCategorySelected.accept(category);
            }
        });

        return button;
    }

    public void selectCategory(String categoryId) {
        for (JToggleButton btn : categoryButtons) {
            CategoryInfo cat = (CategoryInfo) btn.getClientProperty("category");
            if (categoryId == null && cat == null) {
                btn.setSelected(true);
                btn.scrollRectToVisible(new Rectangle(0, 0, btn.getWidth(), btn.getHeight()));
                if (onCategorySelected != null) {
                    onCategorySelected.accept(null);
                }
                break;
            } else if (categoryId != null && cat != null && categoryId.equals(cat.getID())) {
                btn.setSelected(true);
                btn.scrollRectToVisible(new Rectangle(0, 0, btn.getWidth(), btn.getHeight()));
                if (onCategorySelected != null) {
                    onCategorySelected.accept(cat);
                }
                break;
            }
        }
    }
}
