package models;

import java.util.List;

public class User {
    private String userId;
    private String username;
    private String hiveId;
    private boolean isLeader;
    private List<Perk> perks;

    // Üres konstruktor szükséges a Firestore használatához
    public User() {}

    // Paraméterezett konstruktor
    public User(String userId, String username, String hiveId, boolean isLeader, List<Perk> perks) {
        this.userId = userId;
        this.username = username;
        this.hiveId = hiveId;
        this.isLeader = isLeader;
        this.perks = perks;
    }

    // Getterek és Setterek
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHiveId() {
        return hiveId;
    }

    public void setHiveId(String hiveId) {
        this.hiveId = hiveId;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    public void setPerks(List<Perk> perks) {
        this.perks = perks;
    }  

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", hive_id='" + hiveId + '\'' +
                ", is_leader=" + isLeader +
                ", perks=" + perks +
                '}';
    }
}
