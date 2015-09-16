package squidpony.examples;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.BorderLayout;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import squidpony.WeightedLetterNamegen;

/**
 *
 * @author @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class NamegenPanel extends javax.swing.JPanel {

    private final HashMap<String, String[]> starters = new HashMap<>();

    /**
     * Creates new form NamegenPanel
     */
    public NamegenPanel() {
        initComponents();

        starters.put("Star Wars Style", WeightedLetterNamegen.STAR_WARS_STYLE_NAMES);
        starters.put("Viking Style", WeightedLetterNamegen.VIKING_STYLE_NAMES);
        starters.put("USA Male First Name", WeightedLetterNamegen.COMMON_USA_MALE_NAMES);
        starters.put("USA Last Name", WeightedLetterNamegen.COMMON_USA_LAST_NAMES);
        starters.put("Lovecraft Mythos Style", WeightedLetterNamegen.LOVECRAFT_MYTHOS_NAMES);

        inputComboBox.setModel(new DefaultComboBoxModel(starters.keySet().toArray()));
        inputComboBox.setSelectedIndex(0);
        inputComboBoxItemStateChanged(null);
        generateButtonActionPerformed(null);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        outputList = new javax.swing.JList();
        generateButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        inputList = new javax.swing.JList();
        inputComboBox = new javax.swing.JComboBox();

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Output", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        outputList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(outputList);

        generateButton.setText("Generate");
        generateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateButtonActionPerformed(evt);
            }
        });

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Input", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        inputList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(inputList);

        inputComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                inputComboBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                    .addComponent(inputComboBox, 0, 169, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(generateButton, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(inputComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(generateButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateButtonActionPerformed
        String[] original = starters.get((String) inputComboBox.getSelectedItem());
        WeightedLetterNamegen namegen = new WeightedLetterNamegen(original, 2);
        String[] names = namegen.generate(20);
        outputList.setListData(names);
    }//GEN-LAST:event_generateButtonActionPerformed

    private void inputComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_inputComboBoxItemStateChanged
        String[] original = starters.get((String) inputComboBox.getSelectedItem());
        inputList.setListData(original);
    }//GEN-LAST:event_inputComboBoxItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton generateButton;
    private javax.swing.JComboBox inputComboBox;
    private javax.swing.JList inputList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList outputList;
    // End of variables declaration//GEN-END:variables
}
