package com.nexes.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;


public class TestPanel2 extends JPanel {
 
    private static final long serialVersionUID = 8513008374161091875L;
	private javax.swing.JLabel anotherBlankSpace;
    private javax.swing.JLabel blankSpace;
    private javax.swing.ButtonGroup connectorGroup;
    private javax.swing.JRadioButton ethernetRJRadioButton;
    private javax.swing.JRadioButton ethernetTenRadioButton;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton notInventedYetRadioButton;
    private javax.swing.JRadioButton serialParallelRadioButton;
    private javax.swing.JLabel welcomeTitle;
    private javax.swing.JRadioButton wirelessRadioButton;
    private javax.swing.JLabel yetAnotherBlankSpace1;
    
    private JPanel contentPanel;
    private JLabel iconLabel;
    private JSeparator separator;
    private JLabel textLabel;
    private JPanel titlePanel;
        
    public TestPanel2() {
     
        super();
                
        contentPanel = getContentPanel();
        contentPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

        ImageIcon icon = getImageIcon();
        
        titlePanel = new javax.swing.JPanel();
        textLabel = new javax.swing.JLabel();
        iconLabel = new javax.swing.JLabel();
        separator = new javax.swing.JSeparator();

        setLayout(new java.awt.BorderLayout());


        titlePanel.setLayout(new java.awt.BorderLayout());
        titlePanel.setBackground(Color.gray);
        
        textLabel.setBackground(Color.gray);
        textLabel.setFont(new Font("MS Sans Serif", Font.BOLD, 14));
        textLabel.setText("Favorite Connector Type");
        textLabel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        textLabel.setOpaque(true);

        iconLabel.setBackground(Color.gray);
        if (icon != null)
            iconLabel.setIcon(icon);
        
        titlePanel.add(textLabel, BorderLayout.CENTER);
        titlePanel.add(iconLabel, BorderLayout.EAST);
        titlePanel.add(separator, BorderLayout.SOUTH);
        
        add(titlePanel, BorderLayout.NORTH);
        JPanel secondaryPanel = new JPanel();
        secondaryPanel.add(contentPanel, BorderLayout.NORTH);
        add(secondaryPanel, BorderLayout.WEST);

    }  
    
    public void addCheckBoxActionListener(ActionListener l) {
        jCheckBox1.addActionListener(l);
    }
    
    public boolean isCheckBoxSelected() {
        return jCheckBox1.isSelected();
    }
    
    public String getRadioButtonSelected() {
        return connectorGroup.getSelection().getActionCommand();
    }
    
    private JPanel getContentPanel() {     
        
        JPanel contentPanel1 = new JPanel();
        
        connectorGroup = new javax.swing.ButtonGroup();
        welcomeTitle = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        blankSpace = new javax.swing.JLabel();
        wirelessRadioButton = new javax.swing.JRadioButton();
        ethernetRJRadioButton = new javax.swing.JRadioButton();
        ethernetTenRadioButton = new javax.swing.JRadioButton();
        serialParallelRadioButton = new javax.swing.JRadioButton();
        notInventedYetRadioButton = new javax.swing.JRadioButton();
        
        wirelessRadioButton.setActionCommand("Wireless Radio");
        ethernetRJRadioButton.setActionCommand("Ethernet RJ-45");
        ethernetTenRadioButton.setActionCommand("Ethernet 10 base T");
        serialParallelRadioButton.setActionCommand("Serial/Parallel");
        notInventedYetRadioButton.setActionCommand("Not Yet Invented");
        
        anotherBlankSpace = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        yetAnotherBlankSpace1 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        ethernetRJRadioButton.setSelected(true);
        
        contentPanel1.setLayout(new java.awt.BorderLayout());

        welcomeTitle.setText("Please enter your favorite type of data connector:");
        contentPanel1.add(welcomeTitle, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.GridLayout(0, 1));

        jPanel1.add(blankSpace);

        wirelessRadioButton.setText("802.11 b/g");
        connectorGroup.add(wirelessRadioButton);
        jPanel1.add(wirelessRadioButton);

        ethernetRJRadioButton.setText("Ethernet RJ-45");
        connectorGroup.add(ethernetRJRadioButton);
        jPanel1.add(ethernetRJRadioButton);

        ethernetTenRadioButton.setText("Ethernet 10 base T");
        connectorGroup.add(ethernetTenRadioButton);
        jPanel1.add(ethernetTenRadioButton);

        serialParallelRadioButton.setText("Serial/Parallel");
        connectorGroup.add(serialParallelRadioButton);
        jPanel1.add(serialParallelRadioButton);

        notInventedYetRadioButton.setText("Something Not Yet Invented But You're Sure You'll Want It");
        connectorGroup.add(notInventedYetRadioButton);
        jPanel1.add(notInventedYetRadioButton);

        jPanel1.add(anotherBlankSpace);

        jCheckBox1.setText("I agree to laugh at people who chose options other than mine");
        jPanel1.add(jCheckBox1);

        jPanel1.add(yetAnotherBlankSpace1);

        contentPanel1.add(jPanel1, java.awt.BorderLayout.CENTER);

        jLabel1.setText("Note that the 'Next' button is disabled until you check the box above.");
        contentPanel1.add(jLabel1, java.awt.BorderLayout.SOUTH);
        
        return contentPanel1;
    }
    
    private ImageIcon getImageIcon() {
        
        //  Icon to be placed in the upper right corner.
        
        return null;
    }
    
    

}
