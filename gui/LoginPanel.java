package gui;

import java.awt.*;
import javax.swing.*;
import models.User;
import utils.SupportUtils;

public class LoginPanel extends JPanelWithBackground {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private StyledButton loginButton;
    private StyledButton backButton;
    private MainFrame mainFrame;
    private AuthPanel authPanel;

    public LoginPanel(MainFrame mainFrame, AuthPanel authPanel) {
        this.mainFrame = mainFrame;
        this.authPanel = authPanel;
        setLayout(new BorderLayout());

        // 🔹 Tartalom panel létrehozása
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 🔹 Szövegmezők
        usernameField = new JTextField(15);
        usernameField.setPreferredSize(new Dimension(250, 40));
        usernameField.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        usernameField.setBackground(new Color(255, 255, 255, 200));

        passwordField = new JPasswordField(15);
        passwordField.setPreferredSize(new Dimension(250, 40));
        passwordField.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        passwordField.setBackground(new Color(255, 255, 255, 200));

        // 🔹 Gombok létrehozása
        loginButton = new StyledButton("Login");
        backButton = new StyledButton("Back");

        // 🔹 Gombok eseménykezelői
        loginButton.addActionListener(e -> login(usernameField.getText(), new String(passwordField.getPassword())));
        backButton.addActionListener(e -> mainFrame.showPanel("AuthPanel"));

        // 🔹 Szövegek és mezők elhelyezése
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        usernameLabel.setOpaque(true);
        usernameLabel.setBackground(new Color(255, 255, 255, 50));
        usernameLabel.setPreferredSize(new Dimension(150, 30));
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        contentPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        passwordLabel.setOpaque(true);
        passwordLabel.setBackground(new Color(255, 255, 255, 50));
        passwordLabel.setPreferredSize(new Dimension(150, 30));
        passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        contentPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        contentPanel.add(loginButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        contentPanel.add(backButton, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // 🔹 Support gomb panel (jobb alsó sarokban)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        StyledButton supportButton = new StyledButton("Support");
        supportButton.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        supportButton.setPreferredSize(new Dimension(120, 40));
        supportButton.addActionListener(e -> SupportUtils.openPatreonLink());

        bottomPanel.add(supportButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void login(String username, String password) {
        try {
            User user = authPanel.authenticateUser(username, password);
            if (user != null) {
                CustomDialog.showInfo("Login Successful!");
                mainFrame.setUser(user);
                if (user.getHiveId() == null) {
                    mainFrame.showPanel("HiveHandlerPanel");
                } else if (user.isLeader()) {
                    mainFrame.showPanel("LeaderMenuPanel");
                } else {
                    mainFrame.showPanel("MemberMenuPanel");
                }
            } else {
                CustomDialog.showError("Invalid username or password.");
            }
        } catch (Exception e) {
            CustomDialog.showError("Error during login: " + e.getMessage());
        }
    }
}
