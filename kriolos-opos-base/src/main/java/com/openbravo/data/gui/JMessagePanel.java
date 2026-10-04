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
package com.openbravo.data.gui;

import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.forms.AppLocal;
import java.awt.*;
import javax.swing.*;

/**
 * Message and exception alert panel presentable via {@link PosUIModal} or
 * embedded directly into views. Refactored for modern touch-screen ergonomics
 * inspired by Adobe Spectrum Design System. 100% Cross-Platform safe and
 * Look-and-Feel agnostic.
 *
 * @author KriolOS
 */
public class JMessagePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private int optionChosed = Integer.MIN_VALUE;
    private PosUIModal modalContext;

    // Componentes de UI modernos e limpos
    private JPanel headerContainer;
    private JPanel centerContainer;
    private JPanel actionButtonContainer;

    private JLabel jlblIcon;
    private JTextArea jlblMessage; // Mudado para JTextArea para quebra automática de texto sem HTML
    private JScrollPane jscrException;
    private JTextArea jtxtException;

    private JButton jcmdOK;
    private JButton jcmdCancel;
    private JButton jcmdMore;

        /**
     * Creates new form JMessagePanel
     */
    public JMessagePanel() {
        initComponents(null);
        initDomainAdapters();
        jscrException.setVisible(false);
    }

    /**
     * Creates new form JMessagePanel configured with message information.
     */
    public JMessagePanel(MessageInf inf, boolean showConfirm) {
        initComponents(inf);
        initDomainAdapters();
        initContent(inf, showConfirm);
    }


    private void initDomainAdapters() {
        // Namespace identifiers mantidos para manter a compatibilidade com o ecossistema KriolOS
        setName("kriolos:message:panel");
        jlblIcon.setName("kriolos:message:lbl-icon");
        jlblMessage.setName("kriolos:message:lbl-text");
        jtxtException.setName("kriolos:message:txt-exception");
        jcmdOK.setName("kriolos:message:btn-ok");
        jcmdCancel.setName("kriolos:message:btn-cancel");
        jcmdMore.setName("kriolos:message:btn-more");
    }

    public void initContent(MessageInf inf, boolean showConfirm) {
        jscrException.setVisible(false);

        if (inf != null) {
            // Se o MessageInf fornecer um código ou título numérico, injetamos no título
            String codeText = (inf.getCode() != null ? inf.getCode() + " " : "")
                    + (inf.getErrorCodeMsg() != null ? inf.getErrorCodeMsg() : "");

            if (!codeText.trim().isEmpty()) {
                jlblIcon.setText(codeText);
            } else {
                jlblIcon.setText(AppLocal.getIntString("title.message"));
            }

            jlblMessage.setText(inf.getMessage());

            if (inf.getCause() == null) {
                jtxtException.setText(null);
                jcmdMore.setVisible(false);
            } else {
                jcmdMore.setVisible(true);
                StringBuilder sb = new StringBuilder();

                if (inf.getCause() instanceof Throwable) {
                    Throwable t = (Throwable) inf.getCause();
                    while (t != null) {
                        sb.append(t.getClass().getName());
                        sb.append(": \n");
                        sb.append(t.getMessage());
                        sb.append("\n\n");
                        t = t.getCause();
                    }
                } else if (inf.getCause() instanceof Throwable[]) {
                    Throwable[] m_aExceptions = (Throwable[]) inf.getCause();
                    for (Throwable ex : m_aExceptions) {
                        sb.append(ex.getClass().getName());
                        sb.append(": \n");
                        sb.append(ex.getMessage());
                        sb.append("\n\n");
                    }
                } else if (inf.getCause() instanceof Object[]) {
                    Object[] m_aObjects = (Object[]) inf.getCause();
                    for (Object obj : m_aObjects) {
                        sb.append(obj.toString());
                        sb.append("\n\n");
                    }
                } else if (inf.getCause() instanceof String) {
                    sb.append(inf.getCause().toString());
                } else {
                    sb.append(inf.getCause().getClass().getName());
                    sb.append(": \n");
                    sb.append(inf.getCause().toString());
                }
                jtxtException.setText(sb.toString());
            }
            jtxtException.setCaretPosition(0);
        }

        jcmdCancel.setEnabled(showConfirm);
        jcmdCancel.setVisible(showConfirm);

        // Re-arranja a estrutura caso o botão "Mais Detalhes" fique invisível
        actionButtonContainer.revalidate();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root != null) {
            root.setDefaultButton(jcmdOK);
        }
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public String getTitle() {
        return AppLocal.getIntString("title.message");
    }

    public JButton getOkButton() {
        return jcmdOK;
    }

    public int getOptionChosed() {
        return optionChosed;
    }

    private void handleToggleExceptionDetails() {
        jcmdMore.setEnabled(true);
        jscrException.setVisible(!jscrException.isVisible());

        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            w.pack();
            w.revalidate();
            w.repaint();
        } else {
            revalidate();
            repaint();
        }
    }

    private void handleAccept() {
        this.optionChosed = 0;
        if (modalContext != null) {
            modalContext.setResult(this.optionChosed);
            modalContext.close();
        }
    }

    private void handleCancel() {
        this.optionChosed = -1;
        if (modalContext != null) {
            modalContext.setResult(this.optionChosed);
            modalContext.close();
        }
    }

    public static void showMessage(Component parent, MessageInf inf) {
        createMessagePanel(parent, inf, false);
    }

    public static int showConfirmDialog(Component parent, MessageInf inf) {
        return createMessagePanel(parent, inf, true);
    }

    private static int createMessagePanel(Component parent, MessageInf inf, boolean showConfirm) {
        JMessagePanel panel = new JMessagePanel(inf, showConfirm);
        if (parent != null) {
            panel.applyComponentOrientation(parent.getComponentOrientation());
        }

        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(panel.getTitle())
                .setModal(true)
                .setResizable(false);

        panel.setModalContext(modal);
        modal.show();

        return panel.getOptionChosed();
    }

    /**
     * Component Initialization block optimized for Touch Desktop POS. Replaces
     * standard NetBeans code with Adobe Spectrum token mappings using absolute
     * cross-platform safe layouts.
     */
        /**
     * Inicialização de componentes otimizada para toque tátil e adaptada aos
     * estados semânticos do Adobe Spectrum com base no código do MessageInf.
     */
    private void initComponents(MessageInf inf) {
        // 1. Determinar o nível de sinal do MessageInf usando máscara de bits
        int signal = (inf != null) ? (inf.getMessageCode() & 0xFF000000) : MessageInf.SGN_WARNING;
        
        // 2. Mapeamento dinâmico de cores Adobe Spectrum de acordo com o sinal
        Color spectrumBg = this.getBackground();
        Color spectrumBorder;
        Color spectrumTextDark;
        Color spectrumPrimaryButtonBg;
        Color spectrumPrimaryButtonFg = Color.WHITE;
        
        switch (signal) {
            case MessageInf.SGN_DANGER: // Erro Crítico / Bloqueio
                spectrumBg = new Color(255, 242, 242);       // Muted Red Fondo
                spectrumBorder = new Color(211, 47, 47);      // Adobe Crimson Border
                spectrumTextDark = new Color(74, 12, 12);
                spectrumPrimaryButtonBg = new Color(211, 47, 47);
                break;
                
            case MessageInf.SGN_SUCCESS: // Operação Concluída com Sucesso
                spectrumBg = new Color(242, 252, 244);       // Muted Green Fondo
                spectrumBorder = new Color(46, 125, 50);      // Adobe Celadon/Green
                spectrumTextDark = new Color(12, 54, 15);
                spectrumPrimaryButtonBg = new Color(46, 125, 50);
                break;
                
            case MessageInf.SGN_NOTICE:
            case MessageInf.SGN_IMPORTANT: // Informações de Sistema
                spectrumBg = new Color(240, 247, 255);       // Muted Blue Fondo
                spectrumBorder = new Color(2, 106, 194);      // Adobe Seafoam/Blue
                spectrumTextDark = new Color(0, 43, 91);
                spectrumPrimaryButtonBg = new Color(2, 106, 194);
                break;
                
            case MessageInf.SGN_CAUTION:
            case MessageInf.SGN_WARNING:
            default: // Padrão: Aviso / Atenção
                spectrumBg = new Color(255, 248, 242);       // Muted Amber Fondo
                spectrumBorder = new Color(224, 107, 23);     // Adobe Orange
                spectrumTextDark = new Color(74, 33, 4);
                spectrumPrimaryButtonBg = new Color(224, 107, 23);
                break;
        }
        //reset to default before apply (solution to work in dard until final solution)
        spectrumBg = this.getBackground();
        spectrumTextDark = new Label().getForeground();

        // Configuração do Content Card Principal
        this.setBackground(spectrumBg);
        this.setLayout(new BorderLayout(0, 16));
        this.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(spectrumBorder, 1, false),
            new javax.swing.border.EmptyBorder(20, 20, 20, 20)
        ));

        // --- TOPO: HEADER CONTAINER (Ícone Dinâmico Vetorial + Título) ---
        headerContainer = new JPanel(new BorderLayout(16, 0));
        headerContainer.setOpaque(false);

        // Canvas vetorial que adapta a forma geométrica interna ao tipo de alerta
        JPanel iconCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(spectrumBorder);
                
                if (signal == MessageInf.SGN_DANGER) {
                    // Octógono ou Escudo de erro para perigo crítico
                    int[] xPoints = {10, 26, 36, 36, 26, 10, 0, 0};
                    int[] yPoints = {0, 0, 10, 26, 36, 36, 26, 10};
                    g2.fillPolygon(xPoints, yPoints, 8);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
                    g2.drawString("X", 12, 25);
                } else if (signal == MessageInf.SGN_SUCCESS) {
                    // Círculo de validação com checkmark para sucesso
                    g2.fillOval(0, 0, 36, 36);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(10, 18, 16, 24);
                    g2.drawLine(16, 24, 26, 12);
                } else if (signal == MessageInf.SGN_NOTICE || signal == MessageInf.SGN_IMPORTANT) {
                    // Círculo de Informação (i)
                    g2.fillOval(0, 0, 36, 36);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
                    g2.drawString("i", 16, 25);
                } else {
                    // Triângulo Clássico de Aviso para SGN_WARNING / SGN_CAUTION
                    int[] xPoints = {18, 0, 36};
                    int[] yPoints = {0, 32, 32};
                    g2.fillPolygon(xPoints, yPoints, 3);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
                    g2.drawString("!", 16, 24);
                }
            }
        };
        iconCanvas.setPreferredSize(new Dimension(36, 36));
        iconCanvas.setOpaque(false);
        headerContainer.add(iconCanvas, BorderLayout.WEST);

        jlblIcon = new JLabel();
        jlblIcon.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
        jlblIcon.setForeground(spectrumTextDark);
        headerContainer.add(jlblIcon, BorderLayout.CENTER);

        this.add(headerContainer, BorderLayout.NORTH);

        // --- CENTRO: CENTER CONTAINER ---
        centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setOpaque(false);

        jlblMessage = new JTextArea();
        jlblMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        jlblMessage.setForeground(spectrumTextDark);
        jlblMessage.setEditable(false);
        jlblMessage.setFocusable(false);
        jlblMessage.setOpaque(false);
        jlblMessage.setLineWrap(true);
        jlblMessage.setWrapStyleWord(true);
        jlblMessage.setBorder(new javax.swing.border.EmptyBorder(4, 4, 4, 4));
        centerContainer.add(jlblMessage, BorderLayout.NORTH);

        jtxtException = new JTextArea();
        jtxtException.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        //jtxtException.setBackground(new Color(245, 240, 235));
        //jtxtException.setForeground(new Color(40, 40, 40));
        jtxtException.setEditable(false);
        jtxtException.setLineWrap(true);
        jtxtException.setWrapStyleWord(true);

        jscrException = new JScrollPane(jtxtException);
        //jscrException.setBorder(BorderFactory.createLineBorder(new Color(210, 200, 190), 1));
        jscrException.setPreferredSize(new Dimension(450, 150));
        centerContainer.add(jscrException, BorderLayout.CENTER);

        this.add(centerContainer, BorderLayout.CENTER);

        // --- BASE: SOUTH CONTAINER ---
        JPanel southContainer = new JPanel(new GridBagLayout());
        southContainer.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        jcmdMore = new JButton(AppLocal.getIntString("button.information")); // Exemplo usando ficheiro de recursos nativo
        //styleSpectrumButton(jcmdMore, new Color(245, 240, 235), spectrumTextDark, false);
        styleSpectrumButton(jcmdMore, jcmdMore.getBackground(), jcmdMore.getForeground(), false);
        jcmdMore.addActionListener(e -> handleToggleExceptionDetails());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        southContainer.add(jcmdMore, gbc);

        actionButtonContainer = new JPanel(new GridLayout(1, 0, 12, 0));
        actionButtonContainer.setOpaque(false);

        jcmdCancel = new JButton(AppLocal.getIntString("button.cancel"));
        styleSpectrumButton(jcmdCancel, new Color(230, 230, 230), Color.BLACK, false);
        jcmdCancel.addActionListener(e -> handleCancel());
        actionButtonContainer.add(jcmdCancel);

        jcmdOK = new JButton(AppLocal.getIntString("button.ok"));
        styleSpectrumButton(jcmdOK, spectrumPrimaryButtonBg, spectrumPrimaryButtonFg, true);
        jcmdOK.addActionListener(e -> handleAccept());
        actionButtonContainer.add(jcmdOK);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        southContainer.add(actionButtonContainer, gbc);

        this.add(southContainer, BorderLayout.SOUTH);
    }


    /**
     * Aplica regras visuais e mecânicas de toque do Adobe Spectrum a JButtons
     * nativos. Sobrescreve comportamentos nativos dos Look and Feels para
     * assegurar uniformidade cross-platform.
     */
    private void styleSpectrumButton(JButton btn, Color bg, Color fg, boolean isPrimary) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);

        // Altura mínima garantida de 52px via insets estruturais para dedos em ecrãs touch
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isPrimary ? bg.darker() : new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(15, 28, 15, 28)
        ));

        // Feedback visual tátil de clique via UI model agnóstico
        btn.getModel().addChangeListener(e -> {
            if (btn.getModel().isPressed()) {
                btn.setBackground(bg.darker());
            } else if (btn.getModel().isRollover()) {
                btn.setBackground(bg.brighter());
            } else {
                btn.setBackground(bg);
            }
        });
    }
}