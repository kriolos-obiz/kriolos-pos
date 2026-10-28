import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class ModernSidebarFixed extends JFrame {

    public ModernSidebarFixed() {
        setTitle("Modern Sidebar UI - Fixed UI/UX");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Painel da Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(25, 27, 36));
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        // Ajustamos as bordas para dar um padding seguro à esquerda
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        // Título/Logo da App - MUDADO PARA LEFT_ALIGNMENT
        JLabel brandLabel = new JLabel("NEXUS CORE");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandLabel.setForeground(Color.WHITE);
        brandLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // FIX: Alinhado à esquerda de forma fixa
        brandLabel.setBorder(new EmptyBorder(10, 15, 30, 0)); // Padding interno igual ao dos botões
        sidebar.add(brandLabel);

        // Itens de Menu Simples
        sidebar.add(new SidebarButton("Dashboard", false, false));
        sidebar.add(Box.createVerticalStrut(8));

        // ITEM COM SUBMENU (Analytics)
        List<String> analyticsSubs = List.of("Realtime", "Reports", "User Behavior");
        sidebar.add(new SidebarDropdownMenu("Analytics", analyticsSubs));
        sidebar.add(Box.createVerticalStrut(8));

        // ITEM COM SUBMENU (Management)
        List<String> managementSubs = List.of("Users", "Permissions", "Logs");
        sidebar.add(new SidebarDropdownMenu("Management", managementSubs));
        sidebar.add(Box.createVerticalStrut(8));

        // Item Simples
        sidebar.add(new SidebarButton("Settings", false, false));

        sidebar.add(Box.createVerticalGlue());

        // 2. Painel de Conteúdo
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(new Color(240, 242, 245));
        contentPanel.setLayout(new GridBagLayout());
        
        JLabel mainTitle = new JLabel("Área de Conteúdo Principal");
        mainTitle.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        mainTitle.setForeground(new Color(60, 64, 67));
        contentPanel.add(mainTitle);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    // Componente Dropdown Completo
    private static class SidebarDropdownMenu extends JPanel {
        private final SidebarButton menuPai;
        private final JPanel painelFilhos;
        private boolean isOpen = false;

        public SidebarDropdownMenu(String tituloPai, List<String> subItens) {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setAlignmentX(Component.LEFT_ALIGNMENT); // FIX: Alinhamento fixo à esquerda do contentor

            menuPai = new SidebarButton(tituloPai, false, true);
            add(menuPai);

            painelFilhos = new JPanel();
            painelFilhos.setOpaque(false);
            painelFilhos.setLayout(new BoxLayout(painelFilhos, BoxLayout.Y_AXIS));
            painelFilhos.setAlignmentX(Component.LEFT_ALIGNMENT); // FIX: Alinhamento do subpainel interno
            painelFilhos.setBorder(new EmptyBorder(5, 15, 5, 0)); // Identação visual
            painelFilhos.setVisible(false);

            for (String sub : subItens) {
                SidebarButton subBtn = new SidebarButton(sub, false, false);
                subBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                subBtn.setForeground(new Color(130, 135, 155));
                painelFilhos.add(subBtn);
                painelFilhos.add(Box.createVerticalStrut(4));
            }

            add(painelFilhos);

            menuPai.addActionListener(e -> toggleMenu());
        }

        private void toggleMenu() {
            isOpen = !isOpen;
            painelFilhos.setVisible(isOpen);
            menuPai.setDropdownOpen(isOpen);
            revalidate();
            repaint();
        }
    }

    // Botão customizado adaptado
    private static class SidebarButton extends JButton {
        private boolean selected;
        private boolean hover;
        private final boolean isDropdown;
        private boolean isDropdownOpen = false;
        
        private final Color normalBg = new Color(25, 27, 36);
        private final Color hoverBg = new Color(38, 42, 55);
        private final Color selectedBg = new Color(67, 97, 238);
        private final Color normalText = new Color(160, 165, 186);
        private final Color selectedText = Color.WHITE;

        public SidebarButton(String text, boolean isSelected, boolean isDropdown) {
            super(text);
            this.selected = isSelected;
            this.isDropdown = isDropdown;
            
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            
            // --- FIX DE LARGURA E ALINHAMENTO ---
            setAlignmentX(Component.LEFT_ALIGNMENT); // FIX: Garante ancoragem absoluta à esquerda
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            // ------------------------------------

            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(12, 15, 12, 15));
            setForeground(selected ? selectedText : normalText);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!isDropdown) {
                        limparSelecoesGlobais(getTopLevelAncestor());
                        setSelected(true);
                    }
                }
            });
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            setForeground(selected ? selectedText : normalText);
            repaint();
        }

        public void setDropdownOpen(boolean open) {
            this.isDropdownOpen = open;
            repaint();
        }

        private void limparSelecoesGlobais(Component c) {
            if (c instanceof SidebarButton && !((SidebarButton) c).isDropdown) {
                ((SidebarButton) c).setSelected(false);
            }
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) {
                    limparSelecoesGlobais(child);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (selected) g2.setColor(selectedBg);
            else if (hover) g2.setColor(hoverBg);
            else g2.setColor(normalBg);

            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            
            if (isDropdown) {
                g2.setColor(normalText);
                int size = 6;
                int x = getWidth() - 25;
                int y = (getHeight() / 2) - 3;

                if (isDropdownOpen) {
                    int[] xPoints = {x, x + size, x + (size * 2)};
                    int[] yPoints = {y + size, y, y + size};
                    g2.fillPolygon(xPoints, yPoints, 3);
                } else {
                    int[] xPoints = {x, x + size, x + (size * 2)};
                    int[] yPoints = {y, y + size, y};
                    g2.fillPolygon(xPoints, yPoints, 3);
                }
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ModernSidebarFixed().setVisible(true));
    }
}
