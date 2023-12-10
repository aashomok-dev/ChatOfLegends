package com.ashomok.heroai.utils;

import android.content.Context;

import com.ashomok.heroai.model.constants.MessageStat;
import com.ashomok.heroai.model.constants.MessageType;
import com.ashomok.heroai.model.realms.Message;
import com.ashomok.heroai.model.realms.Model;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Devlomi on 03/02/2018.
 */

//this class will create a Message object with the needed properties
//it will also save the message to realm and save chat if not exists before

public class MessageCreator {
    private final Model model;
    private final Context context;
    private final int type;
    private final String text;


    private MessageCreator(Builder builder) {
        model = builder.model;
        context = builder.context;
        type = builder.type;
        text = builder.text;
    }


    public static Builder builder(Model model, int type) {
        return new Builder(model, type);
    }

    public Model getUser() {
        return model;
    }

    public Context getContext() {
        return context;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }




    public static class Builder {
        private final Model model;
        private Context context;
        private int type;
        private String text;

        public Builder(Model model, int type) {
            this.model = model;
            this.type = type;
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder type(int type) {
            this.type = type;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Message build() {
            try {
                String receiverUid = model.getUid();
                //messageId
                String pushKey = UUID.randomUUID().toString();
                Message message = new Message();
                message.setFromId(receiverUid);
                message.setToId(receiverUid);
                message.setChatId(receiverUid);
                message.setType(type);
                //set the message time locally
                // this will replaced when sending to firebase database with the server time
                message.setTimestamp(String.valueOf(new Date().getTime()));
                //initial state is pending
                message.setMessageStat(MessageStat.PENDING);
                message.setMessageId(pushKey);

                switch (type) {
                    case MessageType.SENT_TEXT:
                    case MessageType.RECEIVED_TEXT:
                        message.setContent(text);
                        break;

                    case MessageType.REPLY_STATUS:
                        break;

                }
                //save the message to realm
                RealmHelper.getInstance().saveObjectToRealm(message);
                //save chat if this the first message in this chat
                RealmHelper.getInstance().saveChatIfNotExists(message, model);
                return message;

            } catch (Exception e) {
                return null;
            }
        }
    }
}
