package services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gui.CustomDialog;
import gui.MainFrame;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import models.Perk;
import models.User;
import utils.SupabaseClient;

public class FirebaseService {

    private final SupabaseClient supabase;
    private final PerkStorage perkStorage;

    public FirebaseService(SupabaseClient supabase, PerkStorage perkStorage) {
        this.supabase = supabase;
        this.perkStorage = perkStorage;
    }

    public SupabaseClient getSupabase() {
        return supabase;
    }

    public void generateNewInviteCode(String hiveId) throws ExecutionException, InterruptedException {
        // Új meghívókód generálása (6 karakteres alfanumerikus kód)
        String newInviteCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Firestore frissítése a HIVE dokumentumban
        db.collection("hives").document(hiveId)
                .update("inviteCode", newInviteCode)
                .get();

        // GUI-alapú visszajelzés
        CustomDialog.showInfo("New invitation code generated: " + newInviteCode);
    }

    public User getUserById(String userId) {
        try {
            DocumentSnapshot document = db.collection("users").document(userId).get().get();
            if (document.exists()) {
                return document.toObject(User.class);
            }
        } catch (InterruptedException | ExecutionException e) {
            CustomDialog.showError("Error retrieving user: " + e.getMessage());
        }
        return null;
    }

    public List<String> getMemberPerks(String userId) throws ExecutionException, InterruptedException {
        DocumentSnapshot userSnapshot = db.collection("users").document(userId).get().get();

        if (!userSnapshot.exists()) {
            CustomDialog.showError("User not found!");
            return new ArrayList<>();
        }

        List<String> perks = (List<String>) userSnapshot.get("perks");
        return (perks != null) ? perks : new ArrayList<>();
    }

    public void removeMemberFromHive(String hiveId, String memberUsername, MainFrame mainFrame) throws ExecutionException, InterruptedException {
        DocumentReference hiveRef = db.collection("hives").document(hiveId);

        try {
            DocumentSnapshot hiveSnapshot = hiveRef.get().get();
            if (!hiveSnapshot.exists()) {
                CustomDialog.showError("HIVE does not exist.");
                return;
            }

            List<String> members = (List<String>) hiveSnapshot.get("members");
            if (members == null || members.isEmpty()) {
                CustomDialog.showError("No members in this HIVE.");
                return;
            }

            Query userQuery = db.collection("users").whereEqualTo("username", memberUsername);
            QuerySnapshot userQuerySnapshot = userQuery.get().get();

            if (userQuerySnapshot.isEmpty()) {
                CustomDialog.showError("User not found.");
                return;
            }

            DocumentSnapshot userDocSnapshot = userQuerySnapshot.getDocuments().get(0);
            String userId = userDocSnapshot.getId();

            if (!members.contains(userId)) {
                CustomDialog.showError("Member not found in this HIVE.");
                return;
            }

            boolean isLeader = userDocSnapshot.getBoolean("isLeader") != null
                    && Boolean.TRUE.equals(userDocSnapshot.getBoolean("isLeader"));
            if (isLeader) {
                CustomDialog.showInfo("You are the leader! Before you can leave, you must choose a new leader!");
                if (members.size() == 1) {
                    CustomDialog.showError("Cannot remove the only member of the HIVE.");
                    return;
                }
            }

            // Frissítés a Firestore-ban
            members.remove(userId);
            hiveRef.update("members", members).get();
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.update("hiveId", null).get();

            // 🔹 Helyi gyorsítótár frissítése
            List<User> cachedMembers = mainFrame.getCachedHiveMembers();
            cachedMembers.removeIf(u -> u.getUserId().equals(userId)); // userId itt az eltávolítandó tag azonosítója
            mainFrame.setCachedHiveMembers(cachedMembers);

            // A cachedPerks frissítése: itt a kulcs a tag felhasználóneve
            Map<String, List<Perk>> cachedPerks = mainFrame.getCachedMemberPerks();
            cachedPerks.remove(memberUsername);
            mainFrame.setCachedMemberPerks(cachedPerks);

            CustomDialog.showInfo("Member removed successfully!");
        } catch (Exception e) {
            CustomDialog.showError("Error removing member: " + e.getMessage());
        }
    }

    public String selectNewLeaderByName(String hiveId, String newLeaderName, MainFrame mainFrame) {
        if (newLeaderName == null || newLeaderName.trim().isEmpty()) {
            return null;
        }

        try {
            QuerySnapshot querySnapshot = db.collection("users")
                    .whereEqualTo("username", newLeaderName)
                    .get()
                    .get();

            if (querySnapshot.isEmpty()) {
                CustomDialog.showError("Invalid username. No such member exists.");
                return null;
            }

            String newLeaderId = querySnapshot.getDocuments().get(0).getId();

            DocumentReference hiveRef = db.collection("hives").document(hiveId);
            DocumentSnapshot hiveSnapshot = hiveRef.get().get();

            if (!hiveSnapshot.exists()) {
                CustomDialog.showError("HIVE does not exist.");
                return null;
            }

            List<String> memberIds = (List<String>) hiveSnapshot.get("members");
            if (memberIds == null || memberIds.isEmpty()) {
                CustomDialog.showError("No members found in this HIVE.");
                return null;
            }

            if (!memberIds.contains(newLeaderId)) {
                CustomDialog.showError("The user is not a member of this HIVE.");
                return null;
            }

            hiveRef.update("leaderId", newLeaderId).get();
            CustomDialog.showInfo("New leader is " + newLeaderName);

            DocumentReference userRef = db.collection("users").document(newLeaderId);
            userRef.update("isLeader", true).get();

            String oldLeaderId = hiveSnapshot.getString("leaderId");
            if (oldLeaderId != null && !oldLeaderId.equals(newLeaderId)) {
                DocumentReference oldLeaderRef = db.collection("users").document(oldLeaderId);
                oldLeaderRef.update("isLeader", false).get();
            }

            // 🔹 Ha az aktuális felhasználó volt a vezető, váltson a MemberMenuPanelre
            if (mainFrame.getUser() != null && mainFrame.getUser().getUserId().equals(oldLeaderId)) {
                mainFrame.getUser().setLeader(false); // 🔹 A felhasználó státuszának frissítése
                mainFrame.showPanel("MemberMenuPanel"); // 🔹 Panelváltás a tagok menüjére
            }

            return newLeaderId;
        } catch (Exception e) {
            CustomDialog.showError("Error fetching or updating user details: " + e.getMessage());
            return null;
        }
    }

    public void addPerkToMember(String hiveId, String memberUsername, String perkName, MainFrame mainFrame) {
        DocumentReference hiveRef = db.collection("hives").document(hiveId);

        try {
            DocumentSnapshot hiveSnapshot = hiveRef.get().get();
            if (!hiveSnapshot.exists()) {
                CustomDialog.showError("HIVE does not exist.");
                return;
            }

            List<String> memberIds = (List<String>) hiveSnapshot.get("members");
            if (memberIds == null || memberIds.isEmpty()) {
                CustomDialog.showError("No members in this HIVE.");
                return;
            }

            Query userQuery = db.collection("users").whereEqualTo("username", memberUsername);
            QuerySnapshot userQuerySnapshot = userQuery.get().get();

            if (userQuerySnapshot.isEmpty()) {
                CustomDialog.showError("User not found.");
                return;
            }

            DocumentSnapshot userDocSnapshot = userQuerySnapshot.getDocuments().get(0);
            String userId = userDocSnapshot.getId();

            if (!memberIds.contains(userId)) {
                CustomDialog.showError("Member is not part of this HIVE.");
                return;
            }

            Query perkQuery = db.collection("perks").whereEqualTo("name", perkName);
            QuerySnapshot perkQuerySnapshot = perkQuery.get().get();

            if (perkQuerySnapshot.isEmpty()) {
                CustomDialog.showError("Perk does not exist.");
                return;
            }

            // Lekérjük a felhasználó eddigi perkjeit
            List<String> perks = (List<String>) userDocSnapshot.get("perks");
            if (perks == null) {
                perks = new ArrayList<>();
            }

            // Ellenőrizzük, hogy a felhasználó már ne rendelkezzen a perkkel
            if (perks.contains(perkName)) {
                CustomDialog.showError("Member already has this perk.");
                return;
            }
            if (perks.size() >= 10) {
                CustomDialog.showError("Member cannot have more than 10 perks.");
                return;
            }

            // 🔹 Perk hozzáadása a Firestore-hoz (csak a nevét mentjük el)
            perks.add(perkName);
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.update("perks", perks).get();

            // 🔹 Helyi gyorsítótár frissítése
            Map<String, List<Perk>> memberPerks = mainFrame.getCachedMemberPerks();
            Perk perk = mainFrame.getPerkStorage().getPerkByName(perkName); // Perk objektum lekérése
            if (perk != null) {
                memberPerks.computeIfAbsent(memberUsername, k -> new ArrayList<>()).add(perk);
            }
            mainFrame.setCachedMemberPerks(memberPerks);

            CustomDialog.showInfo("Perk added successfully!");
        } catch (Exception e) {
            CustomDialog.showError("Error adding perk: " + e.getMessage());
        }
    }

    public void replacePerkForMember(String hiveId, String userId, String oldPerk, String newPerk, MainFrame mainFrame) {
        DocumentReference userRef = db.collection("users").document(userId);

        try {
            // Lekérjük a felhasználó adatait
            DocumentSnapshot userSnapshot = userRef.get().get();
            if (!userSnapshot.exists()) {
                CustomDialog.showError("User not found.");
                return;
            }

            // Lekérjük a felhasználó eddigi perkjeit
            List<String> perks = (List<String>) userSnapshot.get("perks");
            if (perks == null) {
                perks = new ArrayList<>();
            }

            // Ellenőrizzük, hogy a felhasználónak van-e régi perkje
            if (!perks.contains(oldPerk)) {
                CustomDialog.showError("Member does not have the old perk.");
                return;
            }

            // Ellenőrizzük, hogy az új perk már létezik-e a listában
            if (perks.contains(newPerk)) {
                CustomDialog.showError("Member already has the new perk.");
                return;
            }

            // Csak a régi perk nevét felülírjuk az új perk nevével
            perks.remove(oldPerk); // Eltávolítjuk a régi perk nevét
            perks.add(newPerk);    // Hozzáadjuk az új perk nevét

            // Frissítjük a Firestore adatbázist
            userRef.update("perks", perks).get();

            // 🔹 Helyi gyorsítótár frissítése is
            Map<String, List<Perk>> memberPerks = mainFrame.getCachedMemberPerks();

            // Keresd meg a régi perket, és cserélje ki az újjára a cache-ben
            List<Perk> userPerks = memberPerks.get(userSnapshot.getString("username"));
            if (userPerks != null) {
                // Eltávolítás a gyűjteményből
                userPerks.removeIf(perk -> perk.getName().equals(oldPerk));

                // Új perk hozzáadása
                Perk newPerkObj = mainFrame.getPerkStorage().getPerkByName(newPerk);
                if (newPerkObj != null) {
                    userPerks.add(newPerkObj);
                }
                memberPerks.put(userSnapshot.getString("username"), userPerks); // Frissítjük a cache-t
                mainFrame.setCachedMemberPerks(memberPerks); // Cache-frissítés
            }

            CustomDialog.showInfo("Perk replaced successfully!");
        } catch (Exception e) {
            CustomDialog.showError("Error replacing perk: " + e.getMessage());
        }
    }

    public void removeAllPerksFromMember(String userId, MainFrame mainFrame) {
        try {
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.update("perks", new ArrayList<>()).get(); // Firestore frissítése

            // Frissítjük a helyi adatokat is
            Map<String, List<Perk>> memberPerks = mainFrame.getCachedMemberPerks();
            String username = ""; // A felhasználónevet meg kell szerezni a Firestore-ból

            // Lekérdezzük a felhasználó nevét, hogy frissíthessük a helyi cache-t
            DocumentSnapshot userSnapshot = userRef.get().get();
            if (userSnapshot.exists()) {
                username = userSnapshot.getString("username");
            }

            // Ha a helyi gyorsítótáron van adat a felhasználóról, töröljük az összes perkjét
            if (memberPerks.containsKey(username)) {
                memberPerks.remove(username); // Töröljük az adott felhasználó perk listáját
                mainFrame.setCachedMemberPerks(memberPerks); // Frissítjük a cache-t
            }

            CustomDialog.showInfo("All perks removed successfully!");
        } catch (Exception e) {
            CustomDialog.showError("Error removing perks: " + e.getMessage());
        }
    }

    public void disbandHive(String hiveId) {
        DocumentReference hiveRef = db.collection("hives").document(hiveId);

        try {
            // 1. Ellenőrizzük, hogy létezik-e a HIVE
            DocumentSnapshot hiveSnapshot = hiveRef.get().get();
            if (!hiveSnapshot.exists()) {
                CustomDialog.showError("HIVE does not exist.");
                return;
            }

            // 2. Tagok lekérése a Firestore-ból
            List<String> memberIds = (List<String>) hiveSnapshot.get("members");
            if (memberIds != null && !memberIds.isEmpty()) {
                // 3. Minden tag esetében frissítjük a 'hiveId' mezőt null-ra és 'isLeader'-t false-ra
                for (String memberId : memberIds) {
                    DocumentReference userRef = db.collection("users").document(memberId);
                    userRef.update("hiveId", null, "isLeader", false).get();
                }
            } else {
                CustomDialog.showInfo("No members in this HIVE.");
            }

            // 4. A HIVE dokumentum törlése
            hiveRef.delete().get();
            CustomDialog.showInfo("HIVE has been disbanded successfully!");
        } catch (Exception e) {
            CustomDialog.showError("Error disbanding HIVE: " + e.getMessage());
        }
    }

    /**
     * Lekérdezi a HIVE adatokat.Ha a megadott hiveId üres vagy null, akkor a
     * felhasználó (userId alapján) saját adatait adja vissza.A cél az, hogy a
     * lekérdezés középpontjában a user álljon, így ha nincs Hive, akkor a user
     * adatai jelenjenek meg.
     *
     * @param hiveId
     * @param userId
     * @param perkStorage
     * @return
     * @throws java.util.concurrent.ExecutionException
     * @throws java.lang.InterruptedException
     */
    // Query the Hive document
    public CompletableFuture<Map<String, Object>> getHiveData(String hiveId, String userId, PerkStorage perkStorage) {
        Map<String, Object> hiveData = new HashMap<>();

        // If hiveId is empty, return the user's own data
        if (hiveId == null || hiveId.trim().isEmpty()) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    String userResponse = supabase.get("/rest/v1/users?id=eq." + userId);
                    List<Map<String, Object>> users = parseResponse(userResponse);
                    if (users.isEmpty()) {
                        System.out.println("User not found.");
                        return hiveData;
                    }

                    Map<String, Object> user = users.get(0);
                    User currentUser = new User(
                            user.get("id").toString(),
                            user.get("username").toString(),
                            user.get("hive_id") != null ? user.get("hive_id").toString() : null,
                            Boolean.TRUE.equals(user.get("is_leader")),
                            (List<String>) user.get("perks")
                    );

                    hiveData.put("hiveName", "No Hive");
                    List<User> members = new ArrayList<>();
                    members.add(currentUser);
                    hiveData.put("members", members);
                    hiveData.put("memberPerks", Map.of(currentUser.getUsername(), perkStorage.getPerksForUser(currentUser)));
                    return hiveData;
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    return hiveData;
                }
            });
        }

        // Query the Hive document
        return CompletableFuture.supplyAsync(() -> {
            try {
                String hiveResponse = supabase.get("/rest/v1/hives?id=eq." + hiveId);
                List<Map<String, Object>> hives = parseResponse(hiveResponse);

                if (hives.isEmpty()) {
                    System.out.println("⚠️ HIVE does not exist.");
                    return null;
                }

                Map<String, Object> hive = hives.get(0);
                String hiveName = hive.get("name") != null ? hive.get("name").toString() : "Unknown Hive";
                String leaderName = hive.get("leader_name") != null ? hive.get("leader_name").toString() : "Unknown Leader";

                // Get member IDs
                List<String> memberIds = (List<String>) hive.get("members");
                if (memberIds == null || memberIds.isEmpty()) {
                    System.out.println("⚠️ No members found in this HIVE.");
                    return null;
                }

                List<User> members = new ArrayList<>();
                Map<String, List<Perk>> memberPerks = new HashMap<>();

                // Fetch member data and their perks
                List<CompletableFuture<Void>> memberFutures = new ArrayList<>();

                for (String mId : memberIds) {
                    CompletableFuture<Void> memberFuture = CompletableFuture.supplyAsync(() -> {
                        try {
                            String userResponse = supabase.get("/rest/v1/users?id=eq." + mId);
                            List<Map<String, Object>> userList = parseResponse(userResponse);

                            if (userList.isEmpty()) {
                                System.out.println("⚠️ User data missing for ID: " + mId);
                                return null;
                            }

                            Map<String, Object> user = userList.get(0);
                            User member = new User(
                                    user.get("id").toString(),
                                    user.get("username").toString(),
                                    user.get("hive_id") != null ? user.get("hive_id").toString() : null,
                                    Boolean.TRUE.equals(user.get("is_leader")),
                                    (List<String>) user.get("perks")
                            );
                            members.add(member);

                            // Fetch perks for the member
                            String perksResponse = supabase.get("/rest/v1/user_perks?user_id=eq." + member.getUserId());
                            List<Map<String, Object>> perksList = parseResponse(perksResponse);
                            List<CompletableFuture<Perk>> perkFutures = new ArrayList<>();

                            for (Map<String, Object> perkData : perksList) {
                                String perkId = perkData.get("perk_id").toString();

                                // Fetch perk name from the perks table
                                CompletableFuture<Perk> perkFuture = CompletableFuture.supplyAsync(() -> {
                                    try {
                                        String perkResponse = supabase.get("/rest/v1/perks?id=eq." + perkId);
                                        List<Map<String, Object>> perkList = parseResponse(perkResponse);

                                        if (perkList.isEmpty()) {
                                            System.out.println("⚠️ Perk not found for ID: " + perkId);
                                            return null;
                                        }

                                        // Get the perk name from the response
                                        String perkName = perkList.get(0).get("name").toString();
                                        // Use PerkStorage to get the complete Perk object
                                        return perkStorage.getPerkByName(perkName);
                                    } catch (IOException | InterruptedException e) {
                                        e.printStackTrace();
                                        return null;
                                    }
                                });
                                perkFutures.add(perkFuture);
                            }

                            // Wait for all perk futures to complete
                            CompletableFuture.allOf(perkFutures.toArray(new CompletableFuture[0])).join();
                            List<Perk> perks = perkFutures.stream()
                                    .map(CompletableFuture::join)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                            memberPerks.put(member.getUsername(), perks);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    });
                    memberFutures.add(memberFuture);
                }

                // Wait for all member futures to complete
                CompletableFuture.allOf(memberFutures.toArray(new CompletableFuture[0])).join();
                // Store data in the hiveData map
                hiveData.put("hiveName", hiveName);
                hiveData.put("leaderName", leaderName);
                hiveData.put("members", members);
                hiveData.put("memberPerks", memberPerks);

                return hiveData;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    // Helper method to parse the JSON response
    private List<Map<String, Object>> parseResponse(String jsonResponse) {
        Gson gson = new Gson();
        return gson.fromJson(jsonResponse, new TypeToken<List<Map<String, Object>>>() {
        }.getType());
    }
}
