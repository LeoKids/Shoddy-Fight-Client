/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChallengeWindow.java
 *
 * Created on Apr 23, 2009, 10:33:15 PM
 */

package shoddybattleclient;

import java.awt.FileDialog;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import shoddybattleclient.network.ServerLink;
import shoddybattleclient.network.ServerLink.ChallengeMediator;
import shoddybattleclient.shoddybattle.*;
import shoddybattleclient.utils.TeamFileParser;

/**
 *
 * @author ben
 */
public class ChallengeWindow extends javax.swing.JFrame {

    private Pokemon[] m_team = null;
    private ServerLink m_link;
    private String m_opponent;

    /** Creates new form ChallengeWindow */
    public ChallengeWindow(ServerLink link, String opponent) {
        initComponents();
        m_link = link;
        m_opponent = opponent;
        btnChallenge.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        lstTeam = new javax.swing.JList();
        btnLoad = new javax.swing.JButton();
        btnChallenge = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setViewportView(lstTeam);

        btnLoad.setText("Load Team");
        btnLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoadActionPerformed(evt);
            }
        });

        btnChallenge.setText("Challenge");
        btnChallenge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChallengeActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, btnChallenge, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, btnLoad, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(btnLoad)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnChallenge)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoadActionPerformed
        /**FileDialog fd = new FileDialog(this, "Choose a team to load", FileDialog.LOAD);
        fd.setVisible(true);
        if (fd.getFile() == null) return;
        String file = fd.getDirectory() + fd.getFile();**/
        String file = "/home/Catherine/team1.sbt";
        TeamFileParser tfp = new TeamFileParser();
        m_team = tfp.parseTeam(file);
        if (m_team != null) {
            lstTeam.setModel(new DefaultComboBoxModel(m_team));
            btnChallenge.setEnabled(true);
        }
    }//GEN-LAST:event_btnLoadActionPerformed

    private void btnChallengeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChallengeActionPerformed
        m_link.postChallenge(new ChallengeMediator() {
            public Pokemon[] getTeam() {
                return m_team;
            }
            public void informResolved(boolean accepted) {
                if (accepted) {
                    m_link.postChallengeTeam(m_opponent, m_team);
                } else {
                    // todo: internationalisation
                    JOptionPane.showMessageDialog(ChallengeWindow.this,
                            m_opponent + " rejected the challenge.");
                }
                dispose();
            }
            public String getOpponent() {
                return m_opponent;
            }
            public int getGeneration() {
                return 1; // platinum
            }
            public int getActivePartySize() {
                return 2; // doubles
            }
        });
        btnChallenge.setEnabled(false);
    }//GEN-LAST:event_btnChallengeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChallenge;
    private javax.swing.JButton btnLoad;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList lstTeam;
    // End of variables declaration//GEN-END:variables

}
