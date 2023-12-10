package com.ashomok.heroai.activities.main.messaging.update_to_premium;

import static com.ashomok.heroai.activities.main.messaging.GPTChatActivity.DAILY_LIMIT;
import static com.ashomok.heroai.utils.Util.getUserCountry;
import static com.ashomok.lullabies.billing_kotlin.AppSku.PREMIUM_MONTHLY_SKU_ID;
import static com.ashomok.lullabies.billing_kotlin.AppSku.PREMIUM_YEARLY_SKU_ID;
import static com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_INFINITE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.ProductDetails;
import com.ashomok.heroai.R;
import com.ashomok.heroai.activities.settings.Settings;
import com.ashomok.heroai.utils.InfoSnackbarUtil;
import com.ashomok.heroai.utils.LogHelper;
import com.ashomok.heroai.utils.NetworkHelper;
import com.ashomok.heroai.utils.Util;
import com.ashomok.heroai.activities.main.messaging.billing.BillingViewModelImpl;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

/**
 * Created by iuliia on 1/29/18.
 */
public class UpdateToPremiumActivity extends AppCompatActivity {
    private static final String TAG = LogHelper.makeLogTag(UpdateToPremiumActivity.class);

    private View mRootView;

    private BillingViewModelImpl billingViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_to_premium);
        mRootView = findViewById(android.R.id.content);
        if (null == mRootView) {
            mRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        }
        initToolbar();
        initFeaturesList();

        TextView unlimitedExplanation = findViewById(R.id.unlimited_explanation);
        unlimitedExplanation.setText(getResources().getString(R.string.unlimited_explanation, String.valueOf(DAILY_LIMIT)));

        TextView brazilianRegulationMessage = findViewById(R.id.brazilian_regulation_message);
        if (Objects.equals(getUserCountry(this), "br")){
            brazilianRegulationMessage.setVisibility(View.VISIBLE);
        }

        checkConnection();
        billingViewModel = new BillingViewModelImpl(getApplication(), ChatRequestsStateModel.Companion.getInstance(getApplication()));
        billingViewModel.getSubscriptionSkuDetailsListLiveData()
                .observe(this, productDetails -> {
                    if (null != productDetails && !productDetails.isEmpty()) {
                        for (ProductDetails item : productDetails) {
                            if (item.getProductId().equals(PREMIUM_MONTHLY_SKU_ID)) {
                                initPremiumMonthRow(item);
                            } else if (item.getProductId().equals(PREMIUM_YEARLY_SKU_ID)) {
                                initPremiumYearRow(item);
                            }
                        }
                    } else {
                        showWarning(R.string.prices_not_loaded_yet);
                        LogHelper.i(UpdateToPremiumActivity.TAG, "empty In App Billing SKU list size");
                    }
                });
        billingViewModel.getRequestsLiveData().observe(this,
                requests -> updateView(REQUESTS_INFINITE == requests));
        billingViewModel.getMessages().observe(this, this::showInfo);
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


    private void checkConnection() {
        if (!NetworkHelper.isConnected(this)) {
            showError(R.string.no_internet_connection);
        }

    }

    @SuppressLint("DefaultLocale")
    public void initPremiumYearRow(ProductDetails item) {
        View oneYearLayout = findViewById(R.id.one_year_subscription);
        TextView oneYearPrice = findViewById(R.id.one_year_price);
        if (!Util.getUserCountry(this).equals("br")) {
            oneYearPrice.setText(item.getSubscriptionOfferDetails()
                    .get(0).getPricingPhases().getPricingPhaseList()
                    .get(0).getFormattedPrice());
            TextView pricePerMonth = findViewById(R.id.price_per_month);
            String subTitle = getString(R.string.price_per_month,
                    item.getSubscriptionOfferDetails()
                            .get(0).getPricingPhases().getPricingPhaseList()
                            .get(0).getPriceCurrencyCode(),
                    String.format("%.2f", (double) item.getSubscriptionOfferDetails()
                            .get(0).getPricingPhases().getPricingPhaseList()
                            .get(0).getPriceAmountMicros() / 12000000));
            pricePerMonth.setText(subTitle);
            oneYearLayout.setOnClickListener(view -> onPaidOptionClicked(item));
        }
        else {
            oneYearPrice.setText("N/A");
        }
    }

    @SuppressLint("DefaultLocale")
    public void initPremiumMonthRow(ProductDetails item) {
        View oneMonthLayout = findViewById(R.id.one_month_subscription);
        TextView oneMonthPrice = findViewById(R.id.one_month_price);
        if (!Objects.equals(Util.getUserCountry(this), "br")) {
            oneMonthPrice.setText(item.getSubscriptionOfferDetails()
                    .get(0).getPricingPhases().getPricingPhaseList()
                    .get(0).getFormattedPrice());
            oneMonthLayout.setOnClickListener(view -> onPaidOptionClicked(item));
        }
        else {
            oneMonthPrice.setText("N/A");
        }
    }

    private void onPaidOptionClicked(ProductDetails item) {
        checkConnection();
        billingViewModel.makePurchase(this, item);
    }

    public void updateView(boolean isPremium) {
        View truePremium = findViewById(R.id.premium_layout);
        View falsePremium = findViewById(R.id.propose_premium_layout);
        truePremium.setVisibility(isPremium ? View.VISIBLE : View.GONE);
        falsePremium.setVisibility(isPremium ? View.GONE : View.VISIBLE);
        Settings.isPremium = isPremium;
    }

    private void initFeaturesList() {
        FeaturesListAdapter featuresListAdapter = new FeaturesListAdapter(FeaturesList.getList(), this);
        RecyclerView recyclerView = findViewById(R.id.premium_features_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(featuresListAdapter);
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
                    collapsingToolbar.setTitle(getResources().getString(R.string.update_to_premium));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");//careful there should a space between double quote otherwise it wont work
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

    public void showWarning(int infoMessageRes) {
        InfoSnackbarUtil.showWarning(infoMessageRes, mRootView);
    }

    public void showInfo(String message) {
        InfoSnackbarUtil.showInfo(message, mRootView);
    }
}

