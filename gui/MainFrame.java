package gui;

import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import models.Perk;
import models.User;
import services.FirebaseService;
import services.PerkStorage;
import utils.SupabaseClient;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private SupabaseClient supabase;
    private FirebaseService firebaseService;
    private PerkStorage perkStorage;
    private User user;
    private String cachedHiveName;
    private List<User> cachedHiveMembers;
    private Map<String, List<Perk>> cachedMemberPerks;

    public String getCachedHiveName() {
        return cachedHiveName;
    }

    public PerkStorage getPerkStorage() {
        return perkStorage;
    }

    public List<User> getCachedHiveMembers() {
        return cachedHiveMembers;
    }

    public Map<String, List<Perk>> getCachedMemberPerks() {
        return cachedMemberPerks;
    }

    public void setCachedHiveData(Map<String, Object> hiveData) {
        if (hiveData != null) {
            this.cachedHiveName = (String) hiveData.get("hiveName");
            this.cachedHiveMembers = (List<User>) hiveData.get("members");
            this.cachedMemberPerks = (Map<String, List<Perk>>) hiveData.get("memberPerks");
        } else {
            this.cachedHiveName = "No Hive";
            this.cachedHiveMembers = new ArrayList<>();
            this.cachedMemberPerks = new HashMap<>();
        }
    }

    public void setCachedHiveName(String hiveName) {
        this.cachedHiveName = hiveName;
    }

    public void setCachedHiveMembers(List<User> members) {
        this.cachedHiveMembers = new ArrayList<>(members);
    }

    public void setCachedMemberPerks(Map<String, List<Perk>> memberPerks) {
        this.cachedMemberPerks = new HashMap<>(memberPerks);
    }

    public MainFrame(SupabaseClient supabase) {
        this.supabase = supabase;
        this.perkStorage = new PerkStorage();
        ImageIcon icon = new ImageIcon(getClass().getResource("/PM_icon.png"));
        setIconImage(icon.getImage());
        setTitle("Perk Manager for Once Human HIVEs");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);

        // Panelek létrehozása
        AuthPanel authPanel = new AuthPanel(supabase, this);
        LoginPanel loginPanel = new LoginPanel(this, authPanel);
        RegistrationPanel registrationPanel = new RegistrationPanel(supabase, this, authPanel);

        // Panelek hozzáadása a CardLayout-hoz
        mainPanel.add(authPanel, "AuthPanel");
        mainPanel.add(loginPanel, "LoginPanel");
        mainPanel.add(registrationPanel, "RegistrationPanel");

        // A hitelesítési panel megjelenítése
        cardLayout.show(mainPanel, "AuthPanel");

        setVisible(true);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) throws ExecutionException, InterruptedException {
        this.user = user;
        this.firebaseService = new FirebaseService(supabase, new PerkStorage());

        // 🔹 HIVE adatainak lekérése
        firebaseService.getHiveData(user.getHiveId(), user.getUserId(), perkStorage)
                .thenAccept(hiveData -> {
                    // Check if hiveData is not null before updating the cache            
                    if (hiveData != null) {
                        // Frissítjük a cache-t
                        setCachedHiveData(hiveData);
                    }

                    Runnable onBack = () -> {
                        String panelToShow = user.isLeader() ? "LeaderMenuPanel" : "MemberMenuPanel";
                        showPanel(panelToShow);
                    };

                    HiveHandlerPanel hiveHandlerPanel = new HiveHandlerPanel(firebaseService, perkStorage, this, supabase, user);
                    LeaderMenuPanel leaderMenuPanel = new LeaderMenuPanel(this, firebaseService, perkStorage, user);
                    MemberMenuPanel memberMenuPanel = new MemberMenuPanel(this, firebaseService, perkStorage, user);
                    HiveInfoPanel hiveInfoPanel = new HiveInfoPanel(this, cachedHiveName, cachedHiveMembers, cachedMemberPerks, perkStorage, onBack);

                    mainPanel.add(hiveHandlerPanel, "HiveHandlerPanel");
                    mainPanel.add(leaderMenuPanel, "LeaderMenuPanel");
                    mainPanel.add(memberMenuPanel, "MemberMenuPanel");
                    mainPanel.add(hiveInfoPanel, "HiveInfoPanel");
                })
                .exceptionally(e -> {
                    // Handle any exceptions that occur during the data retrieval
                    CustomDialog.showError("Failed to retrieve hive data: " + e.getMessage());
                    return null;
                });
    }

    public void updateCachedHiveMembers() {

        try {
            // Update the members' data
            firebaseService.getHiveData(user.getHiveId(), user.getUserId(), perkStorage)
                    .thenAccept(hiveData -> {
                        if (hiveData != null) {
                            setCachedHiveData(hiveData);
                        }
                    })
                    .exceptionally(e -> {
                        CustomDialog.showError("Failed to update cached Hive members: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            CustomDialog.showError("Failed to update cached Hive members: " + e.getMessage());
        }
    }

    // Panel váltás
    public void showPanel(String panelName) {
        if (panelName == null || panelName.isEmpty()) {
            CustomDialog.showError("Error: panelName is null or empty");
            return;
        }
        try {
            SwingUtilities.invokeLater(() -> {
                cardLayout.show(mainPanel, panelName);
                mainPanel.revalidate();
                mainPanel.repaint();
            });
        } catch (Exception e) {
            CustomDialog.showError("Error while trying to show panel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public CardLayout getCardLayout() {
        return cardLayout;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
