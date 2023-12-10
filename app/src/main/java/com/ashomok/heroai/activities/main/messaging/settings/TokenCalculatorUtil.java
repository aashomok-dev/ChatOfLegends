package com.ashomok.heroai.activities.main.messaging.settings;

import com.ashomok.heroai.model.realms.Model;
import com.ashomok.heroai.utils.LogHelper;
import com.ashomok.heroai.utils.SharedPreferencesManager;

import java.math.BigDecimal;
import java.util.Map;

public class TokenCalculatorUtil {

    public static final String EXTRA_CURRENT_USER = LogHelper.makeLogTag(SettingsActivity.class);
    public static final int DEFAULT_TEMPERATURE_INT = 10;
    public static final int MIN_OUTGOING_MESSAGE_LENGTH = 10;
    public static final int MIN_HISTORY_BUFFER_SIZE = 1;
    public static final int MIN_GPT_TOKENS_ALLOWED = 10;
    public static int DEFAULT_OUTGOING_MESSAGE_LENGTH = 500;
    public static int DEFAULT_HISTORY_BUFFER_SIZE = 3;
    public static int DEFAULT_GPT_TOKENS_ALLOWED = 150;
    public static String TAG_MESSAGE_MAX_LENGTH = "messageMaxLength";
    public static String TAG_TEMPERATURE = "Temperature";
    public static String TAG_HISTORY_BUFFER_SIZE = "HistoryBufferSize";
    public static String TAG_GPT_TOKENS_ALLOWED = "GptTokensAllowed";

    public static int calculateMessageCost(Model gptModel) {
        Map<String, Object> allValuesMap = SharedPreferencesManager.getAllValues();
        int gptTokensAllowed = (int) allValuesMap.getOrDefault(gptModel.getUid() + "_" + TAG_GPT_TOKENS_ALLOWED, DEFAULT_GPT_TOKENS_ALLOWED);
        int historyBufferSize = (int) allValuesMap.getOrDefault(gptModel.getUid() + "_" + TAG_HISTORY_BUFFER_SIZE, DEFAULT_HISTORY_BUFFER_SIZE);
        int messageMaxLength = (int) allValuesMap.getOrDefault(gptModel.getUid() + "_" + TAG_MESSAGE_MAX_LENGTH, DEFAULT_OUTGOING_MESSAGE_LENGTH);
        return gptModel.getTokenNeeds() * calculatePointsPrice(TAG_GPT_TOKENS_ALLOWED, gptTokensAllowed, DEFAULT_GPT_TOKENS_ALLOWED)
                * calculatePointsPrice(TAG_HISTORY_BUFFER_SIZE, historyBufferSize, DEFAULT_HISTORY_BUFFER_SIZE)
                * calculatePointsPrice(TAG_MESSAGE_MAX_LENGTH, messageMaxLength, DEFAULT_OUTGOING_MESSAGE_LENGTH);
    }

    public static float convertToFloat(int initialSettingOptionValue) {
        float f = initialSettingOptionValue / 10f;
        return round(f, 2);
    }

    /**
     * Round to certain number of decimals
     *
     * @param d
     * @param decimalPlace
     * @return
     */
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static int calculatePointsPrice(String fragmentTag, int progress, int defaultSettingOptionValue) {
        if (fragmentTag.equals(TAG_TEMPERATURE) || progress <= defaultSettingOptionValue) {
            return 1;
        } else return Math.round(1 + progress / defaultSettingOptionValue);
    }
}
