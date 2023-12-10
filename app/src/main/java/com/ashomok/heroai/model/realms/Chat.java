package com.ashomok.heroai.model.realms;

import com.ashomok.heroai.utils.TimeHelper;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Devlomi on 03/08/2017.
 */

public class Chat extends RealmObject {
    @PrimaryKey
    //index for faster querying
    @Index
    private String chatId;
    //last message for this chat
    private Message lastMessage;
    //last message time ,this is used when deleting last message
    // to save the last message time stamp and keep chatList ordered
    private String lastMessageTimestamp;
    //the user in this chat
    private Model model;

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message message) {
        lastMessage = message;
    }

    public Model getModel() {
        return model;
    }

    public void setUser(Model model) {
        this.model = model;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }


    //to use list.contains or list.indexOf
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Chat) {
            Chat chat = (Chat) obj;
            return chat.chatId.equals(chatId);
        }
        return false;
    }

    //to print user.toString() properly (debugging purposes)
    @Override
    public String toString() {
        return "Chat{" +
                "chatId='" + chatId + '\'' +
                ", lastMessageTimestamp='" + lastMessageTimestamp + '\'' +
                ", user=" + model +
                '}';
    }

    public String getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(String lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getTime() {
        return TimeHelper.getMessageTime(lastMessageTimestamp);
    }

}
