package com.ashomok.chatoflegends.activities

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ashomok.chatoflegends.R
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.reactivex.disposables.CompositeDisposable
import java.util.UUID


abstract class BaseActivity : AppCompatActivity() {
    private val disposables = CompositeDisposable()
    private var isKeyboardShowing = false

    protected fun loadBannerAd(adView: AdView, ad_enabled: Boolean, collapsible: Boolean) {

        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                adView.visibility = View.GONE
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                adView.rootView.viewTreeObserver.addOnGlobalLayoutListener {
                    val r = Rect()
                    adView.rootView.getWindowVisibleDisplayFrame(r)
                    val screenHeight: Int = adView.rootView.height

                    // r.bottom is the position above soft keypad or device button.
                    // if keypad is shown, the r.bottom is smaller than that before.
                    val keypadHeight = screenHeight - r.bottom
                    if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                        // keyboard is opened
                        if (!isKeyboardShowing) {
                            isKeyboardShowing = true
                            adView.visibility = View.GONE
                        }
                    } else {
                        // keyboard is closed
                        if (isKeyboardShowing) {
                            isKeyboardShowing = false
                            if (resources.getBoolean(R.bool.is_chat_ad_enabled)) {
                                adView.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
        if (ad_enabled) {
            // Create an extra parameter that aligns the bottom of the expanded ad to
            // the bottom of the bannerView.
            val extras = Bundle()
            if (collapsible) {
                extras.putString("collapsible", "bottom")
                extras.putString("collapsible_request_id", UUID.randomUUID().toString())
            }
            val adRequest = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
            adView.loadAd(adRequest)
        }
    }

    protected fun loadInterstitialAd(activity: Activity, ad_enabled: Boolean) {
        if (ad_enabled) {
            InterstitialAd.load(
                this,
                getString(R.string.interstitial_ad_id),
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        super.onAdLoaded(interstitialAd)
                        interstitialAd.show(activity)
                    }
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}