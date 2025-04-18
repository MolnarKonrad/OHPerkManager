package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import models.User;
import utils.SupabaseClient;
import utils.SupportUtils;

public class RegistrationPanel extends JPanelWithBackground {

    private JTextField usernameField;
    private JTextField confirmUsernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private StyledButtonCyan registerButton;
    private StyledButtonCyan backButton;
    private MainFrame mainFrame;
    private AuthPanel authPanel;
    private final SupabaseClient supabase;

    public RegistrationPanel(SupabaseClient supabase, MainFrame mainFrame, AuthPanel authPanel) {
        this.supabase = supabase;
        this.mainFrame = mainFrame;
        this.authPanel = authPanel;
        setLayout(new BorderLayout());

        // 🔹 Tartalom panel létrehozása
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField(15);
        usernameField.setPreferredSize(new Dimension(250, 40));
        usernameField.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameField.setBackground(new Color(255, 255, 255, 200));

        confirmUsernameField = new JTextField(15);
        confirmUsernameField.setPreferredSize(new Dimension(250, 40));
        confirmUsernameField.setFont(new Font("Arial", Font.PLAIN, 18));
        confirmUsernameField.setBackground(new Color(255, 255, 255, 200));

        passwordField = new JPasswordField(15);
        passwordField.setPreferredSize(new Dimension(250, 40));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 18));
        passwordField.setBackground(new Color(255, 255, 255, 200));

        confirmPasswordField = new JPasswordField(15);
        confirmPasswordField.setPreferredSize(new Dimension(250, 40));
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 18));
        confirmPasswordField.setBackground(new Color(255, 255, 255, 200));

        registerButton = new StyledButtonCyan("Register");
        backButton = new StyledButtonCyan("Back");

        // 🔹 Gombok eseménykezelői
        registerButton.addActionListener(e -> register(
                usernameField.getText(), confirmUsernameField.getText(),
                new String(passwordField.getPassword()), new String(confirmPasswordField.getPassword())
        ));
        backButton.addActionListener(e -> mainFrame.showPanel("AuthPanel"));

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        usernameLabel.setOpaque(true);
        usernameLabel.setBackground(new Color(255, 255, 255, 50));
        usernameLabel.setPreferredSize(new Dimension(200, 30));
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        contentPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel confirmUsernameLabel = new JLabel("Confirm Username:");
        confirmUsernameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        confirmUsernameLabel.setOpaque(true);
        confirmUsernameLabel.setBackground(new Color(255, 255, 255, 50));
        confirmUsernameLabel.setPreferredSize(new Dimension(200, 30));
        confirmUsernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(confirmUsernameLabel, gbc);

        gbc.gridx = 1;
        contentPanel.add(confirmUsernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 18));
        passwordLabel.setOpaque(true);
        passwordLabel.setBackground(new Color(255, 255, 255, 50));
        passwordLabel.setPreferredSize(new Dimension(200, 30));
        passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        contentPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("Arial", Font.BOLD, 18));
        confirmPasswordLabel.setOpaque(true);
        confirmPasswordLabel.setBackground(new Color(255, 255, 255, 50));
        confirmPasswordLabel.setPreferredSize(new Dimension(200, 30));
        confirmPasswordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        contentPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        contentPanel.add(registerButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
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

    private void register(String username, String confirmUsername, String password, String confirmPassword) {
        if (!username.equals(confirmUsername)) {
            CustomDialog.showError("Usernames do not match!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            CustomDialog.showError("Passwords do not match!");
            return;
        }
        try {
            createUser(username, password);            
            User user = authPanel.authenticateUser(username, password);
            mainFrame.setUser(user);
            if (user.getHiveId().isEmpty()) {
                mainFrame.showPanel("HiveHandlerPanel");
            } else if (user.isLeader()) {
                mainFrame.showPanel("LeaderMenuPanel");
            } else {
                mainFrame.showPanel("MemberMenuPanel");
            }
        } catch (Exception e) {
            CustomDialog.showError("Error during registration: " + e.getMessage());
        }
    }

    private void createUser(String username, String password) throws Exception {
        String hashedPassword = AuthPanel.hashPassword(password);
        String userId = java.util.UUID.randomUUID().toString();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("username", username);
        userMap.put("password", hashedPassword);
        userMap.put("hive_id", null);
        userMap.put("is_leader", false);

        // Beszúrás a Supabase adatbázisba  
        Boolean success = supabase.insertUser(userMap).get();
        if (!success) {
            throw new RuntimeException("Failed to insert user into Supabase.");
        }
    }
}

