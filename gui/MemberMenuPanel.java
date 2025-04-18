package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
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
import services.PerkStorage;
import utils.SupabaseClient;
import utils.SupportUtils;

public class MemberMenuPanel extends JPanel {

    private JLabel welcomeLabel;
    private StyledButtonCyan addPerkButton;
    private StyledButtonCyan showHiveDataButton;
    private StyledButtonCyan replacePerkButton;
    private StyledButtonRed deleteAllPerksButton;
    private StyledButtonRed leaveHiveButton;
    private StyledButtonRed logoutButton;
    private StyledButtonCyan backButton;
    private MainFrame mainFrame;
    private User user;
    private PerkStorage perkStorage;
    private final SupabaseClient supabase;

    public MemberMenuPanel(MainFrame mainFrame, SupabaseClient supabase, PerkStorage perkStorage, User user) {
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
        gbc.insets = new Insets(30, 30, 30, 30);

        Dimension Size = new Dimension(400, 70);
        addPerkButton = new StyledButtonCyan("Add Perk");
        addPerkButton.setPreferredSize(Size);
        showHiveDataButton = new StyledButtonCyan("Show Hive Data");
        showHiveDataButton.setPreferredSize(Size);
        replacePerkButton = new StyledButtonCyan("Replace Perk");
        replacePerkButton.setPreferredSize(Size);
        deleteAllPerksButton = new StyledButtonRed("Delete All Perks");
        deleteAllPerksButton.setPreferredSize(Size);
        leaveHiveButton = new StyledButtonRed("Leave Hive");
        leaveHiveButton.setPreferredSize(Size);
        logoutButton = new StyledButtonRed("Logout");
        logoutButton.setPreferredSize(Size);
       
        addPerkButton.addActionListener(e -> openPerkSearchPanel(null));
        showHiveDataButton.addActionListener(e -> openHiveInfoPanel());
        replacePerkButton.addActionListener(e -> openPerkReplacementPanel());
        deleteAllPerksButton.addActionListener(e -> supabase.removeAllPerksFromMember(user.getUserId(), mainFrame));
        leaveHiveButton.addActionListener(e -> leaveHive());
        logoutButton.addActionListener(e -> mainFrame.showPanel("AuthPanel"));

        gbc.fill = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(addPerkButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(showHiveDataButton, gbc);

        gbc.gridx = 2;
        buttonPanel.add(replacePerkButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        buttonPanel.add(deleteAllPerksButton, gbc);

        gbc.gridx = 1;
        buttonPanel.add(leaveHiveButton, gbc);

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

    private void openPerkSearchPanel(String oldPerk) {
        PerkSearchPanel perkSearchPanel = new PerkSearchPanel(perkStorage, (e) -> {
            StyledButtonCyan source = (StyledButtonCyan) e.getSource();
            String selectedPerkName = source.getText();
            if (oldPerk == null) {               
                addPerkToSelf(selectedPerkName);
            } else {                
                replaceOwnedPerk(oldPerk, selectedPerkName);
            }
           
            mainFrame.showPanel("MemberMenuPanel");
        }, () -> {            
            mainFrame.showPanel("MemberMenuPanel");
        });
       
        mainFrame.getMainPanel().add(perkSearchPanel, "PerkSearchPanel");
        mainFrame.getCardLayout().show(mainFrame.getMainPanel(), "PerkSearchPanel");
    }
   
    private void addPerkToSelf(String perkName) {
        if (perkName != null) {
            try {
                supabase.addPerkToMember(user.getHiveId(), user.getUsername(), perkName, mainFrame);
            } catch (Exception e) {
                showError(e);
            }
        }
    }

    private void openPerkReplacementPanel() {
        try {           
            Map<String, List<Perk>> cachedMemberPerks = mainFrame.getCachedMemberPerks();
            String currentUsername = mainFrame.getUser().getUsername();
            List<Perk> perkList = cachedMemberPerks.get(currentUsername);

            if (perkList == null || perkList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No perks available for replacement.", "Error", JOptionPane.ERROR_MESSAGE);
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
                StyledButtonCyan perkButton = new StyledButtonCyan(perk.getName());
                perkButton.setPreferredSize(new Dimension(500, 90));
                perkButton.setFont(new Font("Arial", Font.BOLD, 17));
               
                ImageIcon icon = (ImageIcon) perkStorage.getPerkIcon(perk.getName());
                if (icon != null) {
                    Image scaledImage = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                    perkButton.setIcon(new ImageIcon(scaledImage));
                    perkButton.setHorizontalTextPosition(SwingConstants.RIGHT);
                }
               
                perkButton.addActionListener(e -> openPerkSearchPanel(perk.getName()));

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
           
            JPanel containerPanel = new JPanel(new BorderLayout());
            containerPanel.add(scrollPane, BorderLayout.CENTER);
           
            StyledButtonCyan backButton = new StyledButtonCyan("Back");                        
            backButton.addActionListener(e -> mainFrame.showPanel("MemberMenuPanel"));
            containerPanel.add(backButton, BorderLayout.SOUTH);
           
            mainFrame.getMainPanel().add(containerPanel, "PerkReplacementPanel");
            mainFrame.getCardLayout().show(mainFrame.getMainPanel(), "PerkReplacementPanel");
            mainFrame.revalidate();
            mainFrame.repaint();
        } catch (HeadlessException ex) {
            showError(ex);
        }
    }


    private void replaceOwnedPerk(String oldPerk, String newPerk) {
        if (oldPerk != null && newPerk != null) {
            try {
                supabase.replacePerkForMember(user.getHiveId(), user.getUserId(), oldPerk, newPerk, mainFrame); // using user.getUserId() and user.getHiveId()                
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
                supabase.removeMemberFromHive(user.getHiveId(), user.getUsername(), mainFrame);               
                user.setHiveId(null); 
                mainFrame.setCachedHiveData(null);
               
                CustomDialog.showInfo("You have left the Hive.");
                mainFrame.showPanel("AuthPanel");

            } catch (Exception e) {
                showError(e);
            }
        }
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

    private void showError(Exception e) {
        CustomDialog.showError("Error: " + e.getMessage());
    }

}
