package com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options.option_delegates;

import android.app.Activity;

import com.android.billingclient.api.ProductDetails;
import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options.PaidOptionRowViewHolder;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options.UiPaidOptionManagingDelegate;
import com.ashomok.chatoflegends.utils.LogHelper;
import com.ashomok.chatoflegends.utils.Util;
import com.ashomok.chatoflegends.activities.main.messaging.billing.BillingViewModelImpl;

import java.util.Objects;

import javax.inject.Inject;

public class Batch100Delegate extends UiPaidOptionManagingDelegate {
    public static final String TAG = LogHelper.makeLogTag(Batch100Delegate.class);

    private final Activity context;

    @Inject
    public Batch100Delegate(BillingViewModelImpl billingViewModelImpl, Activity context) {
        super(billingViewModelImpl, context);
        this.context = context;
    }

    @Override
    public void onBindViewHolder(ProductDetails data, PaidOptionRowViewHolder holder) {
        super.onBindViewHolder(data, holder);
        holder.getTitle().setText(getContext().getResources().getString(R.string.buy_100_tokens));
        if (!Objects.equals(Util.getUserCountry(context), "br")) {
            String subTitle = getContext().getResources().getString(R.string.price_per_1_in_100,
                    data.getOneTimePurchaseOfferDetails().getPriceCurrencyCode(),
                    String.format("%.2f", (double) data.getOneTimePurchaseOfferDetails().getPriceAmountMicros() / 100000000));
            holder.getSubtitleBottom().setText(subTitle);
        }
    }
}