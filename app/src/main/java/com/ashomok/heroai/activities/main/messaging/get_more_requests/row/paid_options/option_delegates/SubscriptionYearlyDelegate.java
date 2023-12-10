package com.ashomok.heroai.activities.main.messaging.get_more_requests.row.paid_options.option_delegates;

import android.app.Activity;

import com.android.billingclient.api.ProductDetails;
import com.ashomok.heroai.R;
import com.ashomok.heroai.activities.main.messaging.get_more_requests.row.paid_options.PaidOptionRowViewHolder;
import com.ashomok.heroai.activities.main.messaging.get_more_requests.row.paid_options.UiPaidOptionManagingDelegate;
import com.ashomok.heroai.utils.LogHelper;
import com.ashomok.heroai.utils.Util;
import com.ashomok.heroai.activities.main.messaging.billing.BillingViewModelImpl;

import java.util.Objects;

import javax.inject.Inject;

public class SubscriptionYearlyDelegate extends UiPaidOptionManagingDelegate {
    public static final String TAG = LogHelper.makeLogTag(SubscriptionYearlyDelegate.class);
    private final Activity context;

    @Inject
    public SubscriptionYearlyDelegate(BillingViewModelImpl billingViewModelImpl,
                                      Activity context) {
        super(billingViewModelImpl, context);
        this.context = context;
    }

    @Override
    public void onBindViewHolder(ProductDetails data, PaidOptionRowViewHolder holder) {
        super.onBindViewHolder(data, holder);

        holder.getTitle().setText(getContext().getResources().getString(R.string.one_year_premium));
        holder.getSubtitleTop().setText(getContext().getResources().getString(R.string.unlimited_tokens_no_ads));

        if (!Objects.equals(Util.getUserCountry(context), "br")) {
            String subTitle = getContext().getResources().getString(R.string.price_per_month,
                    data.getSubscriptionOfferDetails()
                            .get(0).getPricingPhases().getPricingPhaseList()
                            .get(0).getPriceCurrencyCode(),
                    String.format("%.2f", (double) data.getSubscriptionOfferDetails()
                            .get(0).getPricingPhases().getPricingPhaseList()
                            .get(0).getPriceAmountMicros() / 12000000));
            holder.getSubtitleBottom().setText(subTitle);
        }
    }
}
