/*
 * LobbyWindow.java
 *
 * Created on Apr 5, 2009, 12:47:25 PM
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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.*;
import java.util.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import shoddybattleclient.network.ServerLink;
import shoddybattleclient.network.ServerLink.ChallengeMediator;
import shoddybattleclient.utils.UserListModel;

/**
 *
 * @author ben
 */
public class LobbyWindow extends javax.swing.JFrame {

    public static class Channel {
        public static final int OWNER = 1;      // +q
        public static final int PROTECTED = 2;  // +a
        public static final int OP = 4;         // +o
        public static final int HALF_OP = 8;    // +h
        public static final int VOICE = 16;     // +v
        public static final int IDLE = 32;      // inactive
        public static final int BUSY = 64;      // ("ignoring challenges")

        private int m_id;
        private String m_name;
        private String m_topic;
        private int m_flags;
        private ChatPane m_chat;
        private UserListModel m_users =
                new UserListModel(new ArrayList<User>());

        public void setChatPane(ChatPane c) {
            m_chat = c;
        }
        
        public ChatPane getChatPane() {
            return m_chat;
        }

        public UserListModel getModel() {
            return m_users;
        }

        public static int getLevel(int flags) {
            if ((flags & OWNER) != 0)
                return 5;
            if ((flags & PROTECTED) != 0)
                return 4;
            if ((flags & OP) != 0)
                return 3;
            if ((flags & HALF_OP) != 0)
                return 2;
            if ((flags & VOICE) != 0)
                return 1;
            return 0;
        }

        public Channel(int id, String name, String topic, int flags) {
            m_id = id;
            m_name = name;
            m_topic = topic;
            m_flags = flags;
        }
        public void addUser(String name, int flags) {
            m_users.add(new User(name, flags));
        }
        public void removeUser(String name) {
            m_users.remove(name);
        }
        public void updateUser(String name, int flags) {
            m_users.setLevel(name, flags);
        }
        public void sort() {
            m_users.sort();
        }
        public String getTopic() {
            return m_topic;
        }
        public void setTopic(String topic) {
            m_topic = topic;
        }
        public String getName() {
            return m_name;
        }
        int getId() {
            return m_id;
        }
        public User getUser(String name) {
            return m_users.getUser(name);
        }
    }

    public static class User implements Comparable {
        public final static int STATUS_PRESENT = 0;
        public final static int STATUS_AWAY = 1;
        private static final String m_colours[] = {
            "black",        // Regular user
            "#0000aa",      // Mod
            "rgb(200,0,0)"  // Admin
        };
        private String m_name;
        private int m_status = 0;
        private int m_flags, m_level;
        private List<Integer> m_battles = new ArrayList<Integer>();

        public User(String name, int flags) {
            m_name = name;
            m_flags = flags;
            m_level = Channel.getLevel(flags);
        }

        public void setLevel(int level) {
            m_level = Channel.getLevel(level);
        }
        public void setStatus(int status) {
            m_status = status;
        }
        public String getName() {
            return m_name;
        }
        public int compareTo(Object o2) {
            User u2 = ((User)o2);
            if (m_level > u2.m_level)
                return -1;
            if (m_level < u2.m_level)
                return 1;
            if (m_status < u2.m_status)
                return -1;
            if (m_status > u2.m_status)
                return 1;
            String s2 = u2.m_name;
            return m_name.compareToIgnoreCase(s2);
        }
        public String getPrefix() {
            String[] prefixes = { "", "+", "%", "@", "@", "~" };
            return prefixes[m_level];
        }
        public void addBattle(int id) {
            m_battles.add(id);
        }
        public void removeBattle(int id) {
            m_battles.remove(id);
        }
        @Override
        public boolean equals(Object o2) {
            return ((User)o2).m_name.equals(m_name);
        }
        @Override
        public String toString() {
            String colour = "rgb(0, 0, 0)"; // black for now
            String style = (m_battles.size() > 0) ? "font-style: italic;" : "";
            return "<html><font style='color: "
                    + colour + style + "'>" + getPrefix()
                    + m_name + "</font></html>";
        }
    }

    //private ChatPane m_chat;
    private ChallengeNotifier m_notifier;
    private String m_name;
    private ServerLink m_link;
    private Map<Integer, Channel> m_channels = new HashMap<Integer, Channel>();

    public Channel getChannel(String name) {
        for (Channel i : m_channels.values()) {
            if (name.equals(i.getName())) {
                return i;
            }
        }
        return null;
    }

    public void addChannel(Channel channel) {
        m_channels.put(channel.m_id, channel);
        ChatPane c = new ChatPane(channel, this, m_name);
        c.getChat().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                m_notifier.processClick(e);
            }
        });
        channel.setChatPane(c);
        String name = channel.getName();
        c.addMessage(null, "<b>The topic for #"
                + name + " is: "
                + channel.getTopic()
                + "</b>", false);
        tabChats.add("#" + name, c);
    }

    public void handleJoinPart(int id, String user, boolean join) {
        Channel channel = m_channels.get(id);
        if (channel != null) {
            if (join) {
                channel.addUser(user, 0);
            } else {
                channel.removeUser(user);
            }
        }
        listUsers.setModel(new UserListModel(channel.getModel().getList()));
    }

    /** Creates new form LobbyWindow */
    public LobbyWindow(ServerLink link, String userName) {
        initComponents();
        m_link = link;
        m_link.setLobbyWindow(this);
        m_name = userName;
        m_notifier = new ChallengeNotifier(this);
        setGlassPane(m_notifier);

        tabChats.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                ChatPane c = (ChatPane)tabChats.getSelectedComponent();
                listUsers.setModel(c.getChannel().getModel());
            }
        });

        setTitle("Shoddy Battle - " + userName);
    }

    public ServerLink getLink() {
        return m_link;
    }

    /**
     * Updates a user's status in the user list
     * @param name The user's name
     * @param status A constant representing status changes
     * @param value An extra parameter
     */
    /**public void updateUserStatus(String name, Status status, Object value) {
        switch (status) {
            case ONLINE:
                int level = (Integer)value;
                m_userList.add(new User(name, level));
                m_userList.sort();
                break;
            case OFFLINE:
                m_userList.remove(name);
                break;
            case AWAY:
                m_userList.setStatus(name, User.STATUS_AWAY);
                break;
            case RETURN:
                m_userList.setStatus(name, User.STATUS_PRESENT);
                break;
            case BATTLE_START:
                String opp = (String)value;
                m_chat.addMessage("Battle starting", name + " vs. " + opp);
                break;
            case BATTLE_END:
                break;
        }
        m_userList = new UserListModel(m_userList.getList());
        listUsers.setModel(m_userList);
    }**/

    /**
     * Returns the bounds of the main chat pane
     */
    public Rectangle getChatBounds() {
        ChatPane chat = (ChatPane)tabChats.getSelectedComponent();
        Point p = chat.getPane().getLocation();
        Point p2 = chat.getLocation();
        Point p3 = tabChats.getLocation();
        JScrollPane pane = chat.getPane();
        int w = pane.getWidth() - pane.getVerticalScrollBar().getWidth();
        int h = pane.getHeight();
        int x = p.x + p2.x + p3.x;
        int y = p.y + p2.y + p3.y;
        return new Rectangle(x, y, w, h);
    }

    public static void viewWebPage(URL page) {
        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.println(page);
        }
    }

    public void addChallenge(String name, boolean incoming, int gen, int n) {
        m_notifier.addChallenge(name, incoming, gen, n);
    }

    public ChallengeMediator getChallengeMediator(String name) {
        return m_notifier.getMediator(name);
    }

    public void cancelChallenge(String name) {
        m_notifier.removeChallenge(name);
    }

    public void cancelChallenge(int id) {
        m_notifier.removeChallenge(id);
    }

    public ChatPane getChat() {
        return (ChatPane)tabChats.getSelectedComponent();
    }

    public String getUserName() {
        return m_name;
    }

    public void handleUpdateStatus(int id, String user, int flags) {
        Channel channel = m_channels.get(id);
        if (channel != null) {
            channel.updateUser(user, flags);
            updateUsers(channel);
        }
    }

    private void updateUsers(Channel channel) {
        UserListModel model = channel.getModel();
        model.sort();
        listUsers.setModel(new UserListModel(model.getList()));
    }

    public void handleChannelMessage(int id, String user, String message) {
        Channel channel = m_channels.get(id);
        if (channel != null) {
            String prefix = channel.getUser(user).getPrefix();
            channel.getChatPane().addMessage(prefix + user, message, true);
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listUsers = new javax.swing.JList();
        btnChallenge = new javax.swing.JButton();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jComboBox2 = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jComboBox1 = new javax.swing.JComboBox();
        tabChats = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocationByPlatform(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel3.setBackground(new java.awt.Color(244, 242, 242));
        jPanel3.setOpaque(false);

        jScrollPane1.setViewportView(listUsers);

        btnChallenge.setText("Challenge");
        btnChallenge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChallengeActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, btnChallenge, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnChallenge)
                .add(8, 8, 8))
        );

        jTabbedPane1.addTab("Users", jPanel3);

        jPanel2.setBackground(new java.awt.Color(244, 242, 242));
        jPanel2.setOpaque(false);

        jButton3.setText("Load Team");

        jButton5.setText("View Rules");

        jButton7.setText("Find Match");

        jList2.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "1", "2", "3", "4", "5", "6" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(jList2);

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jButton3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .add(jButton5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .add(jButton7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                    .add(jComboBox2, 0, 107, Short.MAX_VALUE))
                .add(20, 20, 20))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(16, 16, 16)
                .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton1)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Ladder", jPanel2);

        jPanel1.setBackground(new java.awt.Color(244, 242, 242));
        jPanel1.setOpaque(false);

        jButton6.setText("Challenge");

        jScrollPane2.setViewportView(jList1);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBox1, 0, 106, Short.MAX_VALUE)
                    .add(jButton6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton6)
                .add(6, 6, 6))
        );

        jTabbedPane2.addTab("Unrated", jPanel1);

        jTabbedPane1.addTab("Find", jTabbedPane2);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(tabChats, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 177, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, tabChats, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you " +
                "want to leave this server?", "Disconnecting...", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            this.dispose();
            m_link.close();
            new WelcomeWindow().setVisible(true);
        }
    }//GEN-LAST:event_formWindowClosing

    private void btnChallengeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChallengeActionPerformed
        User user = (User)listUsers.getSelectedValue();
        if (user == null) return;
        String opponent = user.getName();
        if (opponent.equals(m_name)) {
            // todo: internationalisation
            JOptionPane.showMessageDialog(this, "You cannot challenge yourself.");
        } else {
            new ChallengeWindow(m_link, opponent).setVisible(true);
        }
}//GEN-LAST:event_btnChallengeActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                LobbyWindow lw = new LobbyWindow(null, "Ben");
                //lw.updateUserStatus("bearzly", Status.ONLINE, 2);
                lw.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChallenge;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JList listUsers;
    private javax.swing.JTabbedPane tabChats;
    // End of variables declaration//GEN-END:variables

}
