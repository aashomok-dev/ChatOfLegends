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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import com.android.billingclient.api.ProductDetails
import com.ashomok.heroai.R
import com.ashomok.heroai.activities.main.messaging.update_to_premium.ChatRequestsStateModel
import com.ashomok.heroai.utils.LogHelper
import com.ashomok.lullabies.billing_kotlin.AppSku.PREMIUM_MONTHLY_SKU_ID
import com.ashomok.lullabies.billing_kotlin.AppSku.PREMIUM_YEARLY_SKU_ID
import com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_100_BATCH_SIZE
import com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_100_SKU_ID
import com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_10_BATCH_SIZE
import com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_10_SKU_ID
import com.ashomok.lullabies.billing_kotlin.AppSku.REQUESTS_INFINITE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


/**
 * The repository uses data from the Billing data source and the game state model together to give
 * a unified version of the state of the game to the ViewModel. It works closely with the
 * BillingDataSource to implement consumable items, premium items, etc.
 */
class BillingRepository private constructor(
    private val billingDataSource: BillingDataSource,
    private val defaultScope: CoroutineScope,
    private val chatRequestsStateModel: ChatRequestsStateModel
) {

    companion object {
        val TAG: String = LogHelper.makeLogTag(BillingRepository::class.simpleName)

        @Volatile
        private var sInstance: BillingRepository? = null

        // Standard boilerplate double check locking pattern for thread-safe singletons.
        @JvmStatic
        fun getInstance(
            billingDataSource: BillingDataSource,
            applicationScope: CoroutineScope,
            chatRequestsStateModel: ChatRequestsStateModel
        ) = sInstance ?: synchronized(this) {
            sInstance ?: BillingRepository(
                billingDataSource,
                applicationScope,
                chatRequestsStateModel
            )
                .also { sInstance = it }
        }
    }

    private val appMessages: MutableSharedFlow<String> = MutableSharedFlow()

    /**
     * Sets up the event that we can use to send messages up to the UI to be used in Snackbars.
     * This collects new purchase events from the BillingDataSource, transforming the known SKU
     * strings into useful String messages, and emitting the messages into the game messages flow.
     */
    private fun postMessagesFromBillingFlow() {
        defaultScope.launch {
            try {
                billingDataSource.getConsumedPurchases().collect { productsQuantity ->
                    for (sku in productsQuantity.first) {

                        when (sku) {
                            REQUESTS_10_SKU_ID -> appMessages.emit(
                                String.format(
                                    chatRequestsStateModel.context.getString(R.string.tokens_acquired),
                                    productsQuantity.second * REQUESTS_10_BATCH_SIZE
                                )
                            )

                            REQUESTS_100_SKU_ID -> appMessages.emit(
                                String.format(
                                    chatRequestsStateModel.context.getString(R.string.tokens_acquired),
                                    productsQuantity.second * REQUESTS_100_BATCH_SIZE
                                )
                            )

                            PREMIUM_MONTHLY_SKU_ID,
                            PREMIUM_YEARLY_SKU_ID -> {
                                // this makes sure that upgrades/downgrades to subscriptions are
                                // reflected correctly in our user interface
                                billingDataSource.refreshPurchases()
                                appMessages.emit(chatRequestsStateModel.context.getString(R.string.message_subscribed))
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                LogHelper.d(TAG, "Collection complete")
            }
            LogHelper.d(TAG, "Collection Coroutine Scope Exited")
        }
    }

    /**
     * Automatic support for upgrading/downgrading subscription.
     * @param activity
     * @param sku
     */
    fun buySku(activity: Activity, sku: String) {
        var oldSku: String? = null
        when (sku) {
            PREMIUM_MONTHLY_SKU_ID -> oldSku = PREMIUM_YEARLY_SKU_ID
            PREMIUM_YEARLY_SKU_ID -> oldSku = PREMIUM_MONTHLY_SKU_ID
        }
        if (oldSku == null) {
            billingDataSource.launchBillingFlow(activity, sku)
        } else {
            billingDataSource.launchBillingFlow(activity, sku, oldSku)
        }
    }

    /**
     * Return Flow that indicates whether the sku is currently purchased.
     *
     * @param sku the SKU to get and observe the value for
     * @return Flow that returns true if the sku is purchased.
     */
    private fun isPurchased(sku: String): Flow<Boolean> {
        return billingDataSource.isPurchased(sku)
    }

    val requestsRemaining: LiveData<Int>
        get() = ocrRequestsCount().asLiveData()

    private fun ocrRequestsCount(): Flow<Int> {
        val ocrRequestCountFlow = chatRequestsStateModel.availableTokensLiveData.asFlow()
        val monthlySubPurchasedFlow = isPurchased(PREMIUM_MONTHLY_SKU_ID)
        val yearlySubPurchasedFlow = isPurchased(PREMIUM_YEARLY_SKU_ID)
        return combine(
            ocrRequestCountFlow,
            monthlySubPurchasedFlow,
            yearlySubPurchasedFlow
        ) { ocrRequestCount, monthlySubPurchased, yearlySubPurchased ->
            when {
                monthlySubPurchased || yearlySubPurchased -> REQUESTS_INFINITE
                else -> ocrRequestCount
            }
        }
    }

    val messages: Flow<String>
        get() = appMessages


    fun getInAppSkuDetailsListLiveData(): MutableLiveData<List<ProductDetails>> {
        return billingDataSource.inAppSkuDetailsListLiveData
    }

    fun getSubscriptionsSkuDetailsListLiveData(): MutableLiveData<List<ProductDetails>> {
        return billingDataSource.subscriptionsSkuDetailsListLiveData
    }

    fun consumeTokens(tokensQuantityToConsume: Int) {
        return billingDataSource.consumeTokens(tokensQuantityToConsume)
    }

    init {
        postMessagesFromBillingFlow()
        // Since both are tied to application lifecycle, we can launch this scope to collect
        // consumed purchases from the billing data source while the app process is alive.
        defaultScope.launch {
            billingDataSource.getConsumedPurchases().collect {
                LogHelper.d(TAG, " billingDataSource.getConsumedPurchases().collect")
                for (sku in it.first) {
                    LogHelper.d(TAG, " it.size" + it.first.size)
                    LogHelper.d(TAG, "sku $sku")
                    var purchaseBatchSize = 0
                    if (sku == REQUESTS_10_SKU_ID) {
                        purchaseBatchSize = REQUESTS_10_BATCH_SIZE * it.second
                    } else if (sku == REQUESTS_100_SKU_ID) {
                        purchaseBatchSize = REQUESTS_100_BATCH_SIZE * it.second
                    }
                    chatRequestsStateModel.incrementTokens(purchaseBatchSize)
                }
            }
        }
    }
}
