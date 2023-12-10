/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ashomok.heroai.activities.main.messaging.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.*
import com.android.billingclient.api.ProductDetails
import com.ashomok.heroai.activities.main.messaging.update_to_premium.ChatRequestsStateModel
import com.ashomok.lullabies.billing_kotlin.AppSku.AUTO_CONSUME_SKUS
import com.ashomok.lullabies.billing_kotlin.AppSku.INAPP_SKUS
import com.ashomok.lullabies.billing_kotlin.AppSku.SUBSCRIPTION_SKUS
import kotlinx.coroutines.GlobalScope
import javax.inject.Inject

/*
   This is used for any business logic, as well as to echo LiveData from the BillingRepository.
*/
class BillingViewModelImpl @Inject constructor(
    application: Application,
    chatRequestsStateModel: ChatRequestsStateModel
) :
    AndroidViewModel(application) {

    companion object {
        val TAG = BillingViewModelImpl::class.java.simpleName
    }

    private val billingRepository: BillingRepository

    init {
        val applicationScope = GlobalScope

        val billingDataSource = BillingDataSource.getInstance(
            application,
            chatRequestsStateModel,
            applicationScope,
            INAPP_SKUS,
            SUBSCRIPTION_SKUS,
            AUTO_CONSUME_SKUS
        )
        billingRepository = BillingRepository.getInstance(
            billingDataSource,
            applicationScope,
            chatRequestsStateModel
        )
    }

    val messages: LiveData<String>
        get() = billingRepository.messages.asLiveData()

    /**
     * Starts a billing flow for purchasing gas.
     * @param activity
     * @return whether or not we were able to start the flow
     */
    private fun buySku(activity: Activity, sku: String) {
        billingRepository.buySku(activity, sku)
    }

    fun makePurchase(activity: Activity, productDetails: ProductDetails) {
        buySku(activity, productDetails.productId)
    }

    fun getRequestsLiveData(): LiveData<Int> {
        return billingRepository.requestsRemaining
    }

    fun getInAppSkuDetailsListLiveData(): MutableLiveData<List<ProductDetails>> {
        return billingRepository.getInAppSkuDetailsListLiveData()
    }

    fun getSubscriptionSkuDetailsListLiveData(): MutableLiveData<List<ProductDetails>> {
        return billingRepository.getSubscriptionsSkuDetailsListLiveData()
    }

    fun consumeTokens(tokensQuantityToConsume: Int) {
        billingRepository.consumeTokens(tokensQuantityToConsume)
    }
}
