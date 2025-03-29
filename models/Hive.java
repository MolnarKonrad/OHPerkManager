package models;

import java.util.List;

public class Hive {
    private String id;  // A Firestore dokumentum azonosítója
    private String name;  // HIVE neve
    private String leaderName;  // HIVE vezető neve
    private List<String> members;  // A tagok listája

    // Paraméter nélküli konstruktor (szükséges Firebase számára)
    public Hive() {}

    // Paraméteres konstruktor a Hive létrehozásához
    public Hive(String id, String name, String leaderName, List<String> members) {
        this.id = id;
        this.name = name;
        this.leaderName = leaderName;
        this.members = members;
    }

    // Getterek és setterek
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "Hive{id='" + id + "', name='" + name + "', leaderName='" + leaderName + "', members=" + members + "}";
    }
}
