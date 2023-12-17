package com.ashomok.chatoflegends.events;

/**
 * Created by Devlomi on 04/01/2018.
 */

public class UpdateNetworkProgress {
    private final String id;
    private final int progress;

    public UpdateNetworkProgress(String id, int progress) {
        this.id = id;
        this.progress = progress;
    }

    public String getId() {
        return id;
    }

    public int getProgress() {
        return progress;
    }
}
