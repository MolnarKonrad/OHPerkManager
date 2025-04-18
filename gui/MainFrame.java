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
import services.PerkStorage;
import utils.SupabaseClient;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private SupabaseClient supabase;
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

    public Map<String, Object> getCachedHiveData() {
        Map<String, Object> data = new HashMap<>();
        data.put("hiveName", cachedHiveName);
        data.put("members", cachedHiveMembers);
        data.put("memberPerks", cachedMemberPerks);
        return data;
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
        setTitle("Perk Manager for Once Human Hives");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);

        // Panelek létrehozása
        AuthPanel authPanel = new AuthPanel(supabase, perkStorage, this);
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
        
        // Retrieve hive data
        Map<String, Object> hiveData = supabase.getHiveData(user.getHiveId(), user.getUserId(), perkStorage); // Synchronous call       

        // Check if hiveData is not null before updating the cache            
        if (hiveData != null) {
            // Update the cache
            setCachedHiveData(hiveData);
        }        

        // Create panels
        HiveHandlerPanel hiveHandlerPanel = new HiveHandlerPanel(perkStorage, this, supabase, user);
        LeaderMenuPanel leaderMenuPanel = new LeaderMenuPanel(this, supabase, perkStorage, user);
        MemberMenuPanel memberMenuPanel = new MemberMenuPanel(this, supabase, perkStorage, user);
        HiveInfoPanel hiveInfoPanel = new HiveInfoPanel(this, cachedHiveName, cachedHiveMembers, cachedMemberPerks, perkStorage, () -> {
            String panelToShow = user.isLeader() ? "LeaderMenuPanel" : "MemberMenuPanel";
            showPanel(panelToShow);
        });

        // Add panels to mainPanel
        mainPanel.add(hiveHandlerPanel, "HiveHandlerPanel");
        mainPanel.add(leaderMenuPanel, "LeaderMenuPanel");
        mainPanel.add(memberMenuPanel, "MemberMenuPanel");
        mainPanel.add(hiveInfoPanel, "HiveInfoPanel");

        // Switch to the appropriate panel based on the user's hive ID
        String panelToShow = user.getHiveId().isEmpty() ? "HiveHandlerPanel" : (user.isLeader() ? "LeaderMenuPanel" : "MemberMenuPanel");
        showPanel(panelToShow);
    }

    public void updateCachedHiveMembers() {
        try {
            // Update the members' data
            Map<String, Object> hiveData = supabase.getHiveData(user.getHiveId(), user.getUserId(), perkStorage); // Synchronous call

            if (hiveData != null) {
                setCachedHiveData(hiveData);
            }
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
