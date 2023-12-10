package com.ashomok.heroai.activities.main.messaging.get_more_requests.row.free_options.option_delegates;

import androidx.annotation.NonNull;

import com.ashomok.heroai.R;
import com.ashomok.heroai.activities.main.messaging.get_more_requests.GetMoreRequestsActivity;
import com.ashomok.heroai.activities.main.messaging.get_more_requests.row.free_options.UiFreeOptionManagingDelegate;
import com.ashomok.heroai.activities.main.messaging.update_to_premium.ChatRequestsStateModel;
import com.ashomok.heroai.utils.LogHelper;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import javax.inject.Inject;

/**
 * Created by iuliia on 3/5/18.
 */

//todo Forward lifecycle events https://developers.google.com/admob/android/rewarded-video -
// impossible now due to no delegate from activity - minor bug
public class WatchVideoRewardedDelegate extends UiFreeOptionManagingDelegate {
    public static final String TAG = LogHelper.makeLogTag(WatchVideoRewardedDelegate.class);
    public static final String ID = "watch_video";
    private final GetMoreRequestsActivity activity;
    private final ChatRequestsStateModel chatRequestsStateModel;
    private final String rewardedAdId;
    private RewardedAd rewardedAd;

    @Inject
    public WatchVideoRewardedDelegate(GetMoreRequestsActivity activity, ChatRequestsStateModel chatRequestsStateModel, String rewardedAdsId) {
        super(activity);
        this.activity = activity;
        this.chatRequestsStateModel = chatRequestsStateModel;
        rewardedAdId = rewardedAdsId;

        preloadAd();
    }

    private void preloadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(activity, rewardedAdId, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error.
                LogHelper.d(WatchVideoRewardedDelegate.TAG, loadAdError.toString());
                rewardedAd = null;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                LogHelper.d(WatchVideoRewardedDelegate.TAG, "Ad was loaded.");
            }
        });
    }


    @Override
    protected void startTask() {
        if (null != rewardedAd) {
            rewardedAd.show(activity, rewardItem -> {
                // Handle the reward.
                LogHelper.d(WatchVideoRewardedDelegate.TAG, "The user earned the reward.");
            });

            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    onTaskDone(chatRequestsStateModel);
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    LogHelper.d(WatchVideoRewardedDelegate.TAG, "Ad dismissed fullscreen content.");
                    rewardedAd = null;
                    // Preload the next video ad.
                    preloadAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.
                    LogHelper.i(WatchVideoRewardedDelegate.TAG, "Ad failed to show fullscreen content.");
                    rewardedAd = null;
                    // Preload the next video ad.
                    preloadAd();
                }

                @Override
                public void onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    LogHelper.d(WatchVideoRewardedDelegate.TAG, "Ad recorded an impression.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    LogHelper.d(WatchVideoRewardedDelegate.TAG, "Ad showed fullscreen content.");
                }
            });
        } else {
            activity.showWarning(R.string.not_loaded_yet);
            LogHelper.d(WatchVideoRewardedDelegate.TAG, "The rewarded ad wasn't ready yet.");
        }
    }
}
