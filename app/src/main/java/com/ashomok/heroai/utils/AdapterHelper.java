package com.ashomok.heroai.utils;

import com.ashomok.heroai.R;
import com.ashomok.heroai.model.constants.MessageStat;
import com.ashomok.heroai.model.constants.MessageType;
import com.ashomok.heroai.model.realms.Message;

import java.util.List;

public enum AdapterHelper {
    ;

    //check if all messages are ONLY text
    public static boolean shouldEnableCopyItem(List<Message> selectedItems) {
        boolean returnVal = false;
        for (Message message : selectedItems) {
            if (message.isExists() && message.isTextMessage())
                returnVal = true;
            else return false;
        }
        return returnVal;
    }

    public static int getMessageStatDrawable(int messageStat) {
        switch (messageStat) {
            case MessageStat.PENDING:
                return R.drawable.ic_watch_later_green;

            case MessageStat.SENT:
                return R.drawable.ic_check;

            case MessageStat.RECEIVED:
                return R.drawable.ic_done_all;

            case MessageStat.READ:
                return R.drawable.ic_check_read;

            default:
                return R.drawable.ic_check;

        }
    }


    //check if the list has a media item
    public static boolean shouldHideAllItems(List<Message> selectedItems) {
        final boolean returnVal = false;
        for (Message message : selectedItems) {

            if (!MessageType.isMessageSupported(message.getType()))
                return true;

        }
        return returnVal;
    }
}
