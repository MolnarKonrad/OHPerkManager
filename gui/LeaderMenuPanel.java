package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import models.Perk;
import models.User;
import services.FirebaseService;
import services.PerkStorage;
import utils.SupportUtils;

public class LeaderMenuPanel extends JPanelWithBackground {

    private JLabel welcomeLabel;
    private StyledButton generateInviteCodeButton;
    private StyledButton addPerkToMemberButton;
    private StyledButton removeMemberButton;
    private StyledButton transferLeadershipButton;
    private StyledButton replacePerkForMemberButton;
    private StyledButton deleteAllPerksAtSelfButton;
    private StyledButton showHiveDataButton;
    private StyledButton disbandHiveButton;
    private StyledButton logoutButton;
    private MainFrame mainFrame;
    private FirebaseService firebaseService;
    private PerkStorage perkStorage;
    private final User user;

    public LeaderMenuPanel(MainFrame mainFrame, FirebaseService firebaseService, PerkStorage perkStorage, User user) {
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

        generateInviteCodeButton = new StyledButton("Generate Invite Code");
        addPerkToMemberButton = new StyledButton("Add Perk to Member");
        removeMemberButton = new StyledButton("Remove Member");
        transferLeadershipButton = new StyledButton("Transfer Leadership");
        replacePerkForMemberButton = new StyledButton("Replace Perk for Member");
        deleteAllPerksAtSelfButton = new StyledButton("Delete All Owned Perks");
        showHiveDataButton = new StyledButton("Show HIVE Data");
        disbandHiveButton = new StyledButton("Disband HIVE");
        logoutButton = new StyledButton("Logout");

        // 🔹 Gombok eseménykezelői
        generateInviteCodeButton.addActionListener(e -> generateInviteCode());
        addPerkToMemberButton.addActionListener(e -> openMemberSelectionPanel(true, false, false));
        removeMemberButton.addActionListener(e -> openMemberSelectionPanel(false, false, false));
        transferLeadershipButton.addActionListener(e -> openMemberSelectionPanel(false, true, false));
        replacePerkForMemberButton.addActionListener(e -> openMemberSelectionPanel(false, false, true));
        deleteAllPerksAtSelfButton.addActionListener(e -> firebaseService.removeAllPerksFromMember(user.getUserId(), mainFrame));
        showHiveDataButton.addActionListener(e -> openHiveInfoPanel());
        disbandHiveButton.addActionListener(e -> disbandHive());
        logoutButton.addActionListener(e -> mainFrame.showPanel("AuthPanel"));

        // 🔹 Gombok méretezése
        Dimension minButtonSize = new Dimension(100, 30);
        generateInviteCodeButton.setMinimumSize(minButtonSize);
        addPerkToMemberButton.setMinimumSize(minButtonSize);
        removeMemberButton.setMinimumSize(minButtonSize);
        transferLeadershipButton.setMinimumSize(minButtonSize);
        replacePerkForMemberButton.setMinimumSize(minButtonSize);
        deleteAllPerksAtSelfButton.setMinimumSize(minButtonSize);
        showHiveDataButton.setMinimumSize(minButtonSize);
        disbandHiveButton.setMinimumSize(minButtonSize);
        logoutButton.setMinimumSize(minButtonSize);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(generateInviteCodeButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(addPerkToMemberButton, gbc);

        gbc.gridx = 2;
        buttonPanel.add(removeMemberButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        buttonPanel.add(transferLeadershipButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(replacePerkForMemberButton, gbc);

        gbc.gridx = 2;
        buttonPanel.add(deleteAllPerksAtSelfButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        buttonPanel.add(showHiveDataButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(disbandHiveButton, gbc);

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

    private void openMemberSelectionPanel(boolean isAddingPerk, boolean isTransferingLeadership, boolean isReplacingPerk) {
        try {
            // 🔹 HELYI gyorsítótárból dolgozunk  
            List<User> members = mainFrame.getCachedHiveMembers();

            MemberSelectionPanel panel = new MemberSelectionPanel(members, (ActionEvent e) -> {
                StyledButton source = (StyledButton) e.getSource();
                String selectedMember = source.getText();

                // 🔹 Keresés a helyi members listában  
                User selectedUser = members.stream()
                        .filter(user -> user.getUsername().equals(selectedMember))
                        .findFirst()
                        .orElse(null);

                if (selectedUser == null) {
                    CustomDialog.showError("User not found!");
                    return;
                }

                if (isTransferingLeadership) {
                    firebaseService.selectNewLeaderByName(user.getHiveId(), selectedMember, mainFrame);
                    CustomDialog.showInfo("Leadership transferred successfully!");

                    // 🔹 Firestore és helyi gyorsítótár frissítése  
                    mainFrame.updateCachedHiveMembers();
                } else if (isAddingPerk) {
                    openPerkSearchPanel(selectedUser, null);
                } else if (isReplacingPerk) {
                    openPerkSelectionPanel(selectedUser);
                } else {
                    try {
                        firebaseService.removeMemberFromHive(user.getHiveId(), selectedMember, mainFrame);

                        // 🔹 Firestore és helyi gyorsítótár frissítése  
//                        mainFrame.updateCachedHiveMembers();
                        mainFrame.showPanel("LeaderMenuPanel");
                    } catch (ExecutionException | InterruptedException ex) {
                        showError(ex);
                    }
                }
            }, () -> mainFrame.showPanel("LeaderMenuPanel"));

            mainFrame.getMainPanel().add(panel, "MemberSelectionPanel");
            mainFrame.getCardLayout().show(mainFrame.getMainPanel(), "MemberSelectionPanel");
            mainFrame.revalidate();
            mainFrame.repaint();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void openPerkSelectionPanel(User selectedUser) {
        try {
            List<String> perkNames = firebaseService.getMemberPerks(selectedUser.getUserId());

            if (perkNames == null || perkNames.isEmpty()) {
                CustomDialog.showError("The member you chose has no perks.");
                return;
            }

            JPanel perkSelectionPanel = new JPanelWithBackground(); // Panel típusa  
            perkSelectionPanel.setLayout(new GridBagLayout()); // Használj GridBagLayout-ot  
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10); // Margók a gombok körül  
            gbc.anchor = GridBagConstraints.CENTER; // Középre igazítás  

            int maxMembers = Math.min(perkNames.size(), 10); // Max 10 gomb (2 oszlop, 5 sor)  
            int row = 0; // Sor számláló  
            for (int i = 0; i < maxMembers; i++) {
                String perkName = perkNames.get(i);
                Perk perk = perkStorage.getPerkByName(perkName);
                if (perk == null) {
                    continue;
                }

                // Használjuk a formázott szöveget a gomb szövegeként,
                // ahol a formázás sortördeléseket ad ":" után, illetve két hosszú szó után.
                StyledButton perkButton = new StyledButton(formatPerkNameForButton(perk.getName()));
                perkButton.setPreferredSize(new Dimension(400, 90)); // Gomb pontos mérete 350x90  
                perkButton.setFont(new Font("Times New Roman", Font.BOLD, 17)); // Betűtípus  

                // Beállítjuk az ikont (60x60)  
                ImageIcon icon = (ImageIcon) perkStorage.getPerkIcon(perkName);
                if (icon != null) {
                    Image scaledImage = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH); // 60x60 ikon  
                    perkButton.setIcon(new ImageIcon(scaledImage));
                }

                // Gomb interakció beállítása  
                perkButton.addActionListener(e -> openPerkSearchPanel(selectedUser, perk));

                // GridBagConstraints beállítása  
                gbc.gridx = i % 2; // 2 oszlop  
                gbc.gridy = row; // Aktuális sor  
                perkSelectionPanel.add(perkButton, gbc);

                // Sor növelése  
                if (i % 2 == 1) {
                    row++; // Csak akkor növeljük, ha 2 gomb hozzá lett adva  
                }
            }

            JScrollPane scrollPane = new JScrollPane(perkSelectionPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setPreferredSize(new Dimension(300, 400)); // ScrollPane mérete  

            mainFrame.getMainPanel().add(scrollPane, "PerkSelectionPanel");
            mainFrame.getCardLayout().show(mainFrame.getMainPanel(), "PerkSelectionPanel");
            mainFrame.revalidate();
            mainFrame.repaint();
        } catch (ExecutionException | InterruptedException ex) {
            showError(ex);
        }
    }

    /**
     * Formázza a perk nevet HTML-s sortördeléssel. A ":" után, illetve ha egy
     * szó túl hosszú (például 12 karakter után) automatikusan sortördelést ad.
     */
    private String formatPerkNameForButton(String perkName) {
        if (perkName == null) {
            return "";
        }
        // Helyettesítjük a ":" karaktert ":" + <br>
        String formatted = perkName.replace(":", ":<br>");
        // Ha egy szó hosszabb 12 karakternél, akkor sortördelést adunk (ez egyszerűsített megoldás)
        formatted = formatted.replaceAll("(?<=\\S{12})(?=\\S)", "<br>");
        return "<html>" + formatted + "</html>";
    }

    private void openPerkSearchPanel(User selectedUser, Perk oldPerk) {
        PerkSearchPanel perkSearchPanel = new PerkSearchPanel(perkStorage, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StyledButton source = (StyledButton) e.getSource();
                String selectedPerkName = source.getText();
                if (oldPerk == null) {
                    // Perk hozzáadása
                    firebaseService.addPerkToMember(user.getHiveId(), selectedUser.getUsername(), selectedPerkName, mainFrame);
                } else {
                    // Perk csere
                    firebaseService.replacePerkForMember(user.getHiveId(), selectedUser.getUserId(), oldPerk.getName(), selectedPerkName, mainFrame);
                }
                mainFrame.showPanel("LeaderMenuPanel");
            }
        }, () -> mainFrame.showPanel("LeaderMenuPanel"));

        mainFrame.getMainPanel().add(perkSearchPanel, "PerkSearchPanel");
        mainFrame.getCardLayout().show(mainFrame.getMainPanel(), "PerkSearchPanel");
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    // Normalizálás: kisbetűsítés és extra szóközök eltávolítása
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase(); // Kisbetűs és a felesleges szóközök eltávolítása
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

    private void generateInviteCode() {
        try {
            firebaseService.generateNewInviteCode(user.getHiveId());
        } catch (Exception e) {
            showError(e);
        }
    }

    private void disbandHive() {
        firebaseService.disbandHive(user.getHiveId());
        mainFrame.showPanel("AuthPanel");
    }

    private void showError(Exception e) {
        CustomDialog.showError("Error: " + e.getMessage());
    }

}
