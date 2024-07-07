package com.example.madproject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Activity implements Serializable {
    private String id;
    private String title;
    private String description;
    private String location;
    private long dateTime;
    private String creatorId;
    private String creatorName;
    private List<String> interestedUsers;

    public Activity() {
        // Default constructor required for calls to DataSnapshot.getValue(Activity.class)
    }

    public Activity(String id, String title, String description, String location, long dateTime, String creatorId, String creatorName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.dateTime = dateTime;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.interestedUsers = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getDateTime() { return dateTime; }
    public void setDateTime(long dateTime) { this.dateTime = dateTime; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public List<String> getInterestedUsers() { return interestedUsers; }
    public void setInterestedUsers(List<String> interestedUsers) { this.interestedUsers = interestedUsers; }

    public boolean isUserInterested(String userId) {
        return interestedUsers != null && interestedUsers.contains(userId);
    }

    public void addInterestedUser(String userId) {
        if (interestedUsers == null) {
            interestedUsers = new ArrayList<>();
        }
        if (!interestedUsers.contains(userId)) {
            interestedUsers.add(userId);
        }
    }

    public void removeInterestedUser(String userId) {
        if (interestedUsers != null) {
            interestedUsers.remove(userId);
        }
    }

    public int getInterestedCount() {
        return interestedUsers != null ? interestedUsers.size() : 0;
    }
}