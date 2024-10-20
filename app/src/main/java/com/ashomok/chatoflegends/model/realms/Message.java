package com.ashomok.chatoflegends.model.realms;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.ashomok.chatoflegends.model.constants.MessageType;
import com.ashomok.chatoflegends.utils.RealmHelper;
import com.ashomok.chatoflegends.utils.TimeHelper;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;


public class Message extends RealmObject implements Parcelable, Comparable, Serializable {
    //Parcelable
    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    @Index
    private String messageId;
    //sender id
    private String fromId;

    //receiver id
    private String toId;
    //message type (text,image,video etc..)
    private int type;
    //message content (text content,media item path in database)
    private String content;
    //this is used when the message is too long and couldn't be send by FCM
    //so we cut the text message to it shows a part of it, and then fetch the full message
    private String partialText;
    //message timestamp
    private String timestamp;
    @Index
    private String chatId;
    //messageState if it's pending,sent,read or received
    private int messageStat;

    //download upload state (loading,cancelled,success,finished)
    private int downloadUploadStat;
    //metadata could be (fileSize,videoSize or fileName)
    private String metadata;

    //media total duration (audio,voice or video length)

    //blurred thumb decoded as BASE64
    //this is used when a user sends an image or video to another user
    //and that user did not download the image or video so it can show what content it is before downloading
    //video thumb (not blurred) used to show thumb for a video in recyclerView
    //it is also decoded as BASE64
    //file size for (file,video,audio,image) types

    //when sending or receiving a contact
    private RealmContact contact;

    public Message() {
    }

    public Message(String messageId, String fromId,
                   String toId, int type, String content, String timestamp,
                   String chatId, int messageStat, int downloadUploadStat,
                   String metadata,
                   RealmContact contact) {
        this.messageId = messageId;
        this.fromId = fromId;
        this.toId = toId;
        this.type = type;
        this.content = content;
        this.timestamp = timestamp;
        this.chatId = chatId;
        this.messageStat = messageStat;
        this.downloadUploadStat = downloadUploadStat;
        this.metadata = metadata;
        this.contact = contact;
    }

    //Parcelable
    protected Message(Parcel in) {
        messageId = in.readString();
        fromId = in.readString();
        toId = in.readString();
        type = in.readInt();
        content = in.readString();
        timestamp = in.readString();
        chatId = in.readString();
        messageStat = in.readInt();
        downloadUploadStat = in.readInt();
        metadata = in.readString();
        contact = in.readParcelable(RealmContact.class.getClassLoader());
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }


    public void setToId(String toId) {
        this.toId = toId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    //get formatted time
    public String getTime() {
        return TimeHelper.getMessageTime(timestamp);
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public int getMessageStat() {
        return messageStat;
    }

    public void setMessageStat(int messageStat) {
        this.messageStat = messageStat;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", fromId='" + fromId + '\'' +
                ", toId='" + toId + '\'' +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", chatId='" + chatId + '\'' +
                ", messageStat=" + messageStat +
                ", downloadUploadStat=" + downloadUploadStat +
                ", metadata='" + metadata + '\'' +
                ", contact=" + contact +
                '}';
    }

    //to use list.contains or list.indexOf
    @Override
    public boolean equals(Object o) {
        if (o instanceof Message) {
            Message temp = (Message) o;
            return isValid() && messageId.equals(temp.messageId);
        }
        return false;
    }


    public boolean isExists() {
        return RealmHelper.getInstance().isExists(messageId);
    }

    public boolean isTextMessage() {
        return MessageType.SENT_TEXT == type || MessageType.RECEIVED_TEXT == type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //Parcelable
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(messageId);
        dest.writeString(fromId);
        dest.writeString(toId);
        dest.writeInt(type);
        dest.writeString(content);
        dest.writeString(timestamp);
        dest.writeString(chatId);
        dest.writeInt(messageStat);
        dest.writeInt(downloadUploadStat);
        dest.writeString(metadata);
        dest.writeParcelable(contact, 0);
    }

    //used to sort messages by timestamp when user selects messages and want to copy them
    @Override
    public int compareTo(@NonNull Object o) {
        if (o instanceof Message) {
            Message message = (Message) o;
            Date d1 = new Date(Long.parseLong(timestamp));
            Date d2 = new Date(Long.parseLong(message.timestamp));
            return d1.compareTo(d2);
        }
        return 0;
    }

}
