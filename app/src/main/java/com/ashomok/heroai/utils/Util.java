package com.ashomok.heroai.utils;

import android.content.Context;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;

import java.util.Locale;

/**
 * Created by Devlomi on 01/08/2017.
 */

public enum Util {
    ;

    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    public static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        }
        catch (Exception ignored) { }
        return null;
    }


    //this will highlight the text when user searches for message in chat
    public static Spanned highlightText(String fullText) {
        Spannable wordtoSpan = new SpannableString(fullText);
        wordtoSpan.setSpan(new BackgroundColorSpan(Color.YELLOW), 0, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return wordtoSpan;
    }

}
