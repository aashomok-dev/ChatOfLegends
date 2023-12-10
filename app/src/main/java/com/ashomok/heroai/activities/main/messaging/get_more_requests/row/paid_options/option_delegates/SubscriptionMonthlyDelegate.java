package com.ashomok.heroai.activities.main.messaging.get_more_requests.row.paid_options.option_delegates;

import android.app.Activity;

import com.android.billingclient.api.ProductDetails;
import com.ashomok.heroai.R;
import com.ashomok.heroai.activities.main.messaging.get_more_requests.row.paid_options.PaidOptionRowViewHolder;
import com.ashomok.heroai.activities.main.messaging.get_more_requests.row.paid_options.UiPaidOptionManagingDelegate;
import com.ashomok.heroai.utils.LogHelper;
import com.ashomok.heroai.activities.main.messaging.billing.BillingViewModelImpl;

import javax.inject.Inject;

public class SubscriptionMonthlyDelegate extends UiPaidOptionManagingDelegate {
    public static final String TAG = LogHelper.makeLogTag(SubscriptionMonthlyDelegate.class);

    @Inject
    public SubscriptionMonthlyDelegate(BillingViewModelImpl billingViewModelImpl,
                                       Activity context) {
        super(billingViewModelImpl, context);
    }

    @Override
    public void onBindViewHolder(ProductDetails data, PaidOptionRowViewHolder holder) {
        super.onBindViewHolder(data, holder);

        holder.getTitle().setText(getContext().getResources().getString(R.string.one_month_premium));
        holder.getSubtitleTop().setText(getContext().getResources().getString(R.string.unlimited_tokens_no_ads));
    }
}
