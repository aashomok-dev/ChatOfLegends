package com.ashomok.chatoflegends.utils;

import android.os.PersistableBundle;

public class PBundleUtil {
    private final String action;
    private final String messageId;
    private final String myUid;
    private final int stat;
    private final String groupId;
    private final String id;
    private final String chatId;

    private PBundleUtil(Builder builder) {
        action = builder.action;
        messageId = builder.messageId;
        myUid = builder.myUid;
        stat = builder.stat;
        groupId = builder.groupId;

        id = builder.id;
        chatId = builder.chatId;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public PersistableBundle getBundle() {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(IntentUtils.ACTION_TYPE, action);
        bundle.putString(IntentUtils.ID, id);
        if (null != messageId)
            bundle.putString(IntentUtils.EXTRA_MESSAGE_ID, messageId);
        if (null != myUid)
            bundle.putString(IntentUtils.EXTRA_MY_UID, myUid);
        if (0 != stat)
            bundle.putInt(IntentUtils.EXTRA_STAT, stat);
        if (null != groupId)
            bundle.putString(IntentUtils.EXTRA_GROUP_ID, groupId);
        if (null != chatId)
            bundle.putString(IntentUtils.EXTRA_CHAT_ID, chatId);
        return bundle;
    }

    public String getAction() {
        return action;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getChatId() {
        return chatId;
    }


    public String getGroupId() {
        return groupId;
    }

    public static class Builder {
        private String action;
        private String messageId;
        private String myUid;
        private int stat;
        private final String id;
        private String groupId;
        private String chatId;


        public Builder(String id) {
            this.id = id;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder myUid(String myUid) {
            this.myUid = myUid;
            return this;
        }

        public Builder stat(int stat) {
            this.stat = stat;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder chatId(String chatId) {
            this.chatId = chatId;
            return this;
        }

        public PBundleUtil build() {
            return new PBundleUtil(this);
        }
    }
}
