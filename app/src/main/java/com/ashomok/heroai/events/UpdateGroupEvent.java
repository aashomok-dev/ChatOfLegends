package com.ashomok.heroai.events;

public class UpdateGroupEvent {

    private final String groupId;

    public UpdateGroupEvent(String groupId) {
        this.groupId = groupId;
    }


    public String getGroupId() {
        return groupId;
    }
}
