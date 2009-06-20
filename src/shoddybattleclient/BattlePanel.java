/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BattlePanel.java
 *
 * Created on Jun 20, 2009, 2:48:59 PM
 */

package shoddybattleclient;

import shoddybattleclient.utils.*;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author ben
 */
public class BattlePanel extends javax.swing.JPanel {

    private BattleTableModel m_model = new BattleTableModel();

    /** Creates new form BattlePanel */
    public BattlePanel() {
        initComponents();

        m_model.addBattle(153234, 0, "bearzly", "Catherine", 2, 8);
        m_model.addBattle(23523, 1, "DougJustDoug", "chaos", 1, 6);
        tblBattles.setModel(m_model);
        TableColumnModel cols = tblBattles.getColumnModel();
        cols.getColumn(1).setMaxWidth(120);
        cols.getColumn(2).setMaxWidth(30);
        cols.getColumn(3).setMaxWidth(50);
        System.out.println(tblBattles.getClass().toString());
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
        tblBattles = new SortableJTable();
        btnRefresh = new javax.swing.JButton();
        btnJoin = new javax.swing.JButton();

        tblBattles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tblBattles);

        btnRefresh.setText("Refresh");

        btnJoin.setText("Join");
        btnJoin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnJoinActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(btnJoin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnRefresh)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {btnJoin, btnRefresh}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnRefresh)
                    .add(btnJoin))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnJoinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnJoinActionPerformed
        int idx = tblBattles.getSelectedRow();
        System.out.println(m_model.getId(idx));
    }//GEN-LAST:event_btnJoinActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnJoin;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblBattles;
    // End of variables declaration//GEN-END:variables

    public static void main(String[] args) {
        JFrame frame = new JFrame("Battle Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.add(new BattlePanel());
        frame.setVisible(true);
    }
}
