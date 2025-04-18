package gui;

import javax.swing.*;
import java.awt.*;

public class CustomConfirmationPopup {

    private JDialog dialog;
    private boolean confirmed; // This will store the user's choice  

    public CustomConfirmationPopup(String title, String message) {
        // Create a new JDialog  
        dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(500, 200);
        dialog.setLocationRelativeTo(null); // Center the dialog  

        // Create the main panel with the specified background color  
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(255, 3, 3)); // Main background color  
        mainPanel.setLayout(new BorderLayout()); // Use BorderLayout  

        // Create a JLabel for the message  
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER); // Center-align the text  
        messageLabel.setForeground(Color.BLACK); // Text color  
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Font style and size  

        // Add the message label to the main panel  
        mainPanel.add(messageLabel, BorderLayout.CENTER); // Add message label to the center of the panel  

        // Create and add the buttons  
        JPanel buttonPanel = new JPanel(); // Panel for buttons  
        buttonPanel.setBackground(new Color(255, 3, 3)); // Same background color  

        JButton yesButton = new JButton("Yes");
        yesButton.setBackground(Color.CYAN);
        yesButton.setForeground(Color.BLACK);
        yesButton.setFont(new Font("Arial", Font.BOLD, 20));
        yesButton.setPreferredSize(new Dimension(150, 50)); // Set button size  

        JButton noButton = new JButton("No");
        noButton.setBackground(Color.CYAN);
        noButton.setForeground(Color.BLACK);
        noButton.setFont(new Font("Arial", Font.BOLD, 20));
        noButton.setPreferredSize(new Dimension(150, 50)); // Set button size  

        // Add button listeners  
        yesButton.addActionListener(e -> {
            confirmed = true; // Set confirmed to true if yes button is pressed  
            dialog.dispose(); // Close dialog on Yes click  
        });

        noButton.addActionListener(e -> {
            confirmed = false; // Set confirmed to false if no button is pressed  
            dialog.dispose(); // Close dialog on No click  
        });

        // Add buttons to the button panel  
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        // Add main panel and button panel to the dialog  
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
    }

    // Method to display the dialog and get user's response  
    public boolean show() {
        dialog.setVisible(true); // Show the dialog  
        return confirmed; // Return user's choice  
    }
}
