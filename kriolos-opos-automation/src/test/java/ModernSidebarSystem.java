
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ModernSidebarSystem extends JFrame {

    private boolean isSidebarExpanded = true;
    private final JPanel sidebarPanel;
    private final JLabel brandLabel;
    private final JButton toggleButton;
    private final List<JComponent> renderedComponents = new ArrayList<>();

    // Definição Estática de Dados Separada usando o Builder Pattern
    private final List<MenuItemModel> menuDeclarations = List.of(
            new MenuItemModel.Builder("dash", "Dashboard").withIcon("📊").build(),
            new MenuItemModel.Builder("analytics", "Analytics")
                    .withIcon("📈")
                    .withSubmenus(List.of(
                            new MenuItemModel.Builder("realtime", "Realtime").build(),
                            new MenuItemModel.Builder("reports", "Reports").build()
                    )).build(),
            new MenuItemModel.Builder("mgmt", "Management")
                    .withIcon("⚙️")
                    .withSubmenus(List.of(
                            new MenuItemModel.Builder("users", "Users").build(),
                            new MenuItemModel.Builder("logs", "Logs").build()
                    )).build(),
            new MenuItemModel.Builder("settings", "Settings").withIcon("🔧").build()
    );

    public ModernSidebarSystem() {
        setTitle("Enterprise Architecture Sidebar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Instanciação da Sidebar Principal
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(new Color(25, 27, 36));
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(new EmptyBorder(20, 15, 20, 15));

        // Cabeçalho da Sidebar (Brand + Botão de Toggle Dinâmico)
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandLabel = new JLabel("NEXUS CORE");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandLabel.setForeground(Color.WHITE);

        toggleButton = new JButton("◀");
        toggleButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toggleButton.setForeground(new Color(160, 165, 186));
        toggleButton.setContentAreaFilled(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setFocusPainted(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.addActionListener(e -> toggleSidebarState());

        headerPanel.add(brandLabel, BorderLayout.WEST);
        headerPanel.add(toggleButton, BorderLayout.EAST);
        sidebarPanel.add(headerPanel);
        sidebarPanel.add(Box.createVerticalStrut(30));

        // 2. Renderização Automática baseada no Modelo de Dados (A separação pedida)
        renderMenuStructure();

        sidebarPanel.add(Box.createVerticalGlue());

        // Painel Central Contentor
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(240, 242, 245));
        JLabel centralText = new JLabel("Área de Trabalho Principal");
        centralText.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        contentPanel.add(centralText);

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void renderMenuStructure() {
        for (MenuItemModel model : menuDeclarations) {
            if (model.isDropdown()) {
                SidebarDropdownRender dropdown = new SidebarDropdownRender(model, () -> isSidebarExpanded);
                sidebarPanel.add(dropdown);
                renderedComponents.add(dropdown);
            } else {
                SidebarButtonRender button = new SidebarButtonRender(model, false, false, () -> isSidebarExpanded);
                sidebarPanel.add(button);
                renderedComponents.add(button);
            }
            sidebarPanel.add(Box.createVerticalStrut(8));
        }
    }

    private void toggleSidebarState() {
        isSidebarExpanded = !isSidebarExpanded;

        // Atualizar Layout da Estrutura
        if (isSidebarExpanded) {
            sidebarPanel.setPreferredSize(new Dimension(260, 0));
            brandLabel.setVisible(true);
            toggleButton.setText("◀");
            sidebarPanel.setBorder(new EmptyBorder(20, 15, 20, 15));
        } else {
            sidebarPanel.setPreferredSize(new Dimension(75, 0));
            brandLabel.setVisible(false);
            toggleButton.setText("▶");
            sidebarPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
        }

        // Propagar atualização de estado para os componentes renderizados
        for (JComponent comp : renderedComponents) {
            if (comp instanceof SidebarDropdownRender dropdown) {
                dropdown.updateViewMode();
            } else if (comp instanceof SidebarButtonRender btn) {
                btn.updateViewMode();
            }
        }

        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }

    // Provedor Funcional para checar estado de expansão de forma limpa
    @FunctionalInterface
    interface SidebarStateProvider {

        boolean isExpanded();
    }

    // --- COMPONENTES DE RENDERIZAÇÃO GRÁFICA (VIEW) ---
    private static class SidebarDropdownRender extends JPanel {

        private final MenuItemModel model;
        private final SidebarButtonRender masterButton;
        private final JPanel childPanel;
        private final SidebarStateProvider stateProvider;
        private boolean isOpen = false;

        public SidebarDropdownRender(MenuItemModel model, SidebarStateProvider stateProvider) {
            this.model = model;
            this.stateProvider = stateProvider;

            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            masterButton = new SidebarButtonRender(model, false, true, stateProvider);
            add(masterButton);

            childPanel = new JPanel();
            childPanel.setOpaque(false);
            childPanel.setLayout(new BoxLayout(childPanel, BoxLayout.Y_AXIS));
            childPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            childPanel.setBorder(new EmptyBorder(5, 20, 5, 0));
            childPanel.setVisible(false);

            for (MenuItemModel sub : model.submenus()) {
                SidebarButtonRender subBtn = new SidebarButtonRender(sub, false, false, stateProvider);
                subBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                subBtn.setForeground(new Color(130, 135, 155));
                childPanel.add(subBtn);
                childPanel.add(Box.createVerticalStrut(4));
            }
            add(childPanel);

            masterButton.addActionListener(e -> {
                if (stateProvider.isExpanded()) {
                    isOpen = !isOpen;
                    childPanel.setVisible(isOpen);
                    masterButton.setDropdownIndicatorOpen(isOpen);
                    revalidate();
                    repaint();
                }
            });
        }

        public void updateViewMode() {
            masterButton.updateViewMode();
            if (!stateProvider.isExpanded()) {
                childPanel.setVisible(false); // Força fechar submenus no modo minimal
            } else {
                childPanel.setVisible(isOpen);
            }
        }
    }

    private static class SidebarButtonRender extends JButton {

        private final MenuItemModel model;
        private final boolean isDropdown;
        private final SidebarStateProvider stateProvider;
        private boolean isDropdownIndicatorOpen = false;
        private boolean isHovered = false;
        private boolean isSelected;

        private final Color normalBg = new Color(25, 27, 36);
        private final Color hoverBg = new Color(38, 42, 55);
        private final Color selectedBg = new Color(67, 97, 238);
        private final Color textExpandedColor = new Color(160, 165, 186);

        public SidebarButtonRender(MenuItemModel model, boolean isSelected, boolean isDropdown, SidebarStateProvider stateProvider) {
            super(model.title());
            this.model = model;
            this.isDropdown = isDropdown;
            this.isSelected = isSelected;
            this.stateProvider = stateProvider;

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.LEFT);

            updateViewMode();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (!isDropdown) {
                        clearGlobalSelections(getTopLevelAncestor());
                        setSelectedState(true);
                    }
                }
            });
        }

        public void setSelectedState(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        public void setDropdownIndicatorOpen(boolean open) {
            this.isDropdownIndicatorOpen = open;
            repaint();
        }

        public void updateViewMode() {
            if (stateProvider.isExpanded()) {
                setText(model.title());
                setIcon(null);
                setBorder(new EmptyBorder(12, 15, 12, 15));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            } else {
                setText("");
                setBorder(new EmptyBorder(12, 0, 12, 0));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            }
            repaint();
        }

        private void clearGlobalSelections(Component c) {
            if (c instanceof SidebarButtonRender btn && !btn.isDropdown) {
                btn.setSelectedState(false);
            }
            if (c instanceof Container container) {
                for (Component child : container.getComponents()) {
                    clearGlobalSelections(child);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Desenhar Fundo do Botão baseado no Estado
            if (isSelected) {
                g2.setColor(selectedBg);
            } else if (isHovered) {
                g2.setColor(hoverBg);
            } else {
                g2.setColor(normalBg);
            }

            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));

            // 2. Determinar o Ícone a ser exibido (Emoji Original ou Letra Inicial)
            String activeIcon = model.icon();
            boolean isGeneratedFallback = false;

            if (activeIcon == null || activeIcon.trim().isEmpty()) {
                if (model.title() != null && !model.title().isEmpty()) {
                    // Extrai a primeira letra e transforma em Maiúscula (MAJOR)
                    activeIcon = String.valueOf(model.title().charAt(0)).toUpperCase();
                    isGeneratedFallback = true;
                } else {
                    activeIcon = "?";
                }
            }

            // Métricas de posicionamento vertical centralizado
            int centerY = (getHeight() / 2) + (g2.getFontMetrics().getAscent() / 2) - 2;

            // 3. Renderização Dinâmica dependendo do Modo (Expanded vs Minimal)
            if (stateProvider.isExpanded()) {
                // Se for uma letra gerada automaticamente, damos um tom mais suave ou realce
                if (isGeneratedFallback) {
                    g2.setColor(isSelected ? Color.WHITE : new Color(110, 115, 135));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                    g2.drawString(activeIcon, 18, centerY);

                    // Restaura fonte original para o título
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    g2.setColor(isSelected ? Color.WHITE : textExpandedColor);
                    g2.drawString(model.title(), 45, centerY);
                } else {
                    // Ícone nativo (Emoji/Texto fornecido no Record)
                    g2.setColor(Color.WHITE);
                    g2.drawString(activeIcon, 15, centerY);
                    g2.setColor(isSelected ? Color.WHITE : textExpandedColor);
                    g2.drawString(model.title(), 45, centerY);
                }

                // Desenhar Indicador de Dropdown (Seta)
                if (isDropdown) {
                    g2.setColor(textExpandedColor);
                    int arrowSize = 5;
                    int arrowX = getWidth() - 25;
                    int arrowY = (getHeight() / 2) - 2;
                    if (isDropdownIndicatorOpen) {
                        g2.fillPolygon(new int[]{arrowX, arrowX + arrowSize, arrowX + (arrowSize * 2)}, new int[]{arrowY + arrowSize, arrowY, arrowY + arrowSize}, 3);
                    } else {
                        g2.fillPolygon(new int[]{arrowX, arrowX + arrowSize, arrowX + (arrowSize * 2)}, new int[]{arrowY, arrowY + arrowSize, arrowY}, 3);
                    }
                }
            } else {
                // MODO MINIMAL: Centralização Geométrica Estrita no Meio do Botão Encolhido
                if (isGeneratedFallback) {
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Letra ligeiramente maior no modo compacto
                    g2.setColor(isSelected ? Color.WHITE : new Color(140, 145, 165));
                } else {
                    g2.setColor(isSelected ? Color.WHITE : textExpandedColor);
                }

                int iconWidth = g2.getFontMetrics().stringWidth(activeIcon);
                int iconX = (getWidth() - iconWidth) / 2;
                g2.drawString(activeIcon, iconX, centerY);
            }

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ModernSidebarSystem().setVisible(true));
    }
}
