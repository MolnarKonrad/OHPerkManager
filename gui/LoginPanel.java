package gui;

import java.awt.*;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import models.User;
import utils.SupportUtils;

public class LoginPanel extends JPanelWithBackground {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private StyledButtonCyan loginButton;
    private StyledButtonCyan backButton;
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
        usernameField.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameField.setBackground(new Color(255, 255, 255, 200));

        passwordField = new JPasswordField(15);
        passwordField.setPreferredSize(new Dimension(250, 40));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 18));
        passwordField.setBackground(new Color(255, 255, 255, 200));

        // 🔹 Gombok létrehozása
        loginButton = new StyledButtonCyan("Login");
        backButton = new StyledButtonCyan("Back");

        // 🔹 Gombok eseménykezelői
        loginButton.addActionListener(e -> login(usernameField.getText(), new String(passwordField.getPassword())));
        backButton.addActionListener(e -> mainFrame.showPanel("AuthPanel"));

        // 🔹 Szövegek és mezők elhelyezése
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 18));
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
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 18));
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

        // User Guide gomb (bal oldalon)
        StyledButtonCyan userGuideButton = new StyledButtonCyan("User Guide");
        userGuideButton.setFont(new Font("Arial", Font.BOLD, 17));
        userGuideButton.setPreferredSize(new Dimension(150, 50));
        userGuideButton.addActionListener(e -> SupportUtils.openUserGuideLink());        

        // Support gomb (jobb oldalon)
        StyledButtonCyan supportButton = new StyledButtonCyan("Support");
        supportButton.setFont(new Font("Arial", Font.BOLD, 17));
        supportButton.setPreferredSize(new Dimension(150, 50));
        supportButton.addActionListener(e -> SupportUtils.openPatreonLink());

        bottomPanel.add(userGuideButton, BorderLayout.WEST);
        bottomPanel.add(supportButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void login(String username, String password) {
        try {
            User user = authPanel.authenticateUser(username, password);
            if (user != null) {
                mainFrame.setUser(user); // This will now handle the panel switching
            } else {
                CustomDialog.showError("Invalid username or password.");
            }
        } catch (ExecutionException | InterruptedException e) {
            CustomDialog.showError("Error during login: " + e.getMessage());
        } catch (Exception e) {
            CustomDialog.showError("Error during login: " + e.getMessage());
        }
    }
}
