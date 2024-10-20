package com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.free_options.option_delegates;

import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.GetMoreRequestsActivity;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.free_options.UiFreeOptionManagingDelegate;
import com.ashomok.chatoflegends.activities.main.messaging.update_to_premium.ChatRequestsStateModel;
import com.ashomok.chatoflegends.utils.LogHelper;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import javax.inject.Inject;

public class WatchVideoRewardedIntestitialDelegate extends UiFreeOptionManagingDelegate {
    public static final String TAG = LogHelper.makeLogTag(WatchVideoRewardedIntestitialDelegate.class);
    public static final String ID = "watch_video";
    private final GetMoreRequestsActivity activity;
    private final ChatRequestsStateModel chatRequestsStateModel;
    private final String rewardedAdId;
    private RewardedInterstitialAd rewardedInterstitialAd;

    @Inject
    public WatchVideoRewardedIntestitialDelegate(GetMoreRequestsActivity activity, ChatRequestsStateModel chatRequestsStateModel, String rewardedAdsId) {
        super(activity);
        this.activity = activity;
        this.chatRequestsStateModel = chatRequestsStateModel;
        rewardedAdId = rewardedAdsId;
        preloadAd();
    }

    public void preloadAd() {
        RewardedInterstitialAd.load(activity, rewardedAdId,
                new AdRequest.Builder().build(), new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedInterstitialAd ad) {
                        rewardedInterstitialAd = ad;
                        rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                LogHelper.e(TAG, "Ad dismissed fullscreen content.");
                                rewardedInterstitialAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                LogHelper.e(TAG, "Ad failed to show fullscreen content.");
                                rewardedInterstitialAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        LogHelper.d(TAG, loadAdError.toString());
                        rewardedInterstitialAd = null;
                    }
                });
    }

    @Override
    protected void startTask() {
        if (null != rewardedInterstitialAd) {
            rewardedInterstitialAd.show(activity, rewardItem -> LogHelper.d(TAG, "The user earned the reward."));
            onTaskDone(chatRequestsStateModel);
            // Called when ad is dismissed.
            // Set the ad reference to null so you don't show the ad a second time.
            LogHelper.d(TAG, "Ad dismissed fullscreen content.");
            rewardedInterstitialAd = null;
            // Preload the next video ad.
            preloadAd();
        } else {
            activity.showWarning(R.string.not_loaded_yet);
            LogHelper.d(TAG, "The rewarded ad wasn't ready yet.");
        }
    }
}
