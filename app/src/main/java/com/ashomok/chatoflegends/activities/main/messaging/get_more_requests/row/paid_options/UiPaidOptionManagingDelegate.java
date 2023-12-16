// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.paid_options;

import android.app.Activity;
import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.GetMoreRequestsActivity;
import com.ashomok.chatoflegends.utils.LogHelper;
import com.ashomok.chatoflegends.utils.NetworkHelper;
import com.ashomok.chatoflegends.utils.Util;
import com.ashomok.chatoflegends.activities.main.messaging.billing.BillingViewModelImpl;

import java.util.Objects;


/**
 * Implementations of this abstract class are responsible to render UI and handle user actions for
 * promo rows to render RecyclerView
 */

public abstract class UiPaidOptionManagingDelegate {

    private static final String TAG = LogHelper.makeLogTag(UiPaidOptionManagingDelegate.class);
    private final BillingViewModelImpl billingViewModelImpl;

    private final Activity context;

    protected UiPaidOptionManagingDelegate(BillingViewModelImpl billingViewModelImpl, Activity context) {
        this.billingViewModelImpl = billingViewModelImpl;
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    private boolean isOnline() {
        return NetworkHelper.isConnected(context);
    }

    //todo duplicated code - move to util class
    private void checkConnection() {
        if (null != context) {
            if (!isOnline()) {
                ((GetMoreRequestsActivity) context).showError(R.string.no_internet_connection);
            }
        }
    }

    public void onRowClicked(ProductDetails productDetails) {
        checkConnection();
        if (null != context && null != productDetails) {
            billingViewModelImpl.makePurchase(context, productDetails);
            LogHelper.d(UiPaidOptionManagingDelegate.TAG, "starting purchase flow for SkuDetail ");
        }
    }

    public void onBindViewHolder(ProductDetails data, PaidOptionRowViewHolder holder) {
        if (Objects.equals(Util.getUserCountry(context), "br")){
            holder.getPrice().setText("N/A");
        }
        else {
            if (data.getProductType().equals(BillingClient.ProductType.SUBS)) {
                holder.getPrice().setText(data.getSubscriptionOfferDetails()
                        .get(0).getPricingPhases().getPricingPhaseList()
                        .get(0).getFormattedPrice());
            } else if (data.getProductType().equals(BillingClient.ProductType.INAPP)) {
                holder.getPrice().setText(data.getOneTimePurchaseOfferDetails().getFormattedPrice());
            }
        }
    }
}
