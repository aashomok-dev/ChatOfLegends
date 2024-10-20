package com.ashomok.heroai.utils

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDex
import com.aghajari.emojiview.AXEmojiManager
import com.aghajari.emojiview.appleprovider.AXAppleEmojiProvider
import com.ashomok.heroai.R
import com.google.firebase.analytics.FirebaseAnalytics
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by Devlomi on 13/08/2017.
 */
class MyApp : Application(), ActivityLifecycleCallbacks {
    var isHasMovedToForeground = false
        private set
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate() {
        super.onCreate()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        //add support for vector drawables on older APIs
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        //init realm
        Realm.init(this)
        mApp = this

        //init set realm configs
        val realmConfiguration = RealmConfiguration.Builder()
            .schemaVersion(MyMigration.SCHEMA_VERSION.toLong())
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .deleteRealmIfMigrationNeeded()// if migration needed then this methoud will remove the existing database and will create new database
            .build()
        Realm.setDefaultConfiguration(realmConfiguration)
        //init shared prefs manager
        SharedPreferencesManager.init(this)

        initEmojiKeyboard()

        registerActivityLifecycleCallbacks(this)
    }


    private fun initEmojiKeyboard() {
        AXEmojiManager.install(this, AXAppleEmojiProvider(this))
        val bgColor = ContextCompat.getColor(this, R.color.bgColor)
        val accentColor = ContextCompat.getColor(this, R.color.colorAccent)
        AXEmojiManager.getEmojiViewTheme().footerBackgroundColor = bgColor
        AXEmojiManager.getEmojiViewTheme().categoryColor = bgColor
        AXEmojiManager.getEmojiViewTheme().backgroundColor = bgColor
        AXEmojiManager.getEmojiViewTheme().selectedColor = accentColor
        AXEmojiManager.getStickerViewTheme().categoryColor = bgColor
        AXEmojiManager.getStickerViewTheme().backgroundColor = bgColor
        AXEmojiManager.getStickerViewTheme().selectedColor = accentColor
    }

    //to run multi dex
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        isHasMovedToForeground = ++activityReferences == 1 && !isActivityChangingConfigurations
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            SharedPreferencesManager.setLastActive(System.currentTimeMillis())
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        private var mApp: MyApp? = null

        @JvmStatic
        var currentChatId = ""
            private set
        var isChatActivityVisible = false
            private set

//        @JvmStatic
//        var isBaseActivityVisible = false
//            private set

        @JvmStatic
        fun chatActivityResumed(chatId: String) {
            isChatActivityVisible = true
            currentChatId = chatId
        }

        @JvmStatic
        fun chatActivityPaused() {
            isChatActivityVisible = false
            currentChatId = ""
        }

//        fun baseActivityResumed() {
//            isBaseActivityVisible = true
//        }
//
//        fun baseActivityPaused() {
//            isBaseActivityVisible = false
//        }

        @JvmStatic
        fun context(): Context {
            return mApp!!.applicationContext
        }
    }
}