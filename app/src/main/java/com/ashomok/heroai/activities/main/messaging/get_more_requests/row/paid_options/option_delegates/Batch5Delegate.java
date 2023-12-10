package com.ashomok.heroai.activities.main.messaging.get_more_requests.row.paid_options.option_delegates;

import android.app.Activity;

import com.android.billingclient.api.ProductDetails;
import com.ashomok.heroai.R;
import com.ashomok.heroai.activities.main.messaging.get_more_requests.row.paid_options.PaidOptionRowViewHolder;
import com.ashomok.heroai.activities.main.messaging.get_more_requests.row.paid_options.UiPaidOptionManagingDelegate;
import com.ashomok.heroai.utils.LogHelper;
import com.ashomok.heroai.activities.main.messaging.billing.BillingViewModelImpl;

import javax.inject.Inject;

public class Batch5Delegate extends UiPaidOptionManagingDelegate {
    public static final String TAG = LogHelper.makeLogTag(Batch5Delegate.class);

    @Inject
    public Batch5Delegate(BillingViewModelImpl billingViewModelImpl, Activity context) {
        super(billingViewModelImpl, context);
    }

    @Override
    public void onBindViewHolder(ProductDetails data, PaidOptionRowViewHolder holder) {
        super.onBindViewHolder(data, holder);
        holder.getTitle().setText(getContext().getResources().getString(R.string.buy_10_tokens));
    }
}