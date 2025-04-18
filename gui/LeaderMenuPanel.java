package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import models.Perk;
import models.User;
import services.PerkStorage;
import utils.SupabaseClient;
import utils.SupportUtils;

public class LeaderMenuPanel extends JPanel {

    private JLabel welcomeLabel;
    private StyledButtonCyan generateInviteCodeButton;
    private StyledButtonCyan addPerkToMemberButton;
    private StyledButtonCyan showHiveDataButton;
    private StyledButtonCyan replacePerkForMemberButton;
    private StyledButtonRed removeMemberButton;
    private StyledButtonRed deleteAllPerksAtSelfButton;
    private StyledButtonRed transferLeadershipButton;
    private StyledButtonRed disbandHiveButton;
    private StyledButtonRed logoutButton;
    private MainFrame mainFrame;
    private PerkStorage perkStorage;
    private final User user;
    private final SupabaseClient supabase;

    public LeaderMenuPanel(MainFrame mainFrame, SupabaseClient supabase, PerkStorage perkStorage, User user) {
        this.mainFrame = mainFrame;
        this.supabase = supabase;
        this.perkStorage = perkStorage;
        this.user = user;

        setLayout(new BorderLayout());
        
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        welcomePanel.setOpaque(true);
        welcomePanel.setBackground(new Color(81, 203, 203, 100));
        welcomeLabel = new JLabel("Welcome,  " + user.getUsername(), SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setOpaque(false);
        welcomePanel.add(welcomeLabel);
        add(welcomePanel, BorderLayout.NORTH);
      
        JPanelWithBackground buttonPanel = new JPanelWithBackground(new GridBagLayout());
        setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        generateInviteCodeButton = new StyledButtonCyan("Generate Invite Code");
        addPerkToMemberButton = new StyledButtonCyan("Add Perk to Member");
        showHiveDataButton = new StyledButtonCyan("Show Hive Data");
        replacePerkForMemberButton = new StyledButtonCyan("Replace Perk for Member");
        removeMemberButton = new StyledButtonRed("Remove Member");
        deleteAllPerksAtSelfButton = new StyledButtonRed("Delete All Owned Perks");
        transferLeadershipButton = new StyledButtonRed("Transfer Leadership");
        disbandHiveButton = new StyledButtonRed("Disband Hive");
        logoutButton = new StyledButtonRed("Logout");
      
        generateInviteCodeButton.addActionListener(e -> generateInviteCode());
        addPerkToMemberButton.addActionListener(e -> openMemberSelectionPanel(true, false, false));
        showHiveDataButton.addActionListener(e -> openHiveInfoPanel());
        replacePerkForMemberButton.addActionListener(e -> openMemberSelectionPanel(false, false, true));
        removeMemberButton.addActionListener(e -> openMemberSelectionPanel(false, false, false));
        deleteAllPerksAtSelfButton.addActionListener(e -> supabase.removeAllPerksFromMember(user.getUserId(), mainFrame));
        transferLeadershipButton.addActionListener(e -> openMemberSelectionPanel(false, true, false));
        disbandHiveButton.addActionListener(e -> {
            try {
                disbandHive();
            } catch (IOException ex) {
                Logger.getLogger(LeaderMenuPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(LeaderMenuPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        logoutButton.addActionListener(e -> mainFrame.showPanel("AuthPanel"));
        
        Dimension Size = new Dimension(400, 70);        
        generateInviteCodeButton.setPreferredSize(Size);        
        addPerkToMemberButton.setPreferredSize(Size);        
        showHiveDataButton.setPreferredSize(Size);        
        replacePerkForMemberButton.setPreferredSize(Size);        
        removeMemberButton.setPreferredSize(Size);        
        deleteAllPerksAtSelfButton.setPreferredSize(Size);        
        transferLeadershipButton.setPreferredSize(Size);        
        disbandHiveButton.setPreferredSize(Size);        
        logoutButton.setPreferredSize(Size);
        
        gbc.fill = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(generateInviteCodeButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(addPerkToMemberButton, gbc);
        
        gbc.gridx = 2;
        buttonPanel.add(showHiveDataButton, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        buttonPanel.add(replacePerkForMemberButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(removeMemberButton, gbc);

        gbc.gridx = 2;
        buttonPanel.add(deleteAllPerksAtSelfButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        buttonPanel.add(transferLeadershipButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(disbandHiveButton, gbc);

        gbc.gridx = 2;
        buttonPanel.add(logoutButton, gbc);

        add(buttonPanel, BorderLayout.CENTER);       
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(true);
        bottomPanel.setBackground(new Color(81, 203, 203, 100));
        
        StyledButtonCyan userGuideButton = new StyledButtonCyan("User Guide");
        userGuideButton.setFont(new Font("Arial", Font.BOLD, 17));
        userGuideButton.setPreferredSize(new Dimension(150, 50));
        userGuideButton.addActionListener(e -> SupportUtils.openUserGuideLink());
       
        StyledButtonCyan supportButton = new StyledButtonCyan("Support");
        supportButton.setFont(new Font("Arial", Font.BOLD, 17));
        supportButton.setPreferredSize(new Dimension(150, 50));
        supportButton.addActionListener(e -> SupportUtils.openPatreonLink());

        bottomPanel.add(userGuideButton, BorderLayout.WEST);
        bottomPanel.add(supportButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void openMemberSelectionPanel(boolean isAddingPerk, boolean isTransferingLeadership, boolean isReplacingPerk) {
        try {           
            List<User> members = mainFrame.getCachedHiveMembers();

            MemberSelectionPanel panel = new MemberSelectionPanel(members, (ActionEvent e) -> {
                StyledButtonCyan source = (StyledButtonCyan) e.getSource();
                String selectedMember = source.getText();
               
                User selectedUser = members.stream()
                        .filter(altUser -> altUser.getUsername().equals(selectedMember))
                        .findFirst()
                        .orElse(null);

                if (selectedUser == null) {
                    CustomDialog.showError("User not found!");
                    return;
                }

                if (isTransferingLeadership) {
                    supabase.selectNewLeaderByName(user.getHiveId(), selectedMember, mainFrame);                     
                    mainFrame.updateCachedHiveMembers();
                } else if (isAddingPerk) {
                    openPerkSearchPanel(selectedUser, null);
                } else if (isReplacingPerk) {
                    openPerkSelectionPanel(selectedUser);
                } else {
                    try {
                        supabase.removeMemberFromHive(user.getHiveId(), selectedMember, mainFrame);
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
            Map<String, Object> cachedHiveData = mainFrame.getCachedHiveData();
            @SuppressWarnings("unchecked")
            Map<String, List<Perk>> memberPerksMap = (Map<String, List<Perk>>) cachedHiveData.get("memberPerks");
            List<Perk> perkList = memberPerksMap.get(selectedUser.getUsername());

            if (perkList == null || perkList.isEmpty()) {
                CustomDialog.showError("The member you chose has no perks.");
                return;
            }

            JPanel perkSelectionPanel = new JPanelWithBackground();
            perkSelectionPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.CENTER;

            int maxPerks = Math.min(perkList.size(), 10);
            int row = 0;
            for (int i = 0; i < maxPerks; i++) {
                Perk perk = perkList.get(i);
                StyledButtonCyan perkButton = new StyledButtonCyan(formatPerkNameForButton(perk.getName()));
                perkButton.setPreferredSize(new Dimension(400, 90));
                perkButton.setFont(new Font("Arial", Font.BOLD, 17));

                ImageIcon icon = (ImageIcon) perkStorage.getPerkIcon(perk.getName());
                if (icon != null) {
                    Image scaledImage = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                    perkButton.setIcon(new ImageIcon(scaledImage));
                }

                perkButton.addActionListener(e -> openPerkSearchPanel(selectedUser, perk));

                gbc.gridx = i % 2;
                gbc.gridy = row;
                perkSelectionPanel.add(perkButton, gbc);

                if (i % 2 == 1) {
                    row++;
                }
            }

            JScrollPane scrollPane = new JScrollPane(perkSelectionPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setPreferredSize(new Dimension(300, 400));

            mainFrame.getMainPanel().add(scrollPane, "PerkSelectionPanel");
            mainFrame.getCardLayout().show(mainFrame.getMainPanel(), "PerkSelectionPanel");
            mainFrame.revalidate();
            mainFrame.repaint();

        } catch (Exception ex) {
            showError(ex);
        }
    }
    
    private String formatPerkNameForButton(String perkName) {
        if (perkName == null) {
            return "";
        }       
        String formatted = perkName.replace(":", ":<br>");        
        formatted = formatted.replaceAll("(?<=\\S{12})(?=\\S)", "<br>");
        return "<html>" + formatted + "</html>";
    }

    private void openPerkSearchPanel(User selectedUser, Perk oldPerk) {
        PerkSearchPanel perkSearchPanel = new PerkSearchPanel(perkStorage, (ActionEvent e) -> {
            StyledButtonCyan source = (StyledButtonCyan) e.getSource();
            String selectedPerkName = source.getText();
            if (oldPerk == null) {
                try {                    
                    supabase.addPerkToMember(user.getHiveId(), selectedUser.getUsername(), selectedPerkName, mainFrame);
                } catch (IOException ex) {
                    Logger.getLogger(LeaderMenuPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(LeaderMenuPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {               
                supabase.replacePerkForMember(user.getHiveId(), selectedUser.getUserId(), oldPerk.getName(), selectedPerkName, mainFrame);
            }
            mainFrame.showPanel("LeaderMenuPanel");
        }, () -> mainFrame.showPanel("LeaderMenuPanel"));

        mainFrame.getMainPanel().add(perkSearchPanel, "PerkSearchPanel");
        mainFrame.getCardLayout().show(mainFrame.getMainPanel(), "PerkSearchPanel");
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void openHiveInfoPanel() {
        try {           
            String hiveName = mainFrame.getCachedHiveName();
            List<User> members = mainFrame.getCachedHiveMembers();
            Map<String, List<Perk>> memberPerks = mainFrame.getCachedMemberPerks();

            if (hiveName == null || members == null || memberPerks == null) {
                CustomDialog.showError("Hive data is missing. Please try again.");
                return;
            }
            
            HiveInfoPanel hiveInfoPanel = (HiveInfoPanel) mainFrame.getMainPanel().getComponent(6);
            hiveInfoPanel.updateData(members, memberPerks, perkStorage);
            hiveInfoPanel.updateHiveName(hiveName);
            
            mainFrame.showPanel("HiveInfoPanel");

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void generateInviteCode() {
        try {
            supabase.generateNewInviteCode(user.getHiveId());
        } catch (IOException | InterruptedException e) {
            showError(e);
        }
    }

    private void disbandHive() throws IOException, InterruptedException {
        supabase.disbandHive(user.getHiveId(), mainFrame);        
    }

    private void showError(Exception e) {
        CustomDialog.showError("Error: " + e.getMessage());
    }

}
