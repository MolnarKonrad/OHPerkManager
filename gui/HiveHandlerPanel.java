package gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import models.Hive;
import models.Perk;
import models.User;
import services.FirebaseService;
import services.PerkStorage;
import utils.SupabaseClient;

public class HiveHandlerPanel extends JPanelWithBackground {

    private JTextField hiveNameField;
    private JTextField invitationCodeField;
    private StyledButton createHiveButton;
    private StyledButton joinHiveButton;
    private StyledButton submitButton;
    private StyledButton backButton;
    private StyledButton exitButton;
    private MainFrame mainFrame;
    private SupabaseClient db;  // Firestore példány
    private FirebaseService firebaseService;
    private User user; // A felhasználó, aki a HIVE-ot kezeli    
    private boolean isCreatingHive = false; // Nyomkövető a HIVE létrehozásához  

    public HiveHandlerPanel(FirebaseService firebaseService, PerkStorage perkStorage, MainFrame mainFrame, SupabaseClient db, User user) {
        this.firebaseService = firebaseService;
        this.mainFrame = mainFrame;
        this.db = db; // Firestore inicializálás  
        this.user = user; // A felhasználó beállítása  
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10); // Margó  

        createHiveButton = new StyledButton("Create Hive");
        joinHiveButton = new StyledButton("Join Hive");
        exitButton = new StyledButton("Exit");

        // Szövegbeviteli mezők és gombok  
        hiveNameField = new JTextField(15);
        hiveNameField.setPreferredSize(new Dimension(250, 40)); // Méret beállítása  
        hiveNameField.setFont(new Font("Times New Roman", Font.PLAIN, 18)); // Betűtípus beállítása  
        hiveNameField.setBackground(new Color(255, 255, 255, 200)); // Áttetsző szürke háttér

        invitationCodeField = new JTextField(15);
        invitationCodeField.setPreferredSize(new Dimension(250, 40)); // Méret beállítása  
        invitationCodeField.setFont(new Font("Times New Roman", Font.PLAIN, 18)); // Betűtípus beállítása  
        invitationCodeField.setBackground(new Color(255, 255, 255, 200)); // Áttetsző szürke háttér

        submitButton = new StyledButton("Submit");
        backButton = new StyledButton("Back");

        // Gombok eseménykezelői  
        createHiveButton.addActionListener(e -> showCreateHiveInput(gbc));
        joinHiveButton.addActionListener(e -> showJoinHiveInput(gbc));
        submitButton.addActionListener(e -> handleSubmit(perkStorage));
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
        System.out.println("HiveHandlerPanel user set: " + (user != null ? user.getUsername() : "NULL"));
    }

    private void showCreateHiveInput(GridBagConstraints gbc) {
        isCreatingHive = true; // Jelzi, hogy a HIVE létrehozását szeretnénk  

        removeAll(); // Minden elemet eltávolít  
        revalidate(); // Panel frissítése  
        repaint();

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel hiveNameLabel = new JLabel("Enter the HIVE's name: ");
        hiveNameLabel.setFont(new Font("Times New Roman", Font.BOLD, 18)); // Betűtípus beállítása         
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
        invitationCodeLabel.setFont(new Font("Times New Roman", Font.BOLD, 18)); // Betűtípus beállítása         
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

    private void handleSubmit(PerkStorage perkStorage) {
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
            // HIVE létrehozása a Firestore-ban
            String hiveId = db.collection("hives").document().getId();
            Hive newHive = new Hive(hiveId, hiveName, user.getUsername(), List.of(user.getUserId()));
            db.collection("hives").document(hiveId).set(newHive).get();

            // A felhasználó beállítása, mint a HIVE vezetője
            user.setHiveId(hiveId);
            user.setLeader(true);
            db.collection("users").document(user.getUserId()).update("hiveId", hiveId, "isLeader", true).get();

            // Helyi gyorsítótár frissítése: lekérjük az aktuális HIVE adatait a Firestoreból
            Map<String, Object> hiveData = firebaseService.getHiveData(hiveId, user.getUserId(), perkStorage);
            if (hiveData != null) {
                mainFrame.setCachedHiveData(hiveData);
            } else {
                // Ha nem sikerül lekérdezni, akkor alapértelmezett értékeket állítunk be
                mainFrame.setCachedHiveData(Map.of(
                        "hiveName", hiveName,
                        "members", List.of(user),
                        "memberPerks", Map.of(user.getUsername(), new ArrayList<Perk>())
                ));
            }

            CustomDialog.showInfo("HIVE successfully created: " + hiveName);
            mainFrame.updateCachedHiveMembers();
            mainFrame.showPanel("LeaderMenuPanel"); // Váltás a vezetői panelre

        } catch (ExecutionException | InterruptedException e) {
            showError(e);
        }
    }

    private void joinHive(String inviteCode, PerkStorage perkStorage) {
        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            CustomDialog.showError("Please enter a valid invitation code.");
            return;
        }

        try {
            Query query = db.collection("hives").whereEqualTo("inviteCode", inviteCode);
            QuerySnapshot querySnapshot = query.get().get();

            if (querySnapshot.isEmpty()) {
                CustomDialog.showError("Invalid invitation code!");
                return;
            }

            DocumentSnapshot hiveDoc = querySnapshot.getDocuments().get(0);
            String hiveId = hiveDoc.getId();
            String hiveName = hiveDoc.getString("name");
            List<String> members = (List<String>) hiveDoc.get("members");
            if (members == null) {
                members = new ArrayList<>();
            }

            if (members.size() >= 16) {
                CustomDialog.showError("This HIVE has already reached its maximum membership (16 people).");
                return;
            }

            if (user.getHiveId() != null && !user.getHiveId().isEmpty()) {
                CustomDialog.showError("You are already a member of another HIVE! You must leave it first.");
                return;
            }

            // Hozzáadjuk a felhasználót a HIVE taglistájához Firestore-ban
            members.add(user.getUserId());
            db.collection("hives").document(hiveId).update("members", members).get();
            user.setHiveId(hiveId);
            db.collection("users").document(user.getUserId()).update("hiveId", hiveId).get();

            // Helyi gyorsítótár frissítése a Firestore-ból lekért HIVE adatokkal
            Map<String, Object> hiveData = firebaseService.getHiveData(hiveId, user.getUserId(), perkStorage);
            if (hiveData != null) {
                mainFrame.setCachedHiveData(hiveData);
            } else {
                mainFrame.setCachedHiveData(Map.of(
                        "hiveName", hiveName,
                        "members", List.of(user),
                        "memberPerks", Map.of(user.getUsername(), new ArrayList<Perk>())
                ));
            }

            CustomDialog.showInfo("You have successfully joined HIVE: " + hiveName);
            mainFrame.showPanel("MemberMenuPanel"); // Váltás a tag panelre

        } catch (ExecutionException | InterruptedException e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        CustomDialog.showError("Error: " + e.getMessage());
    }
}
