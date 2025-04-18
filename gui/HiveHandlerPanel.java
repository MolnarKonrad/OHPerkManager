package gui;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import models.Hive;
import models.Perk;
import models.User;
import org.json.JSONArray;
import services.PerkStorage;
import utils.SupabaseClient;

public class HiveHandlerPanel extends JPanelWithBackground {

    private JTextField hiveNameField;
    private JTextField invitationCodeField;
    private StyledButtonCyan createHiveButton;
    private StyledButtonCyan joinHiveButton;
    private StyledButtonCyan submitButton;
    private StyledButtonCyan backButton;
    private StyledButtonCyan exitButton;
    private MainFrame mainFrame;
    private SupabaseClient supabase;
    private User user;
    private boolean isCreatingHive = false;

    public HiveHandlerPanel(PerkStorage perkStorage, MainFrame mainFrame, SupabaseClient supabase, User user) {
        this.mainFrame = mainFrame;
        this.supabase = supabase;
        this.user = user;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        createHiveButton = new StyledButtonCyan("Create Hive");
        joinHiveButton = new StyledButtonCyan("Join Hive");
        exitButton = new StyledButtonCyan("Exit");
       
        hiveNameField = new JTextField(15);
        hiveNameField.setPreferredSize(new Dimension(250, 40));
        hiveNameField.setFont(new Font("Arial", Font.PLAIN, 18));
        hiveNameField.setBackground(new Color(255, 255, 255, 200));

        invitationCodeField = new JTextField(15);
        invitationCodeField.setPreferredSize(new Dimension(250, 40));
        invitationCodeField.setFont(new Font("Arial", Font.PLAIN, 18));
        invitationCodeField.setBackground(new Color(255, 255, 255, 200));

        submitButton = new StyledButtonCyan("Submit");
        backButton = new StyledButtonCyan("Back");
        
        createHiveButton.addActionListener(e -> showCreateHiveInput(gbc));
        joinHiveButton.addActionListener(e -> showJoinHiveInput(gbc));
        submitButton.addActionListener(e -> {
            try {
                handleSubmit(perkStorage);
            } catch (IOException ex) {
                Logger.getLogger(HiveHandlerPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(HiveHandlerPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        backButton.addActionListener(e -> {
            hiveNameField.setText("");
            invitationCodeField.setText("");
           
            isCreatingHive = false;
            removeAll();
            revalidate();
            repaint();
          
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 10, 10, 10);

            gbc.gridx = 0;
            gbc.gridy = 0; // Gomb 1  
            add(createHiveButton, gbc);
            gbc.gridx = 0;
            gbc.gridy = 1; // Gomb 2  
            add(joinHiveButton, gbc);
            gbc.gridx = 0;
            gbc.gridy = 2; // Gomb 3  
            add(exitButton, gbc);
             
            revalidate();
            repaint();
        });
        exitButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Exiting...", "Exit", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        });
       
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(createHiveButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(joinHiveButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(exitButton, gbc);
    }

    public void setUser(User user) {
        this.user = user;       
    }

    private void showCreateHiveInput(GridBagConstraints gbc) {
        isCreatingHive = true;

        removeAll();
        revalidate();
        repaint();

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel hiveNameLabel = new JLabel("Enter the Hive's name: ");
        hiveNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        hiveNameLabel.setOpaque(true);
        hiveNameLabel.setBackground(new Color(255, 255, 255, 50)); 
        hiveNameLabel.setPreferredSize(new Dimension(250, 30));
        hiveNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(hiveNameLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(hiveNameField, gbc);
       
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(submitButton, gbc); 

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(backButton, gbc);
       
        revalidate();
        repaint();
    }

    private void showJoinHiveInput(GridBagConstraints gbc) {
        isCreatingHive = false;

        removeAll();
        revalidate();
        repaint();

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel invitationCodeLabel = new JLabel("Enter the invitation code: ");
        invitationCodeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        invitationCodeLabel.setOpaque(true);
        invitationCodeLabel.setBackground(new Color(255, 255, 255, 50));
        invitationCodeLabel.setPreferredSize(new Dimension(250, 30)); 
        invitationCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(invitationCodeLabel, gbc);
       
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(invitationCodeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(submitButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(backButton, gbc);
       
        revalidate();
        repaint();
    }

    private void handleSubmit(PerkStorage perkStorage) throws IOException, Exception {
        if (isCreatingHive) {
            createHive(hiveNameField.getText(), perkStorage);
        } else {
            joinHive(invitationCodeField.getText(), perkStorage);
        }
    }

    private void createHive(String hiveName, PerkStorage perkStorage) {
        if (hiveName == null || hiveName.trim().isEmpty()) {
            CustomDialog.showError("Please enter a valid hive name.");
            return;
        }

        try {           
            String hiveId = UUID.randomUUID().toString();
            String leaderName = user.getUsername();
           
            Hive newHive = new Hive(hiveId, hiveName, leaderName, null);
           
            supabase.insertHive(newHive);
            
            supabase.addHiveMember(hiveId, user.getUserId());
           
            user.setHiveId(hiveId);
            user.setLeader(true);
            
            supabase.updateUserHiveInfo(user.getUserId(), user.getHiveId(), user.isLeader());
           
            Map<String, Object> hiveData = supabase.getHiveData(hiveId, user.getUserId(), perkStorage);
            if (hiveData != null) {
                mainFrame.setCachedHiveData(hiveData);
            } else {               
                mainFrame.setCachedHiveData(Map.of(
                        "hiveName", hiveName,
                        "members", List.of(user),
                        "memberPerks", Map.of(user.getUsername(), new ArrayList<Perk>())
                ));
            }

            CustomDialog.showInfo("Hive successfully created: " + hiveName);
            mainFrame.updateCachedHiveMembers();
            mainFrame.showPanel("LeaderMenuPanel");

        } catch (Exception e) {
            showError(e);
        }
    }

    private void joinHive(String inviteCode, PerkStorage perkStorage) throws IOException, Exception {
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            CustomDialog.showError("Please enter a valid invitation code.");
            return;
        }

        try {           
            String inviteCodeEndpoint = "/rest/v1/invite_codes?invite_code=eq." + inviteCode + "&select=hive_id";
            String inviteCodeResponse = supabase.get(inviteCodeEndpoint);
            JSONArray inviteCodeArray = new JSONArray(inviteCodeResponse);

            if (inviteCodeArray.length() == 0) {
                CustomDialog.showError("Invalid invitation code!");
                return;
            }
           
            String hiveId = inviteCodeArray.getJSONObject(0).optString("hive_id", null);
            if (hiveId == null) {
                CustomDialog.showError("Invalid invitation code!");
                return;
            }
          
            if (user.getHiveId() != null && !user.getHiveId().trim().isEmpty()) {
                CustomDialog.showError("You are already a member of another Hive! You must leave it first.");
                return;
            }
           
            String membersEndpoint = "/rest/v1/hives?id=eq." + hiveId + "&select=hive_members";
            String membersResponse = supabase.get(membersEndpoint);
            
            JSONArray membersArray = new JSONArray(membersResponse);
           
            if (membersArray.length() == 0) {
                CustomDialog.showError("Hive not found.");
                return;
            }
            
            JSONArray currentMembersArray = membersArray.getJSONObject(0).optJSONArray("hive_members");
            if (currentMembersArray == null) {
                currentMembersArray = new JSONArray(); // Initialize if null
            }
            
            if (currentMembersArray.length() >= 16) {
                CustomDialog.showError("This Hive has already reached its maximum membership (16 people).");
                return;
            }
           
            supabase.updateUserHiveInfo(user.getUserId(), hiveId);           
            supabase.addHiveMember(hiveId, user.getUserId());
            
            user.setHiveId(hiveId);
           
            Map<String, Object> hiveData = supabase.getHiveData(hiveId, user.getUserId(), perkStorage);
            if (hiveData != null) {
                mainFrame.setCachedHiveData(hiveData);
            } else {
                mainFrame.setCachedHiveData(Map.of(
                        "hiveName", "Unknown Hive",
                        "members", List.of(user),
                        "memberPerks", Map.of(user.getUsername(), new ArrayList<>())
                ));
            }

            String hiveName = (String) mainFrame.getCachedHiveData().get("hiveName");
            CustomDialog.showInfo("You have successfully joined the Hive: " + hiveName);
            mainFrame.showPanel("MemberMenuPanel");

        } catch (InterruptedException e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        CustomDialog.showError("Error: " + e.getMessage());
    }
}
