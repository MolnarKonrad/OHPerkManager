package models;

import java.util.List;

public class Hive {
    private String id;
    private String name; 
    private String leaderName;
    private List<String> members;
   
    public Hive() {}
    
    public Hive(String id, String name, String leaderName, List<String> members) {
        this.id = id;
        this.name = name;
        this.leaderName = leaderName;
        this.members = members;
    }
   
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
