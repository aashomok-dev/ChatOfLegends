package com.ashomok.chatoflegends.events;

public class GroupActiveStateChanged {
    private final String groupId;
    private final boolean isActive;

    public GroupActiveStateChanged(String groupId, boolean isActive) {
        this.groupId = groupId;
        this.isActive = isActive;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isActive() {
        return isActive;
    }
}
