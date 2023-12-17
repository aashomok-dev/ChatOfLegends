package com.ashomok.chatoflegends.activities.main.messaging.get_more_requests;

import static com.ashomok.chatoflegends.activities.main.messaging.GPTChatActivity.DAILY_LIMIT;
import static com.ashomok.chatoflegends.activities.settings.Settings.isPremium;
import static com.ashomok.chatoflegends.utils.Util.getUserCountry;
import static com.ashomok.lullabies.billing_kotlin.AppSku.PREMIUM_MONTHLY_SKU_ID;
import static com.ashomok.lullabies.billing_kotlin.AppSku.PREMIUM_YEARLY_SKU_ID;
import static com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_100_SKU_ID;
import static com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_10_SKU_ID;
import static com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_INFINITE;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.ProductDetails;
import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.activities.main.messaging.billing.BillingViewModelImpl;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.free_options.PromoListFreeOptions;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.free_options.PromoListFreeOptionsAdapter;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.free_options.UiDelegatesFactoryFree;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.free_options.UiFreeOptionManagingDelegate;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.free_options.option_delegates.WatchVideoRewardedDelegate;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options.PromoListPaidOptionsAdapter;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options.UiDelegatesFactoryPaid;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options.UiPaidOptionManagingDelegate;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options.option_delegates.Batch100Delegate;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options.option_delegates.Batch5Delegate;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options.option_delegates.SubscriptionMonthlyDelegate;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options.option_delegates.SubscriptionYearlyDelegate;
import com.ashomok.chatoflegends.activities.main.messaging.update_to_premium.ChatRequestsStateModel;
import com.ashomok.chatoflegends.utils.InfoSnackbarUtil;
import com.ashomok.chatoflegends.utils.LogHelper;
import com.ashomok.chatoflegends.utils.NetworkHelper;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by iuliia on 3/2/18.
 */

public class GetMoreRequestsActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.makeLogTag(GetMoreRequestsActivity.class);
    private PromoListPaidOptionsAdapter promoListPaidOptionsAdapter;
    private View mRootView;
    private ChatRequestsStateModel chatRequestStateModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_more_requests);
        mRootView = findViewById(android.R.id.content);
        if (null == mRootView) {
            mRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        }
        initToolbar();

        chatRequestStateModel = ChatRequestsStateModel.Companion.getInstance(getApplication());

        initPromoListFreeOptions();
        initPromoListPaidOptions();

        TextView unlimitedExplanation = findViewById(R.id.unlimited_explanation);
        unlimitedExplanation.setText(getResources().getString(R.string.unlimited_explanation, String.valueOf(DAILY_LIMIT)));

        TextView brazilianRegulationMessage = findViewById(R.id.brazilian_regulation_message);
        if (Objects.equals(getUserCountry(this), "br")) {
            brazilianRegulationMessage.setVisibility(View.VISIBLE);
        }
    }

    private void checkConnection() {
        if (!NetworkHelper.isConnected(this)) {
            showError(R.string.no_internet_connection);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateRequestsCounterView(Integer requests) {
        TextView youHaveRequestsTextView = findViewById(R.id.you_have_requests_text);
        isPremium = (REQUESTS_INFINITE == requests);

        if (isPremium) {
            findViewById(R.id.get_free_more).setVisibility(View.GONE);
            youHaveRequestsTextView.setText(getString(R.string.you_have_unlimited_tokens));
        } else {
            findViewById(R.id.get_free_more).setVisibility(View.VISIBLE);
            youHaveRequestsTextView.setText(getString(R.string.you_have_n_tokens, String.valueOf(requests)));
        }
    }

    public void updatePaidOption(List<ProductDetails> dataList) {
        LogHelper.d(GetMoreRequestsActivity.TAG, "updatePaidOption called");
        promoListPaidOptionsAdapter.getDataList().addAll(dataList);
        LinkedHashSet<ProductDetails> set = new LinkedHashSet<>(promoListPaidOptionsAdapter.getDataList());
        promoListPaidOptionsAdapter.getDataList().clear();
        promoListPaidOptionsAdapter.getDataList().addAll(set.stream().sorted(Comparator.comparing(ProductDetails::getTitle)).collect(Collectors.toList()));
        promoListPaidOptionsAdapter.notifyDataSetChanged();
    }

    private void initPromoListFreeOptions() {
        String rewardedAdsId = getResources().getString(R.string.rewarded_ad_id);
        Map<String, UiFreeOptionManagingDelegate> uiDelegatesFree = new HashMap<>();
        uiDelegatesFree.put(WatchVideoRewardedDelegate.ID, new WatchVideoRewardedDelegate(this,
                chatRequestStateModel, rewardedAdsId));
        PromoListFreeOptionsAdapter promoListFreeOptionsAdapter =
                new PromoListFreeOptionsAdapter(PromoListFreeOptions.getList(),
                new UiDelegatesFactoryFree(uiDelegatesFree));
        RecyclerView recyclerView = findViewById(R.id.promo_list_free_options);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(promoListFreeOptionsAdapter);
    }

    private void initPromoListPaidOptions() {
        checkConnection();
        BillingViewModelImpl billingViewModelImpl = new BillingViewModelImpl(getApplication(),
                chatRequestStateModel);
        billingViewModelImpl.getInAppSkuDetailsListLiveData()
                .observe(this, productDetails -> {
                    if (null != productDetails) {
                        if (!productDetails.isEmpty()) {
                            for (ProductDetails item : productDetails) {
                                if (item.getProductId().equals(REQUESTS_10_SKU_ID) ||
                                        item.getProductId().equals(REQUESTS_100_SKU_ID)) {
                                    updatePaidOption(productDetails);
                                }
                            }
                        } else {
                            showWarning(R.string.prices_not_loaded_yet);
                            LogHelper.i(GetMoreRequestsActivity.TAG, "empty In App Billing SKU list size");
                        }
                    }
                });
        billingViewModelImpl.getSubscriptionSkuDetailsListLiveData()
                .observe(this, productDetails -> {
                    if (null != productDetails) {
                        if (!productDetails.isEmpty()) {
                            for (ProductDetails item : productDetails) {
                                if (item.getProductId().equals(PREMIUM_MONTHLY_SKU_ID) ||
                                        item.getProductId().equals(PREMIUM_YEARLY_SKU_ID)) {
                                    updatePaidOption(productDetails);
                                }
                            }
                        } else {
                            showWarning(R.string.prices_not_loaded_yet);
                            LogHelper.i(GetMoreRequestsActivity.TAG, "empty In App Billing SKU list size");
                        }
                    }
                });
        billingViewModelImpl.getMessages().observe(this, this::showInfo);
        billingViewModelImpl.getRequestsLiveData().observe(this, this::updateRequestsCounterView);

        Map<String, UiPaidOptionManagingDelegate> uiDelegatesPaid = new HashMap<>();
        uiDelegatesPaid.put(REQUESTS_10_SKU_ID, new Batch5Delegate(billingViewModelImpl, this));
        uiDelegatesPaid.put(REQUESTS_100_SKU_ID, new Batch100Delegate(billingViewModelImpl, this));
        uiDelegatesPaid.put(PREMIUM_MONTHLY_SKU_ID, new SubscriptionMonthlyDelegate(billingViewModelImpl, this));
        uiDelegatesPaid.put(PREMIUM_YEARLY_SKU_ID, new SubscriptionYearlyDelegate(billingViewModelImpl, this));
        promoListPaidOptionsAdapter = new PromoListPaidOptionsAdapter(new UiDelegatesFactoryPaid(uiDelegatesPaid));

        RecyclerView recyclerView = findViewById(R.id.promo_list_paid_options);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(promoListPaidOptionsAdapter);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Show CollapsingToolbarLayout Title ONLY when collapsed
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (-1 == scrollRange) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (0 == scrollRange + verticalOffset) {
                    collapsingToolbar.setTitle(getResources().getString(R.string.get_free_tokens)); ////todo
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
    }

    public void showError(int errorMessageRes) {
        InfoSnackbarUtil.showError(errorMessageRes, mRootView);
    }

    public void showInfo(int infoMessageRes) {
        InfoSnackbarUtil.showInfo(infoMessageRes, mRootView);
    }

    public void showInfo(String message) {
        InfoSnackbarUtil.showInfo(message, mRootView);
    }


    public void showWarning(int message) {
        InfoSnackbarUtil.showWarning(message, mRootView);
    }

}
