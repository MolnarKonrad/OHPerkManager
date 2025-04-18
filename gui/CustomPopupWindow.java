package gui;

import javax.swing.*;
import java.awt.*;

public class CustomPopupWindow {

    private JDialog dialog;

    public CustomPopupWindow(String title, String message) {        
        dialog = new JDialog();
        dialog.setTitle(title);
        ImageIcon icon = new ImageIcon(getClass().getResource("/PM_icon.png"));       
        dialog.setIconImage(icon.getImage());
        dialog.setModal(true);
        dialog.setSize(500, 200);
        dialog.setLocationRelativeTo(null);
       
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(81, 203, 203));
        mainPanel.setLayout(new BorderLayout());
       
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(Color.BLACK);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
       
        mainPanel.add(messageLabel, BorderLayout.CENTER);
       
        dialog.add(mainPanel);
       
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        mainPanel.add(okButton, BorderLayout.SOUTH);
        
        okButton.setBackground(Color.CYAN);
        okButton.setForeground(Color.BLACK);
        okButton.setFont(new Font("Arial", Font.BOLD, 20));
        okButton.setPreferredSize(new Dimension(150, 50));
    }
   
    public void show() {
        dialog.setVisible(true);
    }
}
