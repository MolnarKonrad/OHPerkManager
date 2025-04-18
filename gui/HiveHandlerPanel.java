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
    private SupabaseClient supabase;  // Firestore példány   
    private User user; // A felhasználó, aki a HIVE-ot kezeli    
    private boolean isCreatingHive = false; // Nyomkövető a HIVE létrehozásához  

    public HiveHandlerPanel(PerkStorage perkStorage, MainFrame mainFrame, SupabaseClient supabase, User user) {
        this.mainFrame = mainFrame;
        this.supabase = supabase; // Firestore inicializálás  
        this.user = user; // A felhasználó beállítása  
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10); // Margó  

        createHiveButton = new StyledButtonCyan("Create Hive");
        joinHiveButton = new StyledButtonCyan("Join Hive");
        exitButton = new StyledButtonCyan("Exit");

        // Szövegbeviteli mezők és gombok  
        hiveNameField = new JTextField(15);
        hiveNameField.setPreferredSize(new Dimension(250, 40)); // Méret beállítása  
        hiveNameField.setFont(new Font("Arial", Font.PLAIN, 18)); // Betűtípus beállítása  
        hiveNameField.setBackground(new Color(255, 255, 255, 200)); // Áttetsző szürke háttér

        invitationCodeField = new JTextField(15);
        invitationCodeField.setPreferredSize(new Dimension(250, 40)); // Méret beállítása  
        invitationCodeField.setFont(new Font("Arial", Font.PLAIN, 18)); // Betűtípus beállítása  
        invitationCodeField.setBackground(new Color(255, 255, 255, 200)); // Áttetsző szürke háttér

        submitButton = new StyledButtonCyan("Submit");
        backButton = new StyledButtonCyan("Back");

        // Gombok eseménykezelői  
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

            // Vissza a HIVE kezelő panelhez  
            isCreatingHive = false; // Beállítjuk, hogy ne a HIVE létrehozása legyen aktív  
            removeAll(); // Minden elemet eltávolítunk a panelről  
            revalidate(); // Frissítjük a panelt  
            repaint(); // Újrarajzoljuk a panelt  

            // Hozzáadjuk az eredeti gombokat            
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(10, 10, 10, 10); // Margó  

            gbc.gridx = 0;
            gbc.gridy = 0; // Gomb 1  
            add(createHiveButton, gbc);
            gbc.gridx = 0;
            gbc.gridy = 1; // Gomb 2  
            add(joinHiveButton, gbc);
            gbc.gridx = 0;
            gbc.gridy = 2; // Gomb 3  
            add(exitButton, gbc);

            // Frissítjük a panelt  
            revalidate();
            repaint();
        });
        exitButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Exiting...", "Exit", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0); // Kilépés a programból  
        });

        // Gombok elhelyezése a panelen  
        gbc.gridx = 0;
        gbc.gridy = 0; // Gomb 1  
        add(createHiveButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1; // Gomb 2  
        add(joinHiveButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2; // Gomb 3  
        add(exitButton, gbc);
    }

    public void setUser(User user) {
        this.user = user;       
    }

    private void showCreateHiveInput(GridBagConstraints gbc) {
        isCreatingHive = true; // Jelzi, hogy a HIVE létrehozását szeretnénk  

        removeAll(); // Minden elemet eltávolít  
        revalidate(); // Panel frissítése  
        repaint();

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel hiveNameLabel = new JLabel("Enter the Hive's name: ");
        hiveNameLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Betűtípus beállítása         
        hiveNameLabel.setOpaque(true); // Az átlátszóság engedélyezése  
        hiveNameLabel.setBackground(new Color(255, 255, 255, 50)); // Háttérszín beállítása (világoskék)  
        hiveNameLabel.setPreferredSize(new Dimension(250, 30)); // Méret beállítása, hogy jól nézzen ki        
        hiveNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(hiveNameLabel, gbc); // Hozzáadás a nézethez

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(hiveNameField, gbc);

        // Középre igazított elemek hozzáadása
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(submitButton, gbc);    // Submit gomb

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(backButton, gbc);

        // Frissítések  
        revalidate();
        repaint();
    }

    private void showJoinHiveInput(GridBagConstraints gbc) {
        isCreatingHive = false; // Jelzi, hogy a HIVE-hoz csatlakozást szeretnénk  

        removeAll(); // Minden elemet eltávolít  
        revalidate(); // Panel frissítése  
        repaint();

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel invitationCodeLabel = new JLabel("Enter the invitation code: ");
        invitationCodeLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Betűtípus beállítása         
        invitationCodeLabel.setOpaque(true); // Az átlátszóság engedélyezése  
        invitationCodeLabel.setBackground(new Color(255, 255, 255, 50)); // Háttérszín beállítása (világoskék)  
        invitationCodeLabel.setPreferredSize(new Dimension(250, 30)); // Méret beállítása, hogy jól nézzen ki        
        invitationCodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(invitationCodeLabel, gbc);

        // Középre igazított elemek hozzáadása  
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(invitationCodeField, gbc); // Meghívó kód mező

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(submitButton, gbc); // Submit gomb

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(backButton, gbc);

        // Frissítések  
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
            // Generate a unique ID for the hive
            String hiveId = UUID.randomUUID().toString();
            String leaderName = user.getUsername(); // Get the leader's name

            // Create a new Hive object without members for the insert operation
            Hive newHive = new Hive(hiveId, hiveName, leaderName, null); // Members are not included

            // Insert the new hive into the Supabase database
            supabase.insertHive(newHive); // Assuming you have a method to insert the hive

            // Update the hive table to add the leader as a member
            supabase.addHiveMember(hiveId, user.getUserId()); // Assuming you have a method to add a member to the hive

            // Set the user's hive ID and mark them as a leader
            user.setHiveId(hiveId);
            user.setLeader(true);
            
            supabase.updateUserHiveInfo(user.getUserId(), user.getHiveId(), user.isLeader()); // Update user info in Supabase

            // Helyi gyorsítótár frissítése: lekérjük az aktuális HIVE adatait a Supabase-ból
            Map<String, Object> hiveData = supabase.getHiveData(hiveId, user.getUserId(), perkStorage);
            if (hiveData != null) {
                mainFrame.setCachedHiveData(hiveData);
            } else {
                // Set default values if hiveData is null
                mainFrame.setCachedHiveData(Map.of(
                        "hiveName", hiveName,
                        "members", List.of(user),
                        "memberPerks", Map.of(user.getUsername(), new ArrayList<Perk>())
                ));
            }

            CustomDialog.showInfo("Hive successfully created: " + hiveName);
            mainFrame.updateCachedHiveMembers();
            mainFrame.showPanel("LeaderMenuPanel"); // Switch to the leader panel

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
            // Step 1: Check the invite code in the invite_codes table
            String inviteCodeEndpoint = "/rest/v1/invite_codes?invite_code=eq." + inviteCode + "&select=hive_id";
            String inviteCodeResponse = supabase.get(inviteCodeEndpoint);
            JSONArray inviteCodeArray = new JSONArray(inviteCodeResponse);

            if (inviteCodeArray.length() == 0) {
                CustomDialog.showError("Invalid invitation code!");
                return;
            }

            // Step 2: Retrieve the associated hive_id
            String hiveId = inviteCodeArray.getJSONObject(0).optString("hive_id", null);
            if (hiveId == null) {
                CustomDialog.showError("Invalid invitation code!");
                return;
            }

            // Step 3: Check if the user is already a member of a HIVE
            if (user.getHiveId() != null && !user.getHiveId().trim().isEmpty()) {
                CustomDialog.showError("You are already a member of another Hive! You must leave it first.");
                return;
            }

            // Step 4: Check the current membership count in the hives table
            String membersEndpoint = "/rest/v1/hives?id=eq." + hiveId + "&select=hive_members"; // Use the correct field name
            String membersResponse = supabase.get(membersEndpoint);
            
            JSONArray membersArray = new JSONArray(membersResponse);

            // Check if the response is valid and contains the hive_members field
            if (membersArray.length() == 0) {
                CustomDialog.showError("Hive not found.");
                return;
            }

            // Get the current hive_members array
            JSONArray currentMembersArray = membersArray.getJSONObject(0).optJSONArray("hive_members");
            if (currentMembersArray == null) {
                currentMembersArray = new JSONArray(); // Initialize if null
            }

            // Check the membership limit
            if (currentMembersArray.length() >= 16) {
                CustomDialog.showError("This Hive has already reached its maximum membership (16 people).");
                return;
            }

            // Step 5: Update the user's hive_id in the users table (patch)
            supabase.updateUserHiveInfo(user.getUserId(), hiveId);

            // Step 6: Add the user to the hive_members array in the hives table
            supabase.addHiveMember(hiveId, user.getUserId());

            // Step 7: Update the local user object
            user.setHiveId(hiveId);

            // Step 8: Retrieve the current HIVE data from Supabase and update the cache
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
