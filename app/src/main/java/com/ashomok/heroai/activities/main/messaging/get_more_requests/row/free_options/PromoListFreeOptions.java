package com.ashomok.heroai.activities.main.messaging.get_more_requests.row.free_options;

import com.ashomok.heroai.R;
import com.ashomok.heroai.activities.main.messaging.get_more_requests.row.free_options.option_delegates.WatchVideoRewardedIntestitialDelegate;
import com.ashomok.heroai.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iuliia on 3/2/18.
 */

public enum PromoListFreeOptions {
    ;
    public static final String TAG = LogHelper.makeLogTag(PromoListFreeOptions.class);

    public static List<PromoRowFreeOptionData> getList() {
        List<PromoRowFreeOptionData> result = new ArrayList<>();
        result.add(new PromoRowFreeOptionData(
                WatchVideoRewardedIntestitialDelegate.ID, R.drawable.video_black_24dp, R.string.watch_video_ads, R.string.best_choice, 5));
        return result;
    }
}
