package gui;

import javax.swing.*;
import java.awt.*;

public class CustomConfirmationPopup {

    private JDialog dialog;
    private boolean confirmed;

    public CustomConfirmationPopup(String title, String message) {      
        dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(500, 200);
        dialog.setLocationRelativeTo(null);
      
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(255, 3, 3));
        mainPanel.setLayout(new BorderLayout());
       
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(Color.BLACK);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
       
        mainPanel.add(messageLabel, BorderLayout.CENTER);
       
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 3, 3));

        JButton yesButton = new JButton("Yes");
        yesButton.setBackground(Color.CYAN);
        yesButton.setForeground(Color.BLACK);
        yesButton.setFont(new Font("Arial", Font.BOLD, 20));
        yesButton.setPreferredSize(new Dimension(150, 50));

        JButton noButton = new JButton("No");
        noButton.setBackground(Color.CYAN);
        noButton.setForeground(Color.BLACK);
        noButton.setFont(new Font("Arial", Font.BOLD, 20));
        noButton.setPreferredSize(new Dimension(150, 50));
       
        yesButton.addActionListener(e -> {
            confirmed = true;
            dialog.dispose(); 
        });

        noButton.addActionListener(e -> {
            confirmed = false;
            dialog.dispose();
        });
       
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
      
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
    }
    
    public boolean show() {
        dialog.setVisible(true);
        return confirmed;
    }
}
