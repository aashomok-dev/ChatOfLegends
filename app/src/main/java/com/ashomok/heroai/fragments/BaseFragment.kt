package com.ashomok.heroai.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

abstract class BaseFragment : Fragment() {
    open var adView: AdView? = null

    abstract fun showAds(): Boolean

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (adView != null && showAds()) adView!!.loadAd(AdRequest.Builder().build())
    }

}