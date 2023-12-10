package com.ashomok.heroai.utils;

import com.ashomok.heroai.model.constants.DBConstants;
import com.ashomok.heroai.model.constants.MessageStat;
import com.ashomok.heroai.model.constants.MessageType;
import com.ashomok.heroai.model.realms.Chat;
import com.ashomok.heroai.model.realms.Message;
import com.ashomok.heroai.model.realms.Model;
import com.ashomok.heroai.model.realms.UnUpdatedStat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by Devlomi on 13/08/2017.
 */

//this class is responsible for all the Realm Database operations
//create,read,update,delete
public class RealmHelper {

    private static RealmHelper instance;
    private final Realm realm;

    //get instance of real
    private RealmHelper() {
        realm = Realm.getDefaultInstance();
    }

    public static RealmHelper getInstance() {

        RealmHelper.instance = new RealmHelper();

        return RealmHelper.instance;
    }

    //save a message
    public void saveObjectToRealm(RealmObject object) {
        realm.beginTransaction();
        if (object instanceof Message)
            realm.copyToRealm(object);
        else
            realm.copyToRealmOrUpdate(object);
        realm.commitTransaction();
    }

    //get all chats ordered
    public List<Chat> getAllChats() {

//        return realm.where(Chat.class).findAll().sort(DBConstants.CHAT_LAST_MESSAGE_TIMESTAMP, Sort.DESCENDING);
        return new ArrayList<>(realm.where(Chat.class).findAll());
    }

    //get certain chat
    public Chat getChat(String id) {
        return realm.where(Chat.class).equalTo(DBConstants.CHAT_ID, id).findFirst();
    }

    //check if chat stored in realm or not
    public boolean isChatStored(String id) {
        return !realm.where(Chat.class).equalTo(DBConstants.CHAT_ID, id).findAll().isEmpty();
    }


    //delete message from realm and delete file if needed
    public void deleteMessageFromRealm(String chatId, String messageId) {

        //check if message is exists
        Message messageToDelete = getMessage(messageId, chatId);

        if (null == messageToDelete) return;

        Chat chat = getChat(chatId);
        if (null != chat)
            //update last message if this is last message in chat
            updateLastMessageForChat(chatId, messageToDelete, chat);

        //delete message from realm
        realm.beginTransaction();
        messageToDelete.deleteFromRealm();
        realm.commitTransaction();
    }

    private void updateLastMessageForChat(String chatId, Message messageToDelete, Chat chat) {
        if (null != chat) {
            //get last message in this chat
            Message lastMessage = chat.getLastMessage();
            //if this is last message in chat then we want to update
            // 'Chat' with new LastMessage (the message before last message)
            if (null != lastMessage && lastMessage.getMessageId().equals(messageToDelete.getMessageId())) {
                RealmResults<Message> messagesInChat = realm.where(Message.class).equalTo(DBConstants.CHAT_ID, chatId).findAll();
                int messagesCount = messagesInChat.size();
                //check if there is more than one message in this chat
                if (1 < messagesCount) {
                    //get the message before the last message (the new message to set it as the last message)
                    Message messageToSetAsLastMessage = messagesInChat.get(messagesCount - 2);
                    //update the chat with the new last message
                    saveLastMessageForChat(messageToSetAsLastMessage);
                } else {
                    //if there are no messages in chat then just update
                    // the timestamp with the last message timestamp to keep the chat order
                    saveChatLastMessageTimestamp(chat, messagesInChat.last().getTimestamp());
                }
            }
        }
    }

    private void saveChatLastMessageTimestamp(Chat chat, String timestamp) {
        realm.beginTransaction();
        chat.setLastMessageTimestamp(timestamp);
        realm.commitTransaction();
    }

    //get certain message
    public Message getMessage(String id) {
        return realm.where(Message.class).equalTo(DBConstants.MESSAGE_ID, id).findFirst();
    }

    public Message getMessage(String messageId, String chatId) {
        if (null == chatId) return getMessage(messageId);
        return realm.where(Message.class).equalTo(DBConstants.MESSAGE_ID, messageId).equalTo(DBConstants.CHAT_ID, chatId).findFirst();
    }


    //get all messages in chat sorted by time
    public RealmResults<Message> getMessagesInChat(String chatId) {
        return realm.where(Message.class).equalTo(DBConstants.CHAT_ID, chatId).findAll().sort(DBConstants.TIMESTAMP);
    }

    //update chat with new last message
    public void saveLastMessageForChat(String chatId, Message message) {
        Chat chat = getChat(chatId);
        if (null == chat)
            return;
        realm.beginTransaction();
        chat.setLastMessage(getMessage(message.getMessageId(), chatId));
        chat.setLastMessageTimestamp(message.getTimestamp());
        realm.copyToRealmOrUpdate(chat);
        realm.commitTransaction();
    }

    public void saveLastMessageForChat(Message message) {
        String chatId = message.getChatId();
        Chat chat = getChat(chatId);
        if (null == chat)
            return;

        realm.beginTransaction();
        chat.setLastMessage(getMessage(message.getMessageId(), chatId));
        chat.setLastMessageTimestamp(message.getTimestamp());
        realm.copyToRealmOrUpdate(chat);
        realm.commitTransaction();

    }

    // if the user started the chat the we already have the user info
    //therefore we will only create a new chat and save the last message
    public void saveChatIfNotExists(Message message, Model model) {
        String chatId = message.getChatId();
        if (!isChatStored(chatId)) {
            Chat chat = new Chat();
            chat.setChatId(chatId);
            chat.setUser(model);
            chat.setLastMessageTimestamp(String.valueOf(new Date().getTime()));
            saveObjectToRealm(chat);
        }
        saveLastMessageForChat(chatId, message);
    }

    public void updateMessageStatLocally(String messageId, int messageStat) {
        Message message = getMessage(messageId);
        if (null == message) return;
        realm.beginTransaction();
        message.setMessageStat(messageStat);
        realm.commitTransaction();
    }

    public RealmResults<Message> getObservableList(String chatId) {
        return realm.where(Message.class).equalTo(DBConstants.CHAT_ID, chatId)
                .notEqualTo(DBConstants.MESSAGE_STAT, MessageStat.READ)
                .findAll();
    }


    //get certain user
    public Model getUser(String uid) {
        return realm.where(Model.class).equalTo(DBConstants.UID, uid).findFirst();
    }

    //check if messages is exists in database
    public boolean isExists(String messageId) {
        return !realm.where(Message.class).equalTo(DBConstants.MESSAGE_ID, messageId).findAll().isEmpty();
    }

    //search for a text message in certain chat with the given query
    public RealmResults<Message> searchForMessage(String chatId, String query) {
        return realm.where(Message.class)
                .equalTo(DBConstants.CHAT_ID, chatId)
                .contains(DBConstants.CONTENT, query, Case.INSENSITIVE)
                .equalTo(DBConstants.TYPE, MessageType.SENT_TEXT)
                .or()
                .equalTo(DBConstants.CHAT_ID, chatId)
                .contains(DBConstants.CONTENT, query, Case.INSENSITIVE)
                .equalTo(DBConstants.TYPE, MessageType.RECEIVED_TEXT)
                .findAll();
    }


    //get not updated messages state to update them
    public RealmResults<UnUpdatedStat> getUnUpdateMessageStat() {
        return realm.where(UnUpdatedStat.class).findAll();
    }


    //delete deleteUnUpdateStat once it's updated
    public void deleteUnUpdateStat(String messageId) {
        realm.where(UnUpdatedStat.class).equalTo(DBConstants.MESSAGE_ID, messageId).findAll().deleteAllFromRealm();
    }

    public void clearChat(String chatId) {
        RealmResults<Message> messages = realm.where(Message.class).equalTo(DBConstants.CHAT_ID, chatId).findAll();
        if (messages.isEmpty()) {
            return;
        }
        realm.beginTransaction();
        messages.deleteAllFromRealm();
        realm.commitTransaction();
    }

}



