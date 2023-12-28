package com.ashomok.chatoflegends.activities.main.messaging

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.ashomok.chatoflegends.R
import com.ashomok.chatoflegends.activities.BaseActivity
import com.ashomok.chatoflegends.activities.about.AboutActivity
import com.ashomok.chatoflegends.activities.main.messaging.billing.BillingViewModelImpl
import com.ashomok.chatoflegends.activities.main.messaging.update_to_premium.ChatRequestsStateModel.Companion.getInstance
import com.ashomok.chatoflegends.activities.main.messaging.update_to_premium.UpdateToPremiumActivity
import com.ashomok.chatoflegends.activities.main.rate_app.RateAppUtils
import com.ashomok.chatoflegends.activities.settings.Settings
import com.ashomok.chatoflegends.model.constants.MessageType
import com.ashomok.chatoflegends.model.realms.Chat
import com.ashomok.chatoflegends.utils.LogHelper
import com.ashomok.chatoflegends.utils.MessageCreator
import com.ashomok.chatoflegends.utils.RealmHelper
import com.ashomok.chatoflegends.utils.SharedPreferencesManager
import com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_INFINITE
import com.google.android.gms.ads.AdView
import com.google.android.material.navigation.NavigationView
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class ChatListActivity : BaseActivity() {

    lateinit var listView: ListView
    lateinit var scAdapter: ChatListAdapter
    lateinit var mDrawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var billingViewModel: BillingViewModelImpl
    private val TAG = LogHelper.makeLogTag(ChatListActivity::class.java)
    lateinit var adView: AdView
    private lateinit var consentInformation: ConsentInformation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_list_activity)

        // Set tag for under age of consent. false means users are not under age
        // of consent.
        val params = ConsentRequestParameters
            .Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                this@ChatListActivity
            ) { loadAndShowError ->
                // Consent gathering failed.
                LogHelper.w(
                    TAG, String.format(
                        "%s: %s",
                        loadAndShowError?.errorCode,
                        loadAndShowError?.message
                    )
                )
                // Consent has been gathered.
            }
        },
            { requestConsentError ->
                // Consent gathering failed.
                LogHelper.w(
                    TAG, String.format(
                        "%s: %s",
                        requestConsentError.errorCode,
                        requestConsentError.message
                    )
                )
            })

        listView = findViewById(R.id.chat_list)
        scAdapter = ChatListAdapter(this, 0)
        listView.adapter = scAdapter
        var chatList = RealmHelper.getInstance().allChats
        SharedPreferencesManager.getModels().entries.stream().forEach { entry ->
            if (chatList.stream().noneMatch { chat: Chat -> chat.chatId == entry.value.uid }) {
                val message = MessageCreator.Builder(entry.value, MessageType.RECEIVED_TEXT)
                    .text(resources.getString(entry.value.intro))
                    .build()
                RealmHelper.getInstance().saveChatIfNotExists(message, entry.value)
            }
        }

        chatList = RealmHelper.getInstance().allChats.sortedBy { m -> m.model.modelNamePretty }.toList()
        scAdapter.addAll(chatList)
        listView.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>, _: View?, position: Int, arg3: Long ->
                val item: Chat = parent.adapter.getItem(position) as Chat
                val intent = Intent(this, GPTChatActivity::class.java)
                intent.putExtra("user", item.model)
                startActivity(intent)
            }
        setUpNavigationDrawer()
        initToolbar()

        billingViewModel = BillingViewModelImpl(
            application, getInstance(
                application
            )
        )
        billingViewModel.getRequestsLiveData().observe(
            this
        ) { requests: Int? ->
            if (null != requests) {
                updateViewForPremium(REQUESTS_INFINITE == requests)
            }
        }

        adView = findViewById(R.id.ad_view)
        loadBannerAd(adView, resources.getBoolean(R.bool.is_chat_list_ad_enabled), true)
    }

    override fun onStart() {
        super.onStart()
        scAdapter.notifyDataSetChanged()
    }

    private fun startUpdateToPremiumActivity() {
        val intent = Intent(this, UpdateToPremiumActivity::class.java)
        startActivity(intent)
    }

    private fun updateViewForPremium(isPremium: Boolean) {
        updateNavigationDrawerForPremium(isPremium)
        LogHelper.d(TAG, "Update UI. Is premium $isPremium")
        Settings.isPremium = isPremium
        adView.visibility =
            if (!isPremium && resources.getBoolean(R.bool.is_chat_list_ad_enabled)) View.VISIBLE else View.GONE
    }

    private fun updateNavigationDrawerForPremium(isPremium: Boolean) {
        if (isPremium) {
            //in header
            val navHeader = navigationView.getHeaderView(0).findViewById<View>(R.id.header_layout)
            val premiumBtn = navHeader.findViewById<View>(R.id.premium_btn)
            premiumBtn.visibility = View.VISIBLE
            premiumBtn.setOnClickListener { view: View? -> startUpdateToPremiumActivity() }

            //in menu
            val navigationMenu = navigationView.menu
            navigationMenu.findItem(R.id.update_to_premium).setTitle(R.string.my_premium)
        }
    }

    private fun setUpNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout)
        // Set up the navigation drawer.
        navigationView = findViewById(R.id.nav_view)
        navigationView.itemIconTintList = null
        setupDrawerContent(navigationView)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.about -> startAboutActivity()
                R.id.update_to_premium -> startUpdateToPremiumActivity()
                R.id.rate_app -> rateApp()
                else -> {}
            }
            // Close the navigation drawer when an item is selected.
            menuItem.isChecked = true
            mDrawerLayout.closeDrawers()
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                return true
            }

            R.id.menu_item_share -> shareClicked()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val navigationMenu: Menu = navigationView.getMenu()
        updateUpdateToPremiumMenuItem(navigationMenu)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun shareClicked() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val res = resources
        val linkToApp =
            "https://play.google.com/store/apps/details?id=" + res.getString(R.string.app_package_name)
        val sharedBody =
            String.format(res.getString(R.string.share_text_message), "message.content", linkToApp)
        val styledText: Spanned = Html.fromHtml(sharedBody, Html.FROM_HTML_MODE_LEGACY)
        sharingIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            String.format(
                res.getString(R.string.that_is_what_app_says),
                res.getString(R.string.app_name)
            )
        )
        sharingIntent.putExtra(Intent.EXTRA_TEXT, styledText)
        startActivity(
            Intent.createChooser(
                sharingIntent,
                res.getString(R.string.send_to)
            )
        )
    }

    private fun rateApp() {
        val rateAppUtils = RateAppUtils()
        rateAppUtils.rate(this)
    }

    private fun startAboutActivity() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    private fun updateUpdateToPremiumMenuItem(navigationMenu: Menu) {
        val updateToPremiumMenuItem = navigationMenu.findItem(R.id.update_to_premium)
        val menuItemText = updateToPremiumMenuItem.title
        val spannableString = SpannableString(menuItemText)
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.orange_500)),
            0,
            spannableString.length,
            0
        )
        updateToPremiumMenuItem.title = spannableString
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.layout_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (0 < supportFragmentManager.backStackEntryCount) {
            super.onBackPressed()
        } else {
            AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.exit_dialog_title))
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                }
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                    ActivityCompat.finishAffinity(this)
                }
                .show()
        }
    }
}