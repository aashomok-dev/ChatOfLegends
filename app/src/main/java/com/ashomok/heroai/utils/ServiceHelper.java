package com.ashomok.heroai.utils;

import android.content.Context;
import android.content.Intent;

import com.ashomok.heroai.services.NetworkService;

/**
 * Created by Devlomi on 09/01/2018.
 */

//this will manage service starts and put extras
public enum ServiceHelper {
    ;

    //this will fire a Network request (download or upload request or event a Firebase database operation)
    public static void startNetworkRequest(Context context, String messageId, String chatId) {
        final String action = IntentUtils.INTENT_ACTION_NETWORK_REQUEST;

        Intent intent = new Intent(context, NetworkService.class);
        //set the action to identify the type
        intent.setAction(action);
        intent.putExtra(IntentUtils.EXTRA_MESSAGE_ID, messageId);
        intent.putExtra(IntentUtils.EXTRA_CHAT_ID, chatId);
        context.startService(intent);
    }

    //this will update the received message stat and set it as Received
    public static void startUpdateMessageStatRequest(Context context, String messageId, String myUid, String chatId, int statToBeUpdated) {
        try {
            Intent intent = new Intent(context, NetworkService.class);
            intent.setAction(IntentUtils.INTENT_ACTION_UPDATE_MESSAGE_STATE);
            intent.putExtra(IntentUtils.EXTRA_MESSAGE_ID, messageId);
            intent.putExtra(IntentUtils.EXTRA_CHAT_ID, chatId);
            intent.putExtra(IntentUtils.EXTRA_MY_UID, myUid);
            intent.putExtra(IntentUtils.EXTRA_STAT, statToBeUpdated);
            context.startService(intent);
        }
        catch (Exception e){
            //ignore
        }
    }
}