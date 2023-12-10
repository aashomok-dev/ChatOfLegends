package com.ashomok.heroai.events;

/**
 * Created by Devlomi on 06/01/2018.
 */

public class OnNetworkComplete {
    private final String id;

    public OnNetworkComplete(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
