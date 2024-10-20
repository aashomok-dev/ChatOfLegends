package com.ashomok.chatoflegends.model.constants;

/**
 * Created by Devlomi.
 */
//indicates the message type
public enum MessageType {
    ;
    public static final int SENT_TEXT = 1;
    public static final int SENT_IMAGE = 2;
    public static final int RECEIVED_TEXT = 3;
    public static final int RECEIVED_IMAGE = 4;
    public static final int SENT_VIDEO = 5;
    public static final int RECEIVED_VIDEO = 6;
    public static final int SENT_AUDIO = 9;
    public static final int RECEIVED_AUDIO = 10;
    public static final int SENT_VOICE_MESSAGE = 11;
    public static final int RECEIVED_VOICE_MESSAGE = 12;
    public static final int SENT_FILE = 13;
    public static final int RECEIVED_FILE = 14;
    public static final int DAY_ROW = 15;
    public static final int SENT_CONTACT = 16;
    public static final int RECEIVED_CONTACT = 17;
    public static final int SENT_LOCATION = 18;
    public static final int RECEIVED_LOCATION = 19;

    public static final int SENT_DELETED_MESSAGE = 30;
    public static final int RECEIVED_DELETED_MESSAGE = 31;

    public static final int REPLY_STATUS = 32;

    public static final int SENT_STICKER = 33;
    public static final int RECEIVED_STICKER = 34;


    public static final int GROUP_EVENT = 9999;

    public static final int[] SUPPORTED_MESSAGES_TYPES = {
            MessageType.SENT_TEXT,
            MessageType.SENT_IMAGE,
            MessageType.RECEIVED_TEXT,
            MessageType.RECEIVED_IMAGE,
            MessageType.SENT_VIDEO,
            MessageType.RECEIVED_VIDEO,
            MessageType.SENT_AUDIO,
            MessageType.RECEIVED_AUDIO,
            MessageType.SENT_VOICE_MESSAGE,
            MessageType.RECEIVED_VOICE_MESSAGE,
            MessageType.SENT_FILE,
            MessageType.RECEIVED_FILE,
            MessageType.DAY_ROW,
            MessageType.SENT_CONTACT,
            MessageType.RECEIVED_CONTACT,
            MessageType.SENT_LOCATION,
            MessageType.RECEIVED_LOCATION,
            MessageType.SENT_DELETED_MESSAGE,
            MessageType.RECEIVED_DELETED_MESSAGE,
            MessageType.GROUP_EVENT,
            MessageType.REPLY_STATUS,
            MessageType.SENT_STICKER,
            MessageType.RECEIVED_STICKER
    };

    public static boolean isMessageSupported(int type) {
        for (int i = 0; i < MessageType.SUPPORTED_MESSAGES_TYPES.length; i++) {
            int mType = MessageType.SUPPORTED_MESSAGES_TYPES[i];
            if (mType == type)
                return true;
        }
        return false;
    }

    public static boolean isSentType(int type) {
        return SENT_TEXT == type || SENT_IMAGE == type || SENT_VIDEO == type || SENT_AUDIO == type
                || SENT_FILE == type || SENT_VOICE_MESSAGE == type
                || SENT_CONTACT == type || SENT_LOCATION == type
                || SENT_STICKER == type;
    }

}
