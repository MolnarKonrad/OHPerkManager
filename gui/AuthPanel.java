package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import models.Perk;
import models.User;
import org.mindrot.jbcrypt.BCrypt;
import org.json.JSONArray;
import org.json.JSONObject;
import services.PerkStorage;
import utils.SupportUtils;
import utils.SupabaseClient;

public class AuthPanel extends JPanelWithBackground {

    private StyledButtonCyan loginButton;
    private StyledButtonCyan registerButton;
    private final SupabaseClient supabase;
    private final PerkStorage perkStorage;

    // Konstruktor: A SupabaseClient példányt adod át, hasonlóan ahogy korábban a Firestore kapcsolatot adtad át
    public AuthPanel(SupabaseClient supabase, PerkStorage perkStorage, MainFrame mainFrame) {
        this.supabase = supabase;
        this.perkStorage = perkStorage;
        setLayout(new BorderLayout()); // Rugalmasabb elrendezés  

        // ───── Alap tartalmi panel ─────
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false); // Átlátszó háttér, hogy illeszkedjen a háttérhez
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20); // Párnázás  

        // Gombok létrehozása  
        loginButton = new StyledButtonCyan("Login");
        loginButton.setPreferredSize(new Dimension(300, 50));
        registerButton = new StyledButtonCyan("Register");
        registerButton.setPreferredSize(new Dimension(300, 50));

        // Eseménykezelők  
        loginButton.addActionListener(e -> mainFrame.showPanel("LoginPanel"));
        registerButton.addActionListener(e -> mainFrame.showPanel("RegistrationPanel"));

        // Elek elhelyezése  
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(loginButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(registerButton, gbc);

        add(contentPanel, BorderLayout.CENTER); // Tartalmat középre helyezzük

        // ───── Support gomb a jobb alsó sarokban ─────
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false); // Átlátszó háttér

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

    // authenticateUser metódus, amely a Supabase adatbázisból kérdezi le a felhasználót
    public User authenticateUser(String username, String password) throws ExecutionException, InterruptedException {
        try {
            // Supabase REST API call to the "users" table, filtered by username
            String endpoint = "/rest/v1/users?username=eq." + username + "&select=*";
            String response = supabase.get(endpoint);
            JSONArray queryResult = new JSONArray(response);

            if (queryResult.length() == 0) {
                CustomDialog.showError("No user found with the username: " + username);
                return null;
            }

            JSONObject userObj = queryResult.getJSONObject(0);
            String storedPassword = userObj.optString("password", null);

            // Password verification  
            if (storedPassword != null && checkPassword(password, storedPassword)) {
                boolean isLeader = userObj.optBoolean("is_leader", false);
                String id = userObj.optString("id", "");
                String uname = userObj.optString("username", "");
                String hiveId = userObj.optString("hive_id", ""); // Ensure the key matches the API response
                List<Perk> perks = new ArrayList<>();
                JSONArray perksArray = userObj.optJSONArray("perks");
                if (perksArray != null) {
                    for (int i = 0; i < perksArray.length(); i++) {
                        String perkName = perksArray.getString(i); // Get the perk name
                        Perk perkDetails = perkStorage.getPerkByName(perkName); // Retrieve the full perk details
                        if (perkDetails != null) {
                            perks.add(perkDetails); // Add the full perk object to the list
                        }
                    }
                }
                
                return new User(id, uname, hiveId, isLeader, perks);
            } else {
                CustomDialog.showError("Password does not match for username: " + username);
                return null;
            }
        } catch (Exception e) {
            CustomDialog.showError("Error during authentication: " + e.getMessage());
            return null;
        }
    }

    private boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
}
