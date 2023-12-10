package com.ashomok.heroai.activities.main.rate_app;

import android.content.Context;
import android.content.SharedPreferences;

import com.ashomok.heroai.R;

import javax.inject.Inject;


/**
 * Created by iuliia on 10/5/16.
 */

public class RateAppAsker implements OnNeverAskReachedListener {

    /**
     * Ask to rate app if the app was used RATE_APP_COUNT times
     */
    public static final int RATE_APP_COUNT = 20;
    public static final int NEVER_ASK = -1;
    private final Context context;
    private final SharedPreferences sharedPreferences;


    @Inject
    public RateAppAsker(SharedPreferences sharedPreferences, Context context) {
        this.sharedPreferences = sharedPreferences;
        this.context = context;
    }

    public void init(RateAppAskerCallback callback) {
        int timesAppWasUsed = sharedPreferences.getInt(context.getString(R.string.times_app_was_used), 0);

        if (RateAppAsker.NEVER_ASK != timesAppWasUsed) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (RateAppAsker.RATE_APP_COUNT <= timesAppWasUsed) {
                askToRate(callback);
                editor.putInt(context.getString(R.string.times_app_was_used), 0);
            } else {

                ++timesAppWasUsed;
                editor.putInt(context.getString(R.string.times_app_was_used), timesAppWasUsed);
            }
            editor.apply();
        }
    }

    private void askToRate(RateAppAskerCallback callback) {
        RateAppDialogFragment rateAppDialogFragment = RateAppDialogFragment.newInstance();
        rateAppDialogFragment.setOnStopAskListener(this);
        callback.showRateAppDialog(rateAppDialogFragment);
    }

    @Override
    public void onStopAsk() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.times_app_was_used), RateAppAsker.NEVER_ASK);
        editor.apply();
    }
}
