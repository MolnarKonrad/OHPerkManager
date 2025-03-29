package gui;

import javax.swing.*;
import java.awt.*;

public class CustomPopupWindow {

    private JDialog dialog;

    public CustomPopupWindow(String title, String message) {
        // Create a new JDialog  
        dialog = new JDialog();
        dialog.setTitle(title);
        ImageIcon icon = new ImageIcon(getClass().getResource("/PM_icon.png"));       
        dialog.setIconImage(icon.getImage());
        dialog.setModal(true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(null); // Center the dialog  

        // Create the main panel with the specified background color  
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(81, 203, 203)); // Main background color  
        mainPanel.setLayout(new BorderLayout()); // Use BorderLayout  

        // Create a JLabel for the message  
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER); // Center-align the text  
        messageLabel.setForeground(Color.BLACK); // Text color  
        messageLabel.setFont(new Font("Times New Roman", Font.BOLD, 20)); // Font style and size  

        // Add the message label to the main panel  
        mainPanel.add(messageLabel, BorderLayout.CENTER); // Add message label to the center of the panel  

        // Add main panel to the dialog  
        dialog.add(mainPanel);

        // Create and add the OK button  
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose()); // Close dialog on button click  
        mainPanel.add(okButton, BorderLayout.SOUTH); // Add button at the bottom  

        // Optional: Set button properties  
        okButton.setBackground(Color.CYAN); // Button background color  
        okButton.setForeground(Color.BLACK); // Button text color  
        okButton.setFont(new Font("Times New Roman", Font.BOLD, 20)); // Button font
        okButton.setPreferredSize(new Dimension(150, 50)); // Set the button size (width, height) 
    }

    // Method to display the dialog  
    public void show() {
        dialog.setVisible(true);
    }
}