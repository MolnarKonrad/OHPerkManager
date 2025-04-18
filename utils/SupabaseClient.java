package utils;

import gui.CustomConfirmationPopup;
import gui.CustomDialog;
import gui.MainFrame;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import models.Hive;
import models.Perk;
import models.User;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import services.PerkStorage;

public class SupabaseClient {

    private String url;
    private String apiKey;
    private HttpClient httpClient;

    /**
     * Konstruktor, amely beállítja a Supabase URL-t, API kulcsot és
     * inicializálja a HttpClient-et.
     *
     * @param url A Supabase projekt URL-je, pl.
     * "https://your-project.supabase.co"
     * @param apiKey Az anon vagy service API kulcs
     */
    public SupabaseClient(String url, String apiKey) {
        this.url = url;
        this.apiKey = apiKey;
        this.httpClient = HttpClients.createDefault();
    }

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }

    /**
     * Végrehajt egy GET kérést a Supabase REST API felé az adott endpoint-tal.A
     * endpoint a Supabase URL-hez viszonyított útvonal, pl.
     * "/rest/v1/users?username=eq.someUser&select=*"
     *
     * @param endpoint A lekérdezendő endpoint
     * @return A válasz JSON szövegként
     * @throws IOException Ha hálózati hiba történik
     */
    public String get(String endpoint) throws IOException {
        String fullUrl = url + endpoint;
        HttpGet httpGet = new HttpGet(fullUrl);
        httpGet.setHeader("apikey", apiKey);
        httpGet.setHeader("Authorization", "Bearer " + apiKey);
        httpGet.setHeader("Accept", "application/json");

        try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : "";
        }
    }

    public String patch(String endpoint, String jsonPayload) throws IOException {
        String fullUrl = url + endpoint;
        HttpPatch httpPatch = new HttpPatch(fullUrl);
        httpPatch.setHeader("apikey", apiKey);
        httpPatch.setHeader("Authorization", "Bearer " + apiKey);
        httpPatch.setHeader("Content-Type", "application/json");
        httpPatch.setHeader("Accept", "application/json");
        httpPatch.setEntity(new StringEntity(jsonPayload));

        try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(httpPatch)) {
            HttpEntity entity = response.getEntity();
            String responseBody = entity != null ? EntityUtils.toString(entity) : "";
            if (response.getStatusLine().getStatusCode() != 200
                    && response.getStatusLine().getStatusCode() != 204) {
                throw new IOException("Failed to update resource: " + responseBody);
            }
            return responseBody;
        }
    }

    public String delete(String endpoint) throws IOException {
        String fullUrl = url + endpoint;
        HttpDelete httpDelete = new HttpDelete(fullUrl);
        httpDelete.setHeader("apikey", apiKey);
        httpDelete.setHeader("Authorization", "Bearer " + apiKey);
        httpDelete.setHeader("Accept", "application/json");

        try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(httpDelete)) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : "";
        }
    }

    public String post(String endpoint, String jsonPayload) throws IOException {
        String fullUrl = url + endpoint;
        HttpPost httpPost = new HttpPost(fullUrl);
        httpPost.setHeader("apikey", apiKey);
        httpPost.setHeader("Authorization", "Bearer " + apiKey);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(jsonPayload));

        try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String responseBody = entity != null ? EntityUtils.toString(entity) : "";
            if (response.getStatusLine().getStatusCode() != 200
                    && response.getStatusLine().getStatusCode() != 201
                    && response.getStatusLine().getStatusCode() != 204) {
                throw new IOException("Failed to post resource: " + responseBody);
            }
            return responseBody;
        }
    }

    /**
     * Beszúr egy rekordot a megadott táblába a Supabase REST API segítségével.A
     * data map-et JSON objektummá alakítja, és POST kérést küld a Supabase
     * endpointjára.
     *
     * @param data A beszúrandó adatokat tartalmazó Map (kulcs: oszlopnév,
     * érték: érték)
     * @return Egy CompletableFuture, amely true értéket ad vissza, ha a
     * beszúrás sikeres (HTTP státuszkód 201), különben false.
     */
    public CompletableFuture<Boolean> insertUser(Map<String, Object> data) {
        JSONObject json = new JSONObject(data);
        String endpoint = "/rest/v1/users";
        return CompletableFuture.supplyAsync(() -> {
            try {
                post(endpoint, json.toString());
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }

    public void insertHive(Hive hive) throws IOException, InterruptedException {
        String endpoint = "/rest/v1/hives";
        String json = String.format("{\"id\": \"%s\", \"name\": \"%s\", \"leader_name\": \"%s\"}",
                hive.getId(), hive.getName(), hive.getLeaderName());
        post(endpoint, json);
    }

    public void addHiveMember(String hiveId, String userId) throws IOException, InterruptedException, Exception {
        // Step 1: Retrieve the username of the user to be added
        String userEndpoint = "/rest/v1/users?id=eq." + userId + "&select=username";
        String userResponse = get(userEndpoint);
        JSONArray userArray = new JSONArray(userResponse);

        if (userArray.length() == 0) {
            throw new Exception("User not found.");
        }

        String username = userArray.getJSONObject(0).optString("username", null);
        if (username == null) {
            throw new Exception("Username not found for user ID: " + userId);
        }

        // Step 2: Retrieve the current hive_members array
        String hiveEndpoint = "/rest/v1/hives?id=eq." + hiveId + "&select=hive_members";
        String hiveResponse = get(hiveEndpoint);
        JSONArray hiveArray = new JSONArray(hiveResponse);

        if (hiveArray.length() == 0) {
            throw new Exception("Hive not found.");
        }

        // Get the current hive_members array
        JSONArray currentMembersArray = hiveArray.getJSONObject(0).optJSONArray("hive_members");
        if (currentMembersArray == null) {
            currentMembersArray = new JSONArray(); // Initialize if null
        }

        // Step 3: Append the new username to the hive_members array
        currentMembersArray.put(username);

        // Step 4: Create the JSON payload for the update operation
        String json = String.format("{\"hive_members\": %s}", currentMembersArray.toString());

        // Use the generic patch method instead of manually building the request:
        String patchEndpoint = "/rest/v1/hives?id=eq." + hiveId;
        patch(patchEndpoint, json);
    }

    public void updateUserHiveInfo(String userId, String hiveId) throws IOException, InterruptedException {
        updateUserHiveInfo(userId, hiveId, false);
    }

    public void updateUserHiveInfo(String userId, String hiveId, boolean isLeader) throws IOException, InterruptedException {
        if (!isValidUUID(userId)) {
            throw new IllegalArgumentException("Invalid UUID format for userId: " + userId);
        }
        if (!isValidUUID(hiveId)) {
            throw new IllegalArgumentException("Invalid UUID format for hiveId: " + hiveId);
        }
        String endpoint = "/rest/v1/users?id=eq." + userId;
        String json = String.format("{\"hive_id\": \"%s\", \"is_leader\": %b}", hiveId, isLeader);
        patch(endpoint, json);
    }

    // Helper method to validate UUID format
    private boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public Map<String, Object> getHiveData(String hiveId, String userId, PerkStorage perkStorage) {
        Map<String, Object> hiveData = new HashMap<>();

        try {
            // Step 1: Lekérjük a felhasználó adatait a "users" táblából
            String userEndpoint = "/rest/v1/users?id=eq." + userId + "&select=username,hive_id,is_leader,perks";
            String userResponse = get(userEndpoint);
            JSONArray userArray = new JSONArray(userResponse);
            if (userArray.length() == 0) {
                throw new Exception("User not found.");
            }
            JSONObject userObj = userArray.getJSONObject(0);
            String username = userObj.optString("username", null);
            boolean isLeader = userObj.optBoolean("is_leader", false);
            String currentUserHiveId = userObj.optString("hive_id", null);
            JSONArray perksArray = userObj.optJSONArray("perks");

            // Létrehozzuk a felhasználó objektumot (kezdetben üres perk lista)
            List<Perk> emptyPerks = new ArrayList<>();
            User currentUser = new User(userId, username, currentUserHiveId, isLeader, emptyPerks);
            hiveData.put("currentUser", currentUser);

            // Step 2: Lekérjük a HIVE nevét és tagjait, ha a felhasználónak van hive_id-je
            String hiveName = null;
            List<String> hiveMembers = new ArrayList<>();
            if (currentUserHiveId != null && !currentUserHiveId.isEmpty()) {
                String hiveEndpoint = "/rest/v1/hives?id=eq." + currentUserHiveId + "&select=name,hive_members";
                String hiveResponse = get(hiveEndpoint);
                JSONArray hiveArray = new JSONArray(hiveResponse);
                if (hiveArray.length() > 0) {
                    JSONObject hiveObj = hiveArray.getJSONObject(0);
                    hiveName = hiveObj.optString("name", "Unknown Hive");
                    if (hiveObj.has("hive_members")) {
                        JSONArray membersArray = hiveObj.optJSONArray("hive_members");
                        for (int i = 0; i < membersArray.length(); i++) {
                            hiveMembers.add(membersArray.getString(i));
                        }
                    }
                }
            }
            hiveData.put("hiveName", hiveName);

            // Step 3: Létrehozzuk a tagok User objektumait
            List<User> members = new ArrayList<>();
            for (String memberUsername : hiveMembers) {
                String memberEndpoint = "/rest/v1/users?username=eq." + memberUsername + "&select=id,username,hive_id,is_leader,perks";
                String memberResponse = get(memberEndpoint);
                JSONArray memberArray = new JSONArray(memberResponse);
                if (memberArray.length() > 0) {
                    JSONObject memberData = memberArray.getJSONObject(0);
                    String memberUserId = memberData.optString("id", null);
                    boolean memberIsLeader = memberData.optBoolean("is_leader", false);
                    String memberHiveId = memberData.optString("hive_id", null);
                    JSONArray memberPerksArray = memberData.optJSONArray("perks");
                    // Kezdetben üres perk lista, majd frissítjük később
                    User member = new User(memberUserId, memberUsername, memberHiveId, memberIsLeader, new ArrayList<>());
                    List<Perk> memberPerks = new ArrayList<>();
                    if (memberPerksArray != null) {
                        for (int j = 0; j < memberPerksArray.length(); j++) {
                            String perkName = memberPerksArray.getString(j);
                            Perk perkDetails = perkStorage.getPerkByName(perkName);
                            if (perkDetails != null) {
                                memberPerks.add(perkDetails);
                            }
                        }
                    }
                    member.setPerks(memberPerks);
                    members.add(member);
                }
            }
            hiveData.put("members", members);

            // Step 4: Lekérjük a perk-eket a jelenlegi felhasználó számára
            List<Perk> currentUserPerks = new ArrayList<>();
            if (perksArray != null) {
                for (int i = 0; i < perksArray.length(); i++) {
                    String perkName = perksArray.getString(i);
                    Perk perkDetails = perkStorage.getPerkByName(perkName);
                    if (perkDetails != null) {
                        currentUserPerks.add(perkDetails);
                    }
                }
            }
            currentUser.setPerks(currentUserPerks);

            // Step 5: Létrehozunk egy map-et, amely a tagok perk adatait tartalmazza (kulcs: tag username, érték: perk lista)
            Map<String, List<Perk>> memberPerksMap = new HashMap<>();
            for (User member : members) {
                memberPerksMap.put(member.getUsername(), member.getPerks());
            }
            hiveData.put("memberPerks", memberPerksMap);

        } catch (Exception e) {
            CustomDialog.showError("Error retrieving hive data: " + e.getMessage());
        }
        return hiveData;
    }

    public void generateNewInviteCode(String hiveId) throws IOException, InterruptedException {
        // Generate a new invite code (6 character alphanumeric code)
        String newInviteCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Step 1: Check if the hive_id exists in the invite_codes table
        String checkInviteCodeEndpoint = "/rest/v1/invite_codes?hive_id=eq." + hiveId + "&select=invite_code";
        String checkResponse = get(checkInviteCodeEndpoint);
        JSONArray existingInviteCodes = new JSONArray(checkResponse);

        if (existingInviteCodes.length() > 0) {
            // If the hive_id exists, update the invite_code using the general patch method
            String json = String.format("{\"invite_code\": \"%s\"}", newInviteCode);
            String updateEndpoint = "/rest/v1/invite_codes?hive_id=eq." + hiveId;
            patch(updateEndpoint, json);
        } else {
            // If the hive_id does not exist, insert a new record using the general post method
            String json = String.format("{\"hive_id\": \"%s\", \"invite_code\": \"%s\"}", hiveId, newInviteCode);
            String insertEndpoint = "/rest/v1/invite_codes";
            post(insertEndpoint, json);
        }

        // GUI-based feedback
        CustomDialog.showInfo("New invitation code generated: " + newInviteCode);
    }

    public String selectNewLeaderByName(String hiveId, String newLeaderName, MainFrame mainFrame) {
        if (newLeaderName == null || newLeaderName.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. Retrieve the user by username
            String userEndpoint = "/rest/v1/users?username=eq." + newLeaderName + "&select=id";
            String userResponse = get(userEndpoint);
            JSONArray userArray = new JSONArray(userResponse);
            if (userArray.length() == 0) {
                CustomDialog.showError("Invalid username. No such member exists.");
                return null;
            }
            String newLeaderId = userArray.getJSONObject(0).getString("id");

            // 2. Retrieve the HIVE data
            String hiveEndpoint = "/rest/v1/hives?id=eq." + hiveId + "&select=name,leader_name,hive_members";
            String hiveResponse = get(hiveEndpoint);
            JSONArray hiveArray = new JSONArray(hiveResponse);
            if (hiveArray.length() == 0) {
                CustomDialog.showError("Hive does not exist.");
                return null;
            }
            JSONObject hiveObj = hiveArray.getJSONObject(0);
            String hiveName = hiveObj.optString("name", "Unknown Hive");
            String oldLeaderName = hiveObj.optString("leader_name", null);
            JSONArray hiveMembers = hiveObj.optJSONArray("hive_members");

            // 3. Update the hives table: set the leader_name field to the new leader's name.
            patchHiveLeaderName(hiveId, newLeaderName);

            // 4. Update the users table: set is_leader to true for the selected user.
            updateUserHiveInfo(newLeaderId, hiveId, true);

            // 5. Find the current (old) leader, if exists, and set their is_leader value to false.
            String leaderQueryEndpoint = "/rest/v1/users?hive_id=eq." + hiveId + "&is_leader=eq.true&select=id";
            String leaderQueryResponse = get(leaderQueryEndpoint);
            JSONArray leaderArray = new JSONArray(leaderQueryResponse);
            for (int i = 0; i < leaderArray.length(); i++) {
                JSONObject leaderObj = leaderArray.getJSONObject(i);
                String leaderId = leaderObj.getString("id");
                if (!leaderId.equals(newLeaderId)) {
                    updateUserHiveInfo(leaderId, hiveId, false);
                }
            }

            // 6. If the current user was the old leader, update the local status and switch to the MemberMenuPanel.
            if (mainFrame.getUser() != null && mainFrame.getUser().getUserId().equals(newLeaderId)) {
                mainFrame.getUser().setLeader(true);
            } else if (mainFrame.getUser() != null && mainFrame.getUser().getHiveId().equals(hiveId)) {
                mainFrame.getUser().setLeader(false);
                mainFrame.showPanel("MemberMenuPanel");
            }

            CustomDialog.showInfo("New leader is " + newLeaderName);
            return newLeaderId;

        } catch (IOException | InterruptedException | JSONException e) {
            CustomDialog.showError("Error fetching or updating user details: " + e.getMessage());
            return null;
        }
    }

    public void addPerkToMember(String hiveId, String memberUsername, String perkName, MainFrame mainFrame) throws IOException, InterruptedException {
        try {
            // 1. Ellenőrizzük, hogy a felhasználó létezik-e a "users" táblában (username alapján), és lekérjük az id és perks mezőket
            String userEndpoint = "/rest/v1/users?username=eq." + memberUsername + "&select=id,perks";
            String userResponse = get(userEndpoint);
            JSONArray userArray = new JSONArray(userResponse);

            if (userArray.length() == 0) {
                CustomDialog.showError("User not found.");
                return;
            }
            JSONObject userObj = userArray.getJSONObject(0);
            String userId = userObj.getString("id");

            // 2. Lekérjük a felhasználó aktuális perkjeit
            List<String> perks = new ArrayList<>();
            JSONArray perksArray = userObj.optJSONArray("perks");
            if (perksArray != null) {
                for (int i = 0; i < perksArray.length(); i++) {
                    perks.add(perksArray.get(i).toString());
                }
            }

            // 3. Ellenőrizzük, hogy a felhasználó már rendelkezik-e az adott perkkel
            if (perks.contains(perkName)) {
                CustomDialog.showError("Member already has this perk.");
                return;
            }
            // Limit ellenőrzés: maximum 10 perk
            if (perks.size() >= 10) {
                CustomDialog.showError("Member cannot have more than 10 perks.");
                return;
            }

            // 4. Hozzáadjuk az új perket a perk listához
            perks.add(perkName);
            JSONObject updatePayload = new JSONObject();
            updatePayload.put("perks", perks);

            // 5. Frissítjük a felhasználó rekordját a Supabase-ben a PATCH metódussal
            String updateUserEndpoint = "/rest/v1/users?id=eq." + userId;
            patch(updateUserEndpoint, updatePayload.toString());

            // 6. Helyi gyorsítótár frissítése:
            Map<String, List<Perk>> cachedPerks = mainFrame.getCachedMemberPerks();
            // Gyártunk egy új perk objektumot a PerkStorage segítségével
            Perk perkObj = mainFrame.getPerkStorage().getPerkByName(perkName);
            if (perkObj != null) {
                // Ha a cache nem tartalmazza a tagot, akkor hozzuk létre az entry-t
                cachedPerks.computeIfAbsent(memberUsername, k -> new ArrayList<>()).add(perkObj);
            }
            mainFrame.setCachedMemberPerks(cachedPerks);

            // Frissítjük a HiveInfoPanel-t, ha az elérhető
            for (java.awt.Component comp : mainFrame.getMainPanel().getComponents()) {
                if (comp instanceof gui.HiveInfoPanel) {
                    ((gui.HiveInfoPanel) comp).updateData(mainFrame.getCachedHiveMembers(),
                            mainFrame.getCachedMemberPerks(),
                            mainFrame.getPerkStorage());
                    comp.revalidate();
                    comp.repaint();
                    break;
                }
            }

            CustomDialog.showInfo("Perk added successfully!");
        } catch (RuntimeException e) {
            CustomDialog.showError("Error adding perk: " + e.getMessage());
        }
    }

    // A hives táblában a leader_name mező frissítésére (PATCH)
    public void patchHiveLeaderName(String hiveId, String newLeaderName) throws IOException, InterruptedException {
        if (!isValidUUID(hiveId)) {
            throw new IllegalArgumentException("Invalid UUID format for hiveId: " + hiveId);
        }
        String endpoint = "/rest/v1/hives?id=eq." + hiveId;
        String json = String.format("{\"leader_name\": \"%s\"}", newLeaderName);
        patch(endpoint, json);
    }

    public void replacePerkForMember(String hiveId, String userId, String oldPerk, String newPerk, MainFrame mainFrame) {
        try {
            // Step 1: Lekérjük a felhasználó adatait a "users" táblából (perks és username)
            String userEndpoint = "/rest/v1/users?id=eq." + userId + "&select=perks,username";
            String userResponse = get(userEndpoint);
            JSONArray userArray = new JSONArray(userResponse);
            if (userArray.length() == 0) {
                CustomDialog.showError("User not found.");
                return;
            }
            JSONObject userObj = userArray.getJSONObject(0);
            String username = userObj.optString("username", "");
            JSONArray perksArray = userObj.optJSONArray("perks");
            List<String> perks = new ArrayList<>();
            if (perksArray != null) {
                for (int i = 0; i < perksArray.length(); i++) {
                    perks.add(perksArray.getString(i));
                }
            }

            // Step 2: Ellenőrizzük, hogy a felhasználónak van-e régi perkje
            if (!perks.contains(oldPerk)) {
                CustomDialog.showError("Member does not have the old perk.");
                return;
            }

            // Ellenőrizzük, hogy az új perk még nem szerepel-e a listában
            if (perks.contains(newPerk)) {
                CustomDialog.showError("Member already has the new perk.");
                return;
            }

            // Step 3: Lecseréljük a régi perk nevét az új perk nevére
            perks.remove(oldPerk);
            perks.add(newPerk);

            // Step 4: Frissítjük a users táblában a perk listát a Supabase REST API PATCH hívásával
            JSONObject payload = new JSONObject();
            payload.put("perks", perks);
            String patchEndpoint = "/rest/v1/users?id=eq." + userId;
            patch(patchEndpoint, payload.toString());

            // Step 5: Helyi gyorsítótár frissítése:
            // A cachedMemberPerks egy Map<String, List<Perk>>, ahol a kulcs a felhasználó neve.
            Map<String, List<Perk>> memberPerks = mainFrame.getCachedMemberPerks();
            List<Perk> userPerks = memberPerks.get(username);
            if (userPerks != null) {
                // Eltávolítjuk a régi perk objektumát, ha létezik
                userPerks.removeIf(perk -> perk.getName().equals(oldPerk));
                // Új perk objektum lekérése a PerkStorage-ból
                Perk newPerkObj = mainFrame.getPerkStorage().getPerkByName(newPerk);
                if (newPerkObj != null) {
                    userPerks.add(newPerkObj);
                }
                memberPerks.put(username, userPerks);
                mainFrame.setCachedMemberPerks(memberPerks);
            }

            // Frissítjük a HiveInfoPanel-t, ha az elérhető
            for (java.awt.Component comp : mainFrame.getMainPanel().getComponents()) {
                if (comp instanceof gui.HiveInfoPanel) {
                    ((gui.HiveInfoPanel) comp).updateData(mainFrame.getCachedHiveMembers(),
                            mainFrame.getCachedMemberPerks(),
                            mainFrame.getPerkStorage());
                    comp.revalidate();
                    comp.repaint();
                    break;
                }
            }
            CustomDialog.showInfo("Perk replaced successfully!");
        } catch (IOException | JSONException e) {
            CustomDialog.showError("Error replacing perk: " + e.getMessage());
        }
    }

    public void removeAllPerksFromMember(String userId, MainFrame mainFrame) {

        // Kérjük a felhasználótól a megerősítést  
        CustomConfirmationPopup confirmationPopup = new CustomConfirmationPopup(
                "Confirm",
                "Are you sure you want to remove all perks?"
        );

        if (!confirmationPopup.show()) {
            return; // If user selects No, exit method  
        }

        try {
            // 1. Frissítjük a felhasználó rekordját a Supabase "users" táblában: a perks mezőt üres tömbre állítjuk
            String patchEndpoint = "/rest/v1/users?id=eq." + userId;
            String jsonPayload = "{\"perks\": []}";
            patch(patchEndpoint, jsonPayload);

            // 2. Keressük meg a felhasználó nevét a helyi gyorsítótárban (cachedHiveMembers) a userId alapján
            String username = "";
            for (User u : mainFrame.getCachedHiveMembers()) {
                if (u.getUserId().equals(userId)) {
                    username = u.getUsername();
                    break;
                }
            }

            // Ha a username üres, akkor nem találtuk meg a tagot a gyorsítótárban
            if (username.isEmpty()) {
                CustomDialog.showError("User not found in local cache.");
                return;
            }

            // 3. Frissítjük a helyi gyorsítótárat: eltávolítjuk a felhasználó perk listáját a cache-ből
            Map<String, List<Perk>> memberPerks = mainFrame.getCachedMemberPerks();
            if (memberPerks.containsKey(username)) {
                memberPerks.remove(username);
                mainFrame.setCachedMemberPerks(memberPerks);
            }

            // Frissítjük a HiveInfoPanel-t, ha az elérhető
            for (java.awt.Component comp : mainFrame.getMainPanel().getComponents()) {
                if (comp instanceof gui.HiveInfoPanel) {
                    ((gui.HiveInfoPanel) comp).updateData(mainFrame.getCachedHiveMembers(),
                            mainFrame.getCachedMemberPerks(),
                            mainFrame.getPerkStorage());
                    comp.revalidate();
                    comp.repaint();
                    break;
                }
            }

            CustomDialog.showInfo("All perks removed successfully!");
        } catch (IOException e) {
            CustomDialog.showError("Error removing perks: " + e.getMessage());
        }
    }

    public void removeMemberFromHive(String hiveId, String memberUsername, MainFrame mainFrame) throws ExecutionException, InterruptedException {
        try {
            // 1. Lekérjük a HIVE adatait (hive_members és leader_name)
            String hiveEndpoint = "/rest/v1/hives?id=eq." + hiveId + "&select=hive_members,leader_name";
            String hiveResponse = get(hiveEndpoint);
            JSONArray hiveArray = new JSONArray(hiveResponse);
            if (hiveArray.length() == 0) {
                CustomDialog.showError("Hive does not exist.");
                return;
            }
            JSONObject hiveObj = hiveArray.getJSONObject(0);
            JSONArray membersArray = hiveObj.optJSONArray("hive_members");
            if (membersArray == null || membersArray.length() == 0) {
                CustomDialog.showError("No members in this Hive.");
                return;
            }
            List<String> members = new ArrayList<>();
            for (int i = 0; i < membersArray.length(); i++) {
                members.add(membersArray.getString(i));
            }

            // 2. Ellenőrizzük, hogy a tag szerepel-e a listában
            if (!members.contains(memberUsername)) {
                CustomDialog.showError("Member not found in this Hive.");
                return;
            }

            // 3. Ha a tag a HIVE vezetője, ne engedjük a törlést
            String leaderName = hiveObj.optString("leader_name", "");
            if (memberUsername.equals(leaderName)) {
                CustomDialog.showError("<html>Cannot remove the leader of the Hive.<br>Please assign a new leader before removal.</html>");
                return;
            }

            // 4. Frissítjük a HIVE rekordot: eltávolítjuk a memberUsername-t a hive_members listából
            members.remove(memberUsername);
            JSONObject patchHivePayload = new JSONObject();
            patchHivePayload.put("hive_members", members);
            String patchHiveEndpoint = "/rest/v1/hives?id=eq." + hiveId;
            patch(patchHiveEndpoint, patchHivePayload.toString());

            // 5. Frissítjük a felhasználó rekordját a users táblában: a hive_id-t null-ra állítjuk
            String patchUserEndpoint = "/rest/v1/users?username=eq." + memberUsername;
            JSONObject patchUserPayload = new JSONObject();
            patchUserPayload.put("hive_id", JSONObject.NULL);
            patch(patchUserEndpoint, patchUserPayload.toString());

            // 6. Helyi gyorsítótár frissítése: eltávolítjuk a tagot a cachedHiveMembers listából,
            //    valamint töröljük a taghoz tartozó perk listát a cachedMemberPerks map-ből.
            List<User> cachedMembers = mainFrame.getCachedHiveMembers();
            cachedMembers.removeIf(u -> u.getUsername().equals(memberUsername));
            mainFrame.setCachedHiveMembers(cachedMembers);
            Map<String, List<Perk>> cachedPerks = mainFrame.getCachedMemberPerks();
            cachedPerks.remove(memberUsername);
            mainFrame.setCachedMemberPerks(cachedPerks);

            CustomDialog.showInfo("Member removed from HIVE!");
        } catch (IOException | JSONException e) {
            CustomDialog.showError("Error removing member: " + e.getMessage());
        }
    }

    public void disbandHive(String hiveId, MainFrame mainFrame) {
        // Kérjük a felhasználótól a megerősítést  
        CustomConfirmationPopup confirmationPopup = new CustomConfirmationPopup(
                "Confirm",
                "Are you sure you want to disband the Hive?"
        );

        if (!confirmationPopup.show()) {
            return; // If user selects No, exit method  
        }
        
        try {
            // 1. Lekérjük a HIVE adatait (csak a hive_members mezőt)
            String hiveEndpoint = "/rest/v1/hives?id=eq." + hiveId + "&select=hive_members";
            String hiveResponse = get(hiveEndpoint);
            if (!hiveResponse.trim().startsWith("[")) {
                CustomDialog.showError("Error retrieving Hive data: " + hiveResponse);
                return;
            }
            JSONArray hiveArray = new JSONArray(hiveResponse);
            if (hiveArray.length() == 0) {
                CustomDialog.showError("Hive does not exist.");
                return;
            }
            JSONObject hiveObj = hiveArray.getJSONObject(0);

            // 2. Lekérjük a hive_members mezőt
            JSONArray membersArray = hiveObj.optJSONArray("hive_members");
            List<String> members = new ArrayList<>();
            if (membersArray != null) {
                for (int i = 0; i < membersArray.length(); i++) {
                    members.add(membersArray.getString(i));
                }
            }

            // 3. Frissítjük az összes tagot: a users táblában a hive_id-t null-ra, az is_leader értéket false-ra állítjuk
            if (!members.isEmpty()) {
                for (String memberUsername : members) {
                    String patchEndpoint = "/rest/v1/users?username=eq." + memberUsername;
                    String jsonPayload = "{\"hive_id\": null, \"is_leader\": false}";
                    patch(patchEndpoint, jsonPayload);
                }
            } else {
                CustomDialog.showInfo("No members in this Hive.");
            }

            // 4. Először töröljük az invite_codes bejegyzéseket
            String deleteInvitesEndpoint = "/rest/v1/invite_codes?hive_id=eq." + hiveId;
            String deleteInvitesResponse = delete(deleteInvitesEndpoint);
            if (deleteInvitesResponse != null && !deleteInvitesResponse.trim().isEmpty()) {
                CustomDialog.showError("Failed to delete invite codes: " + deleteInvitesResponse);
                return;
            }

            // 5. Töröljük a HIVE rekordját
            String deleteHiveEndpoint = "/rest/v1/hives?id=eq." + hiveId;
            String deleteHiveResponse = delete(deleteHiveEndpoint);
            if (deleteHiveResponse != null && !deleteHiveResponse.trim().isEmpty()) {
                CustomDialog.showError("Failed to delete Hive: " + deleteHiveResponse);
                return;
            }
            
            mainFrame.showPanel("AuthPanel");

            CustomDialog.showInfo("Hive has been disbanded!");
        } catch (IOException | JSONException e) {
            CustomDialog.showError("Error disbanding Hive: " + e.getMessage());
        }
    }
}
