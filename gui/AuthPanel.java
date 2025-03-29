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
import models.User;
import org.mindrot.jbcrypt.BCrypt;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.SupportUtils;
import utils.SupabaseClient;

public class AuthPanel extends JPanelWithBackground {

    private StyledButton loginButton;
    private StyledButton registerButton;
    private final SupabaseClient supabase;

    // Konstruktor: A SupabaseClient példányt adod át, hasonlóan ahogy korábban a Firestore kapcsolatot adtad át
    public AuthPanel(SupabaseClient db, MainFrame mainFrame) {
        this.supabase = db;
        setLayout(new BorderLayout()); // Rugalmasabb elrendezés  

        // ───── Alap tartalmi panel ─────
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false); // Átlátszó háttér, hogy illeszkedjen a háttérhez
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20); // Párnázás  

        // Gombok létrehozása  
        loginButton = new StyledButton("Login");
        loginButton.setPreferredSize(new Dimension(300, 50));
        registerButton = new StyledButton("Register");
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

        StyledButton supportButton = new StyledButton("Support");
        supportButton.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        supportButton.setPreferredSize(new Dimension(120, 40)); // Gomb mérete
        supportButton.addActionListener(e -> SupportUtils.openPatreonLink());

        // Support gombot jobb alsó sarokba helyezzük
        bottomPanel.add(supportButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // authenticateUser metódus, amely a Supabase adatbázisból kérdezi le a felhasználót
    public User authenticateUser(String username, String password) throws ExecutionException, InterruptedException {
        try {
            // Supabase REST API hívás a "users" táblára, szűrve a username alapján
            String endpoint = "/rest/v1/users?username=eq." + username + "&select=*";
            String response = supabase.get(endpoint);
            JSONArray queryResult = new JSONArray(response);

            if (queryResult.length() == 0) {
                CustomDialog.showError("No user found with the username: " + username);
                return null;
            }

            JSONObject userObj = queryResult.getJSONObject(0);
            String storedPassword = userObj.optString("password", null);

            // Jelszó ellenőrzése  
            if (storedPassword != null && checkPassword(password, storedPassword)) {
                boolean isLeader = userObj.optBoolean("isLeader", false);
                String id = userObj.optString("id", "");
                String uname = userObj.optString("username", "");
                String hiveId = userObj.optString("hiveId", "");
                List<String> perks = new ArrayList<>();
                JSONArray perksArray = userObj.optJSONArray("perks");
                if (perksArray != null) {
                    for (int i = 0; i < perksArray.length(); i++) {
                        perks.add(perksArray.getString(i));
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
