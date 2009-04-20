/*
 * BattleWindow.java
 *
 * Created on Apr 7, 2009, 11:51:16 PM
 *
 * This file is a part of Shoddy Battle.
 * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, visit the Free Software Foundation, Inc.
 * online at http://gnu.org.
 */

package shoddybattleclient;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import shoddybattleclient.GameVisualisation.VisualPokemon;
import shoddybattleclient.shoddybattle.*;
import shoddybattleclient.utils.*;

/**
 *
 * @author ben
 */
public class BattleWindow extends javax.swing.JFrame {

    private static class Move {
        private String m_name;
        private int m_pp;
        private int m_maxPp;
        private String m_type;
        public Move(String name, String type, int maxPp) {
            m_name = name;
            m_type = type;
            m_maxPp = maxPp;
            m_pp = maxPp;
        }
        public String getName() {
            return m_name;
        }
        public String getType() {
            return m_type;
        }
        public String getPp() {
            return m_pp + "/" + m_maxPp;
        }
    }

    private static class MoveButton extends JToggleButton {
        private Move m_move = null;
        public MoveButton() {
            setFocusPainted(false);
        }
        public void setMove(Move move) {
            m_move = move;
            repaint();
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (m_move == null) return;
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!isEnabled()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            }
            g2.setFont(g2.getFont().deriveFont(Font.BOLD).deriveFont(17f));
            g2.drawString(m_move.getName(), 10, 25);
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN).deriveFont(12f));
            int y = getHeight() - g2.getFontMetrics().getHeight();
            g2.drawString(m_move.getType(), 10, y);
            String pp = m_move.getPp();
            int left = getWidth() - g2.getFontMetrics().stringWidth(pp) - 5;
            g2.drawString(m_move.getPp(), left, y);
            g2.dispose();
        }
    }

    private static class SwitchButton extends JToggleButton {
        //TODO: make this more advanced than a String
        private String m_pokemon = null;
        public SwitchButton() {
            this.setFocusPainted(false);
        }
        public void setPokemon(String pokemon) {
            m_pokemon = pokemon;
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g.create();
            super.paintComponent(g2);
            if (m_pokemon == null) return;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!isEnabled()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            }
            g2.setFont(g2.getFont().deriveFont(Font.BOLD));
            g2.drawString(m_pokemon, 5, getHeight() / 2 - g2.getFontMetrics().getHeight() / 2 + 7);
            g2.dispose();
        }
    }

    private MoveButton[] m_moveButtons;
    private SwitchButton[] m_switches;
    private GameVisualisation m_visual;
    private HealthBar[] m_healthBars = new HealthBar[2];
    private HTMLPane m_chat;
    private ArrayList<PokemonMove> m_moveList;
    //Pokemon in your party
    private String[] m_pokemon;
    //Users in this match
    private String[] m_users;
    //Moves for each of your pokemon
    private String[][] m_moves;
    //Your participant number in this battle
    private int m_participant;
    //This battles field ID
    private int m_fid;
    //if we are forced to make a certain move
    private boolean m_forced = false;

    /** Creates new form BattleWindow */
    public BattleWindow(int fid, int participant, String[] users, String[][] moves, String[] party) {
        initComponents();

        setTitle(users[0] + " vs. " + users[1] + " - Shoddy Battle");

        m_fid = fid;
        m_participant = participant;
        m_users = users;
        m_moves = moves;
        m_pokemon = party;

        listUsers.setModel(new UserListModel(new ArrayList()));
        setUsers(users);
        if (m_participant == 0) {
            lblPlayer0.setText(users[0]);
            lblPlayer1.setText(users[1]);
        } else {
            lblPlayer0.setText(users[1]);
            lblPlayer1.setText(users[0]);
        }
        m_chat = new HTMLPane("Ben");
        scrollChat.add(m_chat);
        scrollChat.setViewportView(m_chat);

        MoveListParser mlp = new MoveListParser();
        m_moveList = mlp.parseDocument(BattleWindow.class.getResource("resources/moves2.xml").toString());

        createButtons();
        setupVisual();
        setMoves(0);
        updateSwitches();
    }

    private void createButtons() {
        m_moveButtons = new MoveButton[4];
        panelMoves.setLayout(new GridLayout(2, 2));
        ButtonGroup moveButtons = new ButtonGroup();
        for (int i = 0; i < m_moveButtons.length; i++) {
            final int idx = i;
            final MoveButton button = new MoveButton();
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!button.isEnabled()) return;
                    if (e.getClickCount() == 2) {
                        sendMove(idx);
                    }
                }
            });
            moveButtons.add(button);
            m_moveButtons[i] = button;
            panelMoves.add(button);
        }

        ButtonGroup switchButtons = new ButtonGroup();
        panelSwitch.setLayout(new GridLayout(3, 2));
        m_switches = new SwitchButton[6];
        for (int i = 0; i < m_switches.length; i++) {
            final int idx = i;
            final SwitchButton button = new SwitchButton();
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!button.isEnabled()) return;
                    if (e.getClickCount() == 2) {
                        sendSwitch(idx);
                    }
                }
            });
            switchButtons.add(button);
            m_switches[i] = button;
            panelSwitch.add(button);
        }
    }

    public void setUsers(String[] users) {
        listUsers.setModel(new UserListModel(new ArrayList(Arrays.asList(users))));
    }

    private void setMoves(int i) {
        for (int j = 0; j < m_moveButtons.length; j++) {
            setMove(j, m_moves[i][j]);
        }
    }

    private void setupVisual() {
        m_visual = new GameVisualisation(0);
        m_visual.setSize(m_visual.getPreferredSize());
        int base = 15;
        int buffer = 5;
        int healthHeight = 35;
        int x = 20;
        m_visual.setLocation(x, base + healthHeight + buffer);
        m_healthBars[0] = new HealthBar();
        m_healthBars[0].setLocation(x, base);
        m_healthBars[0].setSize(m_visual.getWidth(), healthHeight);
        m_healthBars[1] = new HealthBar();
        m_healthBars[1].setLocation(x, base + healthHeight + (2 * buffer) + m_visual.getHeight());
        m_healthBars[1].setSize(m_visual.getWidth(), healthHeight);
        add(m_healthBars[0]);
        add(m_healthBars[1]);
        add(m_visual);
    }

    public void setMove(int idx, String name) {
        if ((idx < 0) || (idx >= m_moveButtons.length)) return;
        for (PokemonMove move : m_moveList) {
            if (move.name.equals(name)) {
                m_moveButtons[idx].setMove(new Move(name, move.type, move.pp));
                break;
            }
        }
    }

    public void requestMove() {
        btnMove.setEnabled(true);
        btnSwitch.setEnabled(true);
        btnMoveCancel.setEnabled(false);
        btnSwitchCancel.setEnabled(false);
        tabAction.setSelectedIndex(0);
    }

    public void requestReplacement() {
        btnMove.setEnabled(false);
        btnMoveCancel.setEnabled(false);
        btnSwitch.setEnabled(true);
        btnSwitchCancel.setEnabled(false);
        tabAction.setSelectedIndex(1);
    }

    public void requestTarget() {
        //do something for targetting here
    }

    private void sendMove(int idx) {
        if (!btnMove.isEnabled()) return;
        System.out.println("Used move " + idx);
        btnMove.setEnabled(false);
        btnMoveCancel.setEnabled(true);
    }

    private void sendSwitch(int idx) {
        if (!btnSwitch.isEnabled()) return;
        System.out.println("Switched to " + idx);
        btnSwitch.setEnabled(false);
        btnSwitchCancel.setEnabled(true);
    }

    public void setValidMoves(boolean[] valid) {
        boolean struggle = true;
        for (int i = 0; i < m_moveButtons.length; i++) {
            m_moveButtons[i].setEnabled(valid[i]);
            if (valid[i]) struggle = false;
        }
        if (struggle && !m_forced) {
            btnMove.setText("Struggle");
        }
    }

    public void setValidSwitches(boolean[] valid) {
        for (int i = 0; i < m_switches.length; i++) {
            m_switches[i].setEnabled(valid[i]);
        }
    }

    private void updateSwitches() {
        for (int i = 0; i < m_switches.length; i++) {
            m_switches[i].setPokemon(m_pokemon[i]);
        }
    }

    public void setPokemon(VisualPokemon[] p1, VisualPokemon[] p2) {
        m_visual.setParties(p1, p2);
    }

    public void setForced(boolean forced) {
        m_forced = forced;
        if (forced) {
            setValidMoves(new boolean[] {false, false, false, false});
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtChat = new javax.swing.JTextField();
        tabAction = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        panelMoves = new javax.swing.JPanel();
        btnMove = new javax.swing.JButton();
        btnMoveCancel = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        panelSwitch = new javax.swing.JPanel();
        btnSwitch = new javax.swing.JButton();
        btnSwitchCancel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listUsers = new javax.swing.JList();
        scrollChat = new javax.swing.JScrollPane();
        lblPlayer0 = new javax.swing.JLabel();
        lblPlayer1 = new javax.swing.JLabel();
        lblClock0 = new javax.swing.JLabel();
        lblClock1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocationByPlatform(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        txtChat.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtChatFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtChatFocusLost(evt);
            }
        });
        txtChat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtChatKeyReleased(evt);
            }
        });

        jPanel4.setOpaque(false);

        panelMoves.setOpaque(false);

        org.jdesktop.layout.GroupLayout panelMovesLayout = new org.jdesktop.layout.GroupLayout(panelMoves);
        panelMoves.setLayout(panelMovesLayout);
        panelMovesLayout.setHorizontalGroup(
            panelMovesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 335, Short.MAX_VALUE)
        );
        panelMovesLayout.setVerticalGroup(
            panelMovesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 142, Short.MAX_VALUE)
        );

        btnMove.setText("Attack");
        btnMove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveActionPerformed(evt);
            }
        });

        btnMoveCancel.setText("Cancel");
        btnMoveCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveCancelActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(btnMove, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 165, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(btnMoveCancel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
            .add(panelMoves, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .add(panelMoves, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnMove)
                    .add(btnMoveCancel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel4Layout.linkSize(new java.awt.Component[] {btnMove, btnMoveCancel}, org.jdesktop.layout.GroupLayout.VERTICAL);

        tabAction.addTab("Move", jPanel4);

        jPanel3.setOpaque(false);

        panelSwitch.setOpaque(false);

        org.jdesktop.layout.GroupLayout panelSwitchLayout = new org.jdesktop.layout.GroupLayout(panelSwitch);
        panelSwitch.setLayout(panelSwitchLayout);
        panelSwitchLayout.setHorizontalGroup(
            panelSwitchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 335, Short.MAX_VALUE)
        );
        panelSwitchLayout.setVerticalGroup(
            panelSwitchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 142, Short.MAX_VALUE)
        );

        btnSwitch.setText("Switch");
        btnSwitch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSwitchActionPerformed(evt);
            }
        });

        btnSwitchCancel.setText("Cancel");
        btnSwitchCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSwitchCancelActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(btnSwitch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 165, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(btnSwitchCancel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
            .add(panelSwitch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(panelSwitch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnSwitch)
                    .add(btnSwitchCancel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(new java.awt.Component[] {btnSwitch, btnSwitchCancel}, org.jdesktop.layout.GroupLayout.VERTICAL);

        tabAction.addTab("Switch", jPanel3);

        jScrollPane1.setViewportView(listUsers);

        lblPlayer0.setText("Player 1");

        lblPlayer1.setText("Player 2");

        lblClock0.setText("20:00:00");

        lblClock1.setText("20:00:00");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(29, 29, 29)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(tabAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 340, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(scrollChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                    .add(txtChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblPlayer0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                            .add(lblPlayer1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblClock1)
                            .add(lblClock0))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {lblPlayer0, lblPlayer1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {lblClock0, lblClock1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblClock0)
                            .add(lblPlayer0))
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblClock1)
                            .add(lblPlayer1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(scrollChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtChat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 230, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(tabAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 216, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtChatFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtChatFocusGained

}//GEN-LAST:event_txtChatFocusGained

    private void txtChatFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtChatFocusLost

    }//GEN-LAST:event_txtChatFocusLost

    private void txtChatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtChatKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            m_chat.addMessage("Ben", txtChat.getText());
            txtChat.setText("");
        }
    }//GEN-LAST:event_txtChatKeyReleased

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        int result = JOptionPane.showConfirmDialog(this, "Leaving will cause you " +
                "to forfeit this battle. Are you sure you want to leave?",
                "Leaving Battle", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            dispose();
        }
    }//GEN-LAST:event_formWindowClosing

    private void btnMoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveActionPerformed
        int selected = -1;
        for (int i = 0; i < m_moveButtons.length; i++) {
            if (m_moveButtons[i].isSelected()) {
                selected = i;
                break;
            }
        }
        sendMove(selected);
    }//GEN-LAST:event_btnMoveActionPerformed

    private void btnSwitchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSwitchActionPerformed
        int selected = -1;
        for (int i = 0; i < m_switches.length; i++) {
            if (m_switches[i].isSelected()) {
                selected = i;
                break;
            }
        }
        if (selected == -1) return;
        sendSwitch(selected);
    }//GEN-LAST:event_btnSwitchActionPerformed

    private void btnMoveCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveCancelActionPerformed
        btnMove.setEnabled(true);
        btnMoveCancel.setEnabled(false);
    }//GEN-LAST:event_btnMoveCancelActionPerformed

    private void btnSwitchCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSwitchCancelActionPerformed
        btnSwitch.setEnabled(true);
        btnSwitchCancel.setEnabled(false);
    }//GEN-LAST:event_btnSwitchCancelActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    //javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    
                }
                BattleWindow battle = new BattleWindow(0, 1, new String[] {"bearzly", "Catherine"},
                        new String[][] {
                            {"Tackle", "Bite", "Yawn", "Mega Punch"},
                            {"Tackle", "Bite", "Yawn", "Mega Punch"},
                            {"Tackle", "Bite", "Yawn", "Mega Punch"},
                            {"Tackle", "Bite", "Yawn", "Mega Punch"},
                            {"Tackle", "Bite", "Yawn", "Mega Punch"},
                            {"Tackle", "Bite", "Yawn", "Mega Punch"}
                }, new String[] {"Bulbasaur", "Squirtle", "Ivysaur", "Chansey", "Pikachu", "Totodile"});
                battle.setPokemon(new VisualPokemon[] {new VisualPokemon("Bulbasaur", 1, false)},
                        new VisualPokemon[] {new VisualPokemon("Groudon", 0, true)});
                battle.setForced(true);
                battle.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMove;
    private javax.swing.JButton btnMoveCancel;
    private javax.swing.JButton btnSwitch;
    private javax.swing.JButton btnSwitchCancel;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblClock0;
    private javax.swing.JLabel lblClock1;
    private javax.swing.JLabel lblPlayer0;
    private javax.swing.JLabel lblPlayer1;
    private javax.swing.JList listUsers;
    private javax.swing.JPanel panelMoves;
    private javax.swing.JPanel panelSwitch;
    private javax.swing.JScrollPane scrollChat;
    private javax.swing.JTabbedPane tabAction;
    private javax.swing.JTextField txtChat;
    // End of variables declaration//GEN-END:variables

}
