package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import models.Perk;
import models.User;
import services.FirebaseService;
import services.PerkStorage;
import utils.SupportUtils;

public class MemberMenuPanel extends JPanelWithBackground {

    private JLabel welcomeLabel;
    private StyledButton addPerkButton;
    private StyledButton replacePerkButton;
    private StyledButton deleteAllPerksButton;
    private StyledButton showHiveDataButton;
    private StyledButton leaveHiveButton;
    private StyledButton logoutButton;
    private MainFrame mainFrame;
    private FirebaseService firebaseService;
    private User user;
    private PerkStorage perkStorage;

    public MemberMenuPanel(MainFrame mainFrame, FirebaseService firebaseService, PerkStorage perkStorage, User user) {
        this.mainFrame = mainFrame;
        this.firebaseService = firebaseService;
        this.perkStorage = perkStorage;
        this.user = user;

        setLayout(new BorderLayout());

        // 🔹 Üdvözlő panel
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        welcomePanel.setOpaque(true);
        welcomePanel.setBackground(new Color(81, 203, 203, 100));
        welcomeLabel = new JLabel("Welcome,  " + user.getUsername(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        welcomeLabel.setOpaque(true);
        welcomeLabel.setBackground(new Color(81, 203, 203, 100));
        welcomePanel.add(welcomeLabel);
        add(welcomePanel, BorderLayout.NORTH);

        // 🔹 Gombokat tartalmazó fő panel
        JPanelWithBackground buttonPanel = new JPanelWithBackground(new GridBagLayout());
        setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        addPerkButton = new StyledButton("Add Perk");
        replacePerkButton = new StyledButton("Replace Perk");
        deleteAllPerksButton = new StyledButton("Delete All Perks");
        showHiveDataButton = new StyledButton("Show HIVE Data");
        leaveHiveButton = new StyledButton("Leave HIVE");
        logoutButton = new StyledButton("Logout");

        // 🔹 Gombok eseménykezelői
        addPerkButton.addActionListener(e -> openPerkSearchPanel(null));
        replacePerkButton.addActionListener(e -> openPerkReplacementPanel());
        deleteAllPerksButton.addActionListener(e -> firebaseService.removeAllPerksFromMember(user.getUserId(), mainFrame));
        showHiveDataButton.addActionListener(e -> openHiveInfoPanel());
        leaveHiveButton.addActionListener(e -> leaveHive());
        logoutButton.addActionListener(e -> mainFrame.showPanel("AuthPanel"));

        // 🔹 Gombok méretezése
        Dimension minButtonSize = new Dimension(100, 30);
        addPerkButton.setMinimumSize(minButtonSize);
        replacePerkButton.setMinimumSize(minButtonSize);
        deleteAllPerksButton.setMinimumSize(minButtonSize);
        showHiveDataButton.setMinimumSize(minButtonSize);
        leaveHiveButton.setMinimumSize(minButtonSize);
        logoutButton.setMinimumSize(minButtonSize);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(addPerkButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(replacePerkButton, gbc);

        gbc.gridx = 2;
        buttonPanel.add(deleteAllPerksButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        buttonPanel.add(showHiveDataButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(leaveHiveButton, gbc);

        gbc.gridx = 2;
        buttonPanel.add(logoutButton, gbc);

        add(buttonPanel, BorderLayout.CENTER);

        // 🔹 Support gomb a jobb alsó sarokban
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        StyledButton supportButton = new StyledButton("Support");
        supportButton.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        supportButton.setPreferredSize(new Dimension(120, 40));
        supportButton.addActionListener(e -> SupportUtils.openPatreonLink());

        bottomPanel.add(supportButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void openPerkSearchPanel(String oldPerk) {
        PerkSearchPanel perkSearchPanel = new PerkSearchPanel(perkStorage, (e) -> {
            StyledButton source = (StyledButton) e.getSource();
            String selectedPerkName = source.getText(); // Get the perk name from the button's text
            if (oldPerk == null) {
                // 🔹 Ha nincs megadva régi perk, akkor új perket adunk hozzá
                addPerkToSelf(selectedPerkName);
            } else {
                // 🔹 Ha van megadva régi perk, akkor cserélünk
                replaceOwnedPerk(oldPerk, selectedPerkName);
            }

            // Add the selected perk to the member
            mainFrame.showPanel("MemberMenuPanel");// Pass the selected perk name
        }, () -> {
            // Handle back action if needed
            mainFrame.showPanel("MemberMenuPanel");
        });

        // Show the PerkSearchPanel in the MainFrame
        mainFrame.getMainPanel().add(perkSearchPanel, "PerkSearchPanel");
        mainFrame.getCardLayout().show(mainFrame.getMainPanel(), "PerkSearchPanel");
    }

    // Funkciók a gombokhoz  
    private void addPerkToSelf(String perkName) {
        if (perkName != null) {
            try {
                firebaseService.addPerkToMember(user.getHiveId(), user.getUsername(), perkName, mainFrame);
            } catch (Exception e) {
                showError(e);
            }
        }
    }

    private void openPerkReplacementPanel() {
        try {
            // Fetch perks for the current user  
            List<String> perkNames = firebaseService.getMemberPerks(user.getUserId());

            if (perkNames == null || perkNames.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No perks available for replacement.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create a panel to display the perk buttons with a background  
            JPanel perkSelectionPanel = new JPanelWithBackground(); // Use JPanelWithBackground  
            perkSelectionPanel.setLayout(new GridBagLayout()); // Use GridBagLayout  
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10); // Margók a gombok körül  
            gbc.anchor = GridBagConstraints.CENTER; // Center alignment  

            int maxMembers = Math.min(perkNames.size(), 10); // Max 10 buttons  
            int row = 0; // Row counter  
            for (int i = 0; i < maxMembers; i++) {
                String perkName = perkNames.get(i);
                Perk perk = perkStorage.getPerkByName(perkName);

                if (perk != null) {
                    StyledButton perkButton = new StyledButton(perk.getName());
                    perkButton.setPreferredSize(new Dimension(400, 90)); // Button size 300x90  
                    perkButton.setFont(new Font("Times New Roman", Font.BOLD, 17)); // Font  

                    // Set the icon (60x60)  
                    ImageIcon icon = (ImageIcon) perkStorage.getPerkIcon(perkName);
                    if (icon != null) {
                        Image scaledImage = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH); // 60x60 icon  
                        perkButton.setIcon(new ImageIcon(scaledImage));
                    }

                    // Button interaction setup  
                    perkButton.addActionListener(e -> {
                        // Call to open the PerkSearchPanel for selecting the new perk to replace the old one  
                        openPerkSearchPanel(perkName); // Send the selected old perk name for replacement   
                    });

                    // GridBagConstraints setup  
                    gbc.gridx = i % 2; // 2 columns  
                    gbc.gridy = row; // Current row  
                    perkSelectionPanel.add(perkButton, gbc);

                    // Increase row counter  
                    if (i % 2 == 1) {
                        row++; // Only increase when 2 buttons have been added  
                    }
                }
            }

            JScrollPane scrollPane = new JScrollPane(perkSelectionPanel); // Allow scrolling if there are many perks  
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setPreferredSize(new Dimension(300, 400)); // ScrollPane size  

            // Show the perks selection panel  
            mainFrame.getMainPanel().add(scrollPane, "PerkSelectionPanel");
            mainFrame.getCardLayout().show(mainFrame.getMainPanel(), "PerkSelectionPanel");
            mainFrame.revalidate();
            mainFrame.repaint();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void replaceOwnedPerk(String oldPerk, String newPerk) {
        if (oldPerk != null && newPerk != null) {
            try {
                firebaseService.replacePerkForMember(user.getHiveId(), user.getUserId(), oldPerk, newPerk, mainFrame); // using user.getUserId() and user.getHiveId()                
                mainFrame.showPanel("MemberMenuPanel");
            } catch (Exception e) {
                showError(e);
            }
        }
    }

    private void leaveHive() {
        int confirm = CustomDialog.showConfirm("Are you sure you want to leave your Hive?", "Confirm");
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 🔹 FIRESTORE FRISSÍTÉS (A felhasználót eltávolítjuk a HIVE-ból)
                firebaseService.removeMemberFromHive(user.getHiveId(), user.getUsername(), mainFrame);

                // 🔹 HELYI ADATOK FRISSÍTÉSE (Garancia arra, hogy friss adatok lesznek!)
                user.setHiveId(null);  // A felhasználó már nem tagja egyetlen HIVE-nak sem

                mainFrame.setCachedHiveData(null); // Kiürítjük a gyorsítótárat

                // 🔹 Visszatérés az AuthPanelre
                CustomDialog.showInfo("You have left the Hive.");
                mainFrame.showPanel("AuthPanel");

            } catch (Exception e) {
                showError(e);
            }
        }
    }

    private void openHiveInfoPanel() {
        try {
            // 🔹 A HIVE adatok lekérése a MainFrame-ből, NEM a Firestore-ból!  
            String hiveName = mainFrame.getCachedHiveName(); // A MainFrame-ben eltárolt HIVE név  
            List<User> members = mainFrame.getCachedHiveMembers(); // Előzőleg letöltött tagok listája  
            Map<String, List<Perk>> memberPerks = mainFrame.getCachedMemberPerks(); // Tagok perkjei  

            if (hiveName == null || members == null || memberPerks == null) {
                CustomDialog.showError("Hive data is missing. Please try again.");
                return;
            }

            // 🔹 Frissítjük a HiveInfoPanelt az előzőleg letöltött adatokkal  
            HiveInfoPanel hiveInfoPanel = (HiveInfoPanel) mainFrame.getMainPanel().getComponent(6);
            hiveInfoPanel.updateData(members, memberPerks, perkStorage);
            hiveInfoPanel.updateHiveName(hiveName);

            // 🔹 Átváltunk a HiveInfoPanel-re  
            mainFrame.showPanel("HiveInfoPanel");

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showError(Exception e) {
        CustomDialog.showError("Error: " + e.getMessage());
    }

}
