package com.ashomok.heroai.activities.main.messaging.billing

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

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.ashomok.heroai.activities.main.messaging.update_to_premium.ChatRequestsStateModel
import com.ashomok.heroai.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors
import kotlin.math.min

/**
 * The BillingDataSource implements all billing functionality for our test application.
 * Purchases can happen while in the app or at any time while out of the app, so the
 * BillingDataSource has to account for that.
 *
 * Since every SKU (Product ID) can have an individual state, all SKUs have an associated StateFlow
 * to allow their state to be observed.
 *
 * This BillingDataSource knows nothing about the application; all necessary information is either
 * passed into the constructor, exported as observable Flows, or exported through callbacks.
 * This code can be reused in a variety of apps.
 *
 * Beginning a purchase flow involves passing an Activity into the Billing Library, but we merely
 * pass it along to the API.
 *
 * This data source has a few automatic features:
 * 1) It checks for a valid signature on all purchases before attempting to acknowledge them.
 * 2) It automatically acknowledges all known SKUs for non-consumables, and doesn't set the state
 * to purchased until the acknowledgement is complete.
 * 3) The data source will automatically consume skus that are set in knownAutoConsumeSKUs. As
 * SKUs are consumed, a Flow will emit.
 * 4) If the BillingService is disconnected, it will attempt to reconnect with exponential
 * fallback.
 *
 * This data source attempts to keep billing library specific knowledge confined to this file;
 * The only thing that clients of the BillingDataSource need to know are the SKUs used by their
 * application.
 *
 * The BillingClient needs access to the Application context in order to bind the remote billing
 * service.
 *
 * The BillingDataSource can also act as a LifecycleObserver for an Activity; this allows it to
 * refresh purchases during onResume.
 */

private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L // 15 minutes
private const val SKU_DETAILS_REQUERY_TIME = 1000L * 60L * 60L * 4L // 4 hours

class BillingDataSource private constructor(
    application: Application,
    chatRequestsStateModel: ChatRequestsStateModel,
    private val defaultScope: CoroutineScope,
    knownInappSKUs: Array<String>?,
    knownSubscriptionSKUs: Array<String>?,
    autoConsumeSKUs: Array<String>?
) :
    LifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener {

    private var chatTokensStateModel: ChatRequestsStateModel

    // Billing client, connection, cached data
    private val billingClient: BillingClient

    // known SKUs (used to query sku data and validate responses)
    private val knownInappSKUs: List<String>?
    private val knownSubscriptionSKUs: List<String>?

    // SKUs to auto-consume
    private val knownAutoConsumeSKUs: MutableSet<String>

    // how long before the data source tries to reconnect to Google play
    private var reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS

    // when was the last successful SkuDetailsResponse?
    private var skuDetailsResponseTime = -SKU_DETAILS_REQUERY_TIME

    private enum class SkuState {
        SKU_STATE_UNPURCHASED, SKU_STATE_PENDING, SKU_STATE_PURCHASED, SKU_STATE_PURCHASED_AND_ACKNOWLEDGED
    }

    // Flows that are mostly maintained so they can be transformed into observables.
    private val skuStateMap: MutableMap<String, MutableStateFlow<SkuState>> = HashMap()
    private val productsDetailsMap: MutableMap<String, MutableStateFlow<ProductDetails?>> =
        HashMap()

    // Observables that are used to communicate state.
    private val purchaseConsumptionInProcess: MutableSet<Purchase> = HashSet()
    private val newPurchaseFlow = MutableSharedFlow<List<String>>(extraBufferCapacity = 1)
    private val purchaseConsumedFlow = MutableSharedFlow<Pair<List<String>, Int>>()
    private val billingFlowInProcess = MutableStateFlow(false)

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        LogHelper.d(TAG, "onBillingSetupFinished: $responseCode $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                // The billing client is ready. You can query purchases here.
                // This doesn't mean that your app is set up correctly in the console -- it just
                // means that you have a connection to the Billing service.
                reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS
                defaultScope.launch {
                    querySkuDetailsAsync()
                    refreshPurchases()
                }
            }

            else -> retryBillingServiceConnectionWithExponentialBackoff()
        }
    }

    /**
     * This is a pretty unusual occurrence. It happens primarily if the Google Play Store
     * self-upgrades or is force closed.
     */
    override fun onBillingServiceDisconnected() {
        retryBillingServiceConnectionWithExponentialBackoff()
    }

    /**
     * Retries the billing service connection with exponential backoff, maxing out at the time
     * specified by RECONNECT_TIMER_MAX_TIME_MILLISECONDS.
     */
    private fun retryBillingServiceConnectionWithExponentialBackoff() {
        handler.postDelayed(
            { billingClient.startConnection(this@BillingDataSource) },
            reconnectMilliseconds
        )
        reconnectMilliseconds = min(
            reconnectMilliseconds * 2,
            RECONNECT_TIMER_MAX_TIME_MILLISECONDS
        )
    }

    /**
     * Called by initializeFlows to create the various Flow objects we're planning to emit.
     * @param skuList a List<String> of SKUs representing purchases and subscriptions.
    </String> */
    private fun addSkuFlows(skuList: List<String>?) {
        for (sku in skuList!!) {
            val skuState = MutableStateFlow(SkuState.SKU_STATE_UNPURCHASED)
            val details = MutableStateFlow<ProductDetails?>(null)
            details.subscriptionCount.map { count -> count > 0 } // map count into active/inactive flag
                .distinctUntilChanged() // only react to true<->false changes
                .onEach { isActive -> // configure an action
                    if (isActive && (SystemClock.elapsedRealtime() - skuDetailsResponseTime > SKU_DETAILS_REQUERY_TIME)) {
                        skuDetailsResponseTime = SystemClock.elapsedRealtime()
                        Log.v(TAG, "Skus not fresh, requerying")
                        querySkuDetailsAsync()
                    }
                }
                .launchIn(defaultScope) // launch it
            skuStateMap[sku] = skuState
            productsDetailsMap[sku] = details
        }
    }

    /**
     * Creates a Flow object for every known SKU so the state and SKU details can be observed
     * in other layers. The repository is responsible for mapping this data in ways that are more
     * useful for the application.
     */
    private fun initializeFlows() {
        addSkuFlows(knownInappSKUs)
        addSkuFlows(knownSubscriptionSKUs)
    }

    /**
     * This is a flow that is used to observe consumed purchases.
     * @return Flow that contains skus of the consumed purchases.
     */
    fun getConsumedPurchases() = purchaseConsumedFlow.asSharedFlow()

    /**
     * Returns whether or not the user has purchased a SKU. It does this by returning
     * a Flow that returns true if the SKU is in the PURCHASED state and
     * the Purchase has been acknowledged.
     * @return a Flow that observes the SKUs purchase state
     */
    fun isPurchased(sku: String): Flow<Boolean> {
        val skuStateFLow = skuStateMap[sku]!!
        return skuStateFLow.map { skuState -> skuState == SkuState.SKU_STATE_PURCHASED_AND_ACKNOWLEDGED }
    }

    // There's lots of information in SkuDetails, but our app only needs a few things, since our
    // goods never go on sale, have introductory pricing, etc. You can add to this for your app,
    // or create your own class to pass the information across.

    /**
     * Receives the result from [.querySkuDetailsAsync]}.
     *
     * Store the SkuDetails and post them in the [.skuDetailsMap]. This allows other
     * parts of the app to use the [SkuDetails] to show SKU information and make purchases.
     */
    private fun onSkuDetailsResponse(
        billingResult: BillingResult,
        productDetails: List<ProductDetails>?
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.i(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
                if (productDetails.isNullOrEmpty()) {
                    Log.e(
                        TAG,
                        "onSkuDetailsResponse: " +
                                "Found null or empty SkuDetails. " +
                                "Check to see if the SKUs you requested are correctly published " +
                                "in the Google Play Console."
                    )
                } else {
                    for (skuDetails in productDetails) {
                        val sku = skuDetails.productId
                        val detailsMutableFlow = productsDetailsMap[sku]
                        detailsMutableFlow?.tryEmit(skuDetails)
                            ?: LogHelper.e(TAG, "Unknown sku: $sku")
                    }
                }
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR ->
                LogHelper.e(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")

            BillingClient.BillingResponseCode.USER_CANCELED ->
                Log.i(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")

            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED ->
                Log.wtf(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")

            else -> Log.wtf(TAG, "onSkuDetailsResponse: $responseCode $debugMessage")
        }
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            skuDetailsResponseTime = SystemClock.elapsedRealtime()
        } else {
            skuDetailsResponseTime = -SKU_DETAILS_REQUERY_TIME
        }
    }

    /**
     * Calls the billing client functions to query sku details for both the inapp and subscription
     * SKUs. SKU details are useful for displaying item names and price lists to the user, and are
     * required to make a purchase.
     */

    /**
     * This list tells clients what in-app products are available for sale
     */
    val inAppSkuDetailsListLiveData: MutableLiveData<List<ProductDetails>> by lazy {
        MutableLiveData<List<ProductDetails>>()
    }

    /**
     * This list tells clients what in-app products are available for sale
     */
    val subscriptionsSkuDetailsListLiveData: MutableLiveData<List<ProductDetails>> by lazy {
        MutableLiveData<List<ProductDetails>>()
    }

    private fun querySkuDetailsAsync() {
        if (!knownInappSKUs.isNullOrEmpty()) {
            billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        knownInappSKUs.stream().map {
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(it)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()
                        }.collect(Collectors.toList())
                    )

                    .build()
            ) { billingResult, productDetails ->
                onSkuDetailsResponse(
                    billingResult,
                    productDetails
                )
            }
        }

        if (!knownSubscriptionSKUs.isNullOrEmpty()) {
            billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        knownSubscriptionSKUs.stream().map {
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(it)
                                .setProductType(BillingClient.ProductType.SUBS)
                                .build()
                        }.collect(Collectors.toList())
                    ).build()
            ) { billingResult, productDetails ->
                onSkuDetailsResponse(
                    billingResult,
                    productDetails
                )
            }
        }
        val productListInApp: MutableList<QueryProductDetailsParams.Product> = arrayListOf()
        if (!knownInappSKUs.isNullOrEmpty()) {
            for (sku: String in knownInappSKUs) {
                productListInApp.add(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            }
        }

        val paramsInApp = QueryProductDetailsParams.newBuilder().setProductList(productListInApp)
        billingClient.queryProductDetailsAsync(paramsInApp.build()) { billingResult, productDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    productDetailsList.forEach { _ ->
                        CoroutineScope(Job() + Dispatchers.IO).launch {
                            inAppSkuDetailsListLiveData.postValue(productDetailsList)
                        }
                    }
                }

                else -> {
                    LogHelper.i(TAG, billingResult.debugMessage)
                }
            }
        }

        val productListSub: MutableList<QueryProductDetailsParams.Product> = arrayListOf()
        if (!knownSubscriptionSKUs.isNullOrEmpty()) {
            for (sku: String in knownSubscriptionSKUs) {
                val product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(sku)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
                productListSub.add(product)
            }
        }
        val paramsSub = QueryProductDetailsParams.newBuilder().setProductList(productListSub)
        billingClient.queryProductDetailsAsync(paramsSub.build()) { billingResult, productDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    productDetailsList.forEach { _ ->
                        CoroutineScope(Job() + Dispatchers.IO).launch {
                            subscriptionsSkuDetailsListLiveData.postValue(productDetailsList)
                        }
                    }
                }

                else -> {
                    LogHelper.i(TAG, billingResult.debugMessage)
                }
            }
        }
    }

    /*
        GPBLv3 now queries purchases synchronously, simplifying this flow. This only gets active
        purchases.
     */
    fun refreshPurchases() {
        LogHelper.d(TAG, "Refreshing purchases.")
        val queryPurchasesParamsInApp =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        billingClient.queryPurchasesAsync(queryPurchasesParamsInApp)
        { billingResult, purchasesList ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                LogHelper.e(TAG, "Problem getting purchases: " + billingResult.debugMessage)
            } else {
                processPurchaseList(purchasesList, knownInappSKUs)
            }
        }

        val queryPurchasesParamsSubs =
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        billingClient.queryPurchasesAsync(queryPurchasesParamsSubs) { billingResult, purchasesList ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                LogHelper.e(TAG, "Problem getting subscriptions: " + billingResult.debugMessage)
            } else {
                processPurchaseList(purchasesList, knownSubscriptionSKUs)
            }
        }
    }

    /**
     * Calling this means that we have the most up-to-date information for a Sku in a purchase
     * object. This uses the purchase state (Pending, Unspecified, Purchased) along with the
     * acknowledged state.
     * @param purchase an up-to-date object to set the state for the Sku
     */
    private fun setSkuStateFromPurchase(purchase: Purchase) {
        for (purchaseSku in purchase.products) {
            val skuStateFlow = skuStateMap[purchaseSku]
            if (null == skuStateFlow) {
                Log.e(
                    TAG,
                    "Unknown SKU " + purchaseSku + ". Check to make " +
                            "sure SKU matches SKUS in the Play developer console."
                )
            } else {
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PENDING -> skuStateFlow.tryEmit(SkuState.SKU_STATE_PENDING)
                    Purchase.PurchaseState.UNSPECIFIED_STATE -> skuStateFlow.tryEmit(SkuState.SKU_STATE_UNPURCHASED)
                    Purchase.PurchaseState.PURCHASED -> if (purchase.isAcknowledged) {
                        skuStateFlow.tryEmit(SkuState.SKU_STATE_PURCHASED_AND_ACKNOWLEDGED)
                    } else {
                        skuStateFlow.tryEmit(SkuState.SKU_STATE_PURCHASED)
                    }

                    else -> LogHelper.e(TAG, "Purchase in unknown state: " + purchase.purchaseState)
                }
            }
        }
    }

    /**
     * Since we (mostly) are getting sku states when we actually make a purchase or update
     * purchases, we keep some internal state when we do things like acknowledge or consume.
     * @param sku product ID to change the state of
     * @param newSkuState the new state of the sku.
     */
    private fun setSkuState(sku: String, newSkuState: SkuState) {
        val skuStateFlow = skuStateMap[sku]
        skuStateFlow?.tryEmit(newSkuState)
            ?: Log.e(
                TAG,
                "Unknown SKU " + sku + ". Check to make " +
                        "sure SKU matches SKUS in the Play developer console."
            )
    }

    /**
     * Goes through each purchase and makes sure that the purchase state is processed and the state
     * is available through Flows. Verifies signature and acknowledges purchases. PURCHASED isn't
     * returned until the purchase is acknowledged.
     *
     * https://developer.android.com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
     *
     * Developers can choose to acknowledge purchases from a server using the
     * Google Play Developer API. The server has direct access to the user database,
     * so using the Google Play Developer API for acknowledgement might be more reliable.
     *
     * If the purchase token is not acknowledged within 3 days,
     * then Google Play will automatically refund and revoke the purchase.
     * This behavior helps ensure that users are not charged unless the user has successfully
     * received access to the content.
     * This eliminates a category of issues where users complain to developers
     * that they paid for something that the app is not giving to them.
     *
     * If a skusToUpdate list is passed-into this method, any purchases not in the list of
     * purchases will have their state set to UNPURCHASED.
     *
     * @param purchases the List of purchases to process.
     * @param skusToUpdate a list of skus that we want to update the state from --- this allows us
     * to set the state of non-returned SKUs to UNPURCHASED.
     */
    private fun processPurchaseList(purchases: List<Purchase>?, skusToUpdate: List<String>?) {
        val updatedSkus = HashSet<String>()
        if (null != purchases) {
            for (purchase in purchases) {
                for (sku in purchase.products) {
                    val skuStateFlow = skuStateMap[sku]
                    if (null == skuStateFlow) {
                        Log.e(
                            TAG, "Unknown SKU " + sku + ". Check to make " +
                                    "sure SKU matches SKUS in the Play developer console."
                        )
                        continue
                    }
                    updatedSkus.add(sku)
                }
                // Global check to make sure all purchases are signed correctly.
                // This check is best performed on your server.
                val purchaseState = purchase.purchaseState
                if (purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!isSignatureValid(purchase)) {
                        Log.e(
                            TAG, "Invalid signature. Check to make sure your " +
                                    "public key is correct."
                        )
                        continue
                    }
                    // only set the purchased state after we've validated the signature.
                    setSkuStateFromPurchase(purchase)
                    var isConsumable = false
                    defaultScope.launch {
                        for (sku in purchase.products) {
                            if (knownAutoConsumeSKUs.contains(sku)) {
                                isConsumable = true
                            } else {
                                if (isConsumable) {
                                    Log.e(
                                        TAG, "Purchase cannot contain a mixture of consumable" +
                                                "and non-consumable items: " + purchase.products.toString()
                                    )
                                    isConsumable = false
                                    break
                                }
                            }
                        }
                        if (isConsumable) {
                            consumePurchase(purchase)
                            newPurchaseFlow.tryEmit(purchase.products)
                        } else if (!purchase.isAcknowledged) {
                            // acknowledge everything --- new purchases are ones not yet acknowledged
                            billingClient.acknowledgePurchase(
                                AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()
                            ) { billingResult ->
                                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                                    Log.e(
                                        TAG,
                                        "Error acknowledging purchase: ${purchase.products}"
                                    )
                                } else {
                                    // purchase acknowledged
                                    for (sku in purchase.products) {
                                        setSkuState(
                                            sku,
                                            SkuState.SKU_STATE_PURCHASED_AND_ACKNOWLEDGED
                                        )
                                    }
                                }
                            }
                            newPurchaseFlow.tryEmit(purchase.products)
                        }
                    }
                } else {
                    // make sure the state is set
                    setSkuStateFromPurchase(purchase)
                }
            }
        } else {
            LogHelper.d(TAG, "Empty purchase list.")
        }
        // Clear purchase state of anything that didn't come with this purchase list if this is
        // part of a refresh.
        if (null != skusToUpdate) {
            for (sku in skusToUpdate) {
                if (!updatedSkus.contains(sku)) {
                    setSkuState(sku, SkuState.SKU_STATE_UNPURCHASED)
                }
            }
        }
    }

    private suspend fun consumePurchase(purchase: Purchase) {
        // weak check to make sure we're not already consuming the sku
        if (purchaseConsumptionInProcess.contains(purchase)) {
            // already consuming
            return
        }
        purchaseConsumptionInProcess.add(purchase)
        billingClient.consumeAsync(
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        ) { billingResult, _ ->
            purchaseConsumptionInProcess.remove(purchase)
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                LogHelper.d(TAG, "Consumption successful. Emitting sku.")
                defaultScope.launch {
                    purchaseConsumedFlow.emit(Pair(purchase.products, purchase.quantity))
                    LogHelper.d(TAG, "run emit on " + purchase.products)
                    LogHelper.d(TAG, "0 elem is " + purchase.products[0])

                }
                // Since we've consumed the purchase
                for (sku in purchase.products) {
                    setSkuState(sku, SkuState.SKU_STATE_UNPURCHASED)
                }
            } else {
                LogHelper.e(TAG, "Error while consuming: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Launch the billing flow. This will launch an external Activity for a result, so it requires
     * an Activity reference. For subscriptions, it supports upgrading from one SKU type to another
     * by passing in SKUs to be upgraded.
     *
     * @param activity active activity to launch our billing flow from
     * @param sku SKU (Product ID) to be purchased
     * @param upgradeSkusVarargs SKUs that the subscription can be upgraded from
     * @return true if launch is successful
     */
    fun launchBillingFlow(activity: Activity?, sku: String, vararg upgradeSkusVarargs: String) {
        val productDetails = productsDetailsMap[sku]?.value
        if (null != productDetails) {
            val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken
            if (offerToken != null) {

                val productDetailsParamsList =
                    listOf(
                        offerToken.let {
                            ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(it)
                                .build()
                        }
                    )
                val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)

                val upgradeSkus = arrayOf(*upgradeSkusVarargs)
                LogHelper.i(TAG, "upgradeSkus.size: " + upgradeSkus.size)
                defaultScope.launch {
                    val queryPurchasesParamsSubscriptions =
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    billingClient.queryPurchasesAsync(queryPurchasesParamsSubscriptions) { billingResult, purchasesList ->
                        val returnPurchasesList: MutableList<Purchase> = LinkedList()
                        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                            LogHelper.e(
                                TAG,
                                "Problem getting purchases: " + billingResult.debugMessage
                            )
                        } else {
                            for (purchase in purchasesList) {
                                for (skuItem in upgradeSkus) {
                                    for (purchaseSku in purchase.products) {
                                        if (purchaseSku == skuItem) {
                                            returnPurchasesList.add(purchase)
                                        }
                                    }
                                }
                            }
                        }
                        LogHelper.i(TAG, "returnPurchasesList.size: " + returnPurchasesList.size)
                        when (returnPurchasesList.size) {
                            1 -> {
                                val purchase = returnPurchasesList[0]
                                billingFlowParamsBuilder.setSubscriptionUpdateParams(
                                    BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                                        .setOldSkuPurchaseToken(purchase.purchaseToken)
                                        .build()
                                )
                                LogHelper.i(
                                    TAG,
                                    "purchase.purchaseToken: " + purchase.purchaseToken
                                )
                            }

                            0 -> {
                            }

                            else -> Log.e(
                                TAG,
                                returnPurchasesList.size.toString() +
                                        " subscriptions subscribed to. Upgrade not possible."
                            )
                        }

                        val br = billingClient.launchBillingFlow(
                            activity!!,
                            billingFlowParamsBuilder.build()
                        )
                        if (br.responseCode == BillingClient.BillingResponseCode.OK) {
                            defaultScope.launch {
                                billingFlowInProcess.emit(true)
                            }
                        } else {
                            LogHelper.e(TAG, "Billing failed: + " + br.debugMessage)
                        }
                    }
                }
            } else {
                val productDetailsParamsList =
                    listOf(
                        offerToken.let {
                            ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        }
                    )
                val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)

                val queryPurchasesParamsInApp =
                    QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP).build()
                billingClient.queryPurchasesAsync(queryPurchasesParamsInApp) { billingResult, purchasesList ->
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        LogHelper.e(TAG, "Problem getting purchases: " + billingResult.debugMessage)
                    }

                    val br = billingClient.launchBillingFlow(
                        activity!!,
                        billingFlowParamsBuilder.build()
                    )
                    if (br.responseCode == BillingClient.BillingResponseCode.OK) {
                        defaultScope.launch {
                            billingFlowInProcess.emit(true)
                        }
                    } else {
                        LogHelper.e(TAG, "Billing failed: + " + br.debugMessage)
                    }
                }
            }

        } else {
            LogHelper.e(TAG, "SkuDetails not found for: $sku")
        }
    }

    /**
     * Called by the BillingLibrary when new purchases are detected; typically in response to a
     * launchBillingFlow.
     * @param billingResult result of the purchase flow.
     * @param list of new purchases.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> if (null != list) {
                processPurchaseList(list, null)
                return
            } else LogHelper.d(TAG, "Null Purchase List Returned from OK response!")

            BillingClient.BillingResponseCode.USER_CANCELED -> Log.i(
                TAG,
                "onPurchasesUpdated: User canceled the purchase"
            )

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Log.i(
                TAG,
                "onPurchasesUpdated: The user already owns this item"
            )

            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> Log.e(
                TAG,
                "onPurchasesUpdated: Developer error means that Google Play " +
                        "does not recognize the configuration. If you are just getting started, " +
                        "make sure you have configured the application correctly in the " +
                        "Google Play Console. The SKU product ID must match and the APK you " +
                        "are using must be signed with release keys."
            )

            else -> Log.d(
                TAG,
                "BillingResult [" + billingResult.responseCode + "]: " + billingResult.debugMessage
            )
        }
        defaultScope.launch {
            billingFlowInProcess.emit(false)
        }
    }

    /**
     * Ideally your implementation will comprise a secure server, rendering this check
     * unnecessary. @see [Security]
     */
    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(purchase.originalJson, purchase.signature)
    }

    fun consumeTokens(tokensQuantityToConsume: Int) {
        chatTokensStateModel.consumeTokens(tokensQuantityToConsume)
    }

    companion object {
        private val TAG = BillingDataSource::class.java.simpleName

        @Volatile
        private var sInstance: BillingDataSource? = null
        private val handler = Handler(Looper.getMainLooper())

        // Standard boilerplate double check locking pattern for thread-safe singletons.
        @JvmStatic
        fun getInstance(
            application: Application,
            chatRequestsStateModel: ChatRequestsStateModel,
            defaultScope: CoroutineScope,
            knownInappSKUs: Array<String>?,
            knownSubscriptionSKUs: Array<String>?,
            autoConsumeSKUs: Array<String>?
        ) = sInstance ?: synchronized(this) {
            sInstance ?: BillingDataSource(
                application,
                chatRequestsStateModel,
                defaultScope,
                knownInappSKUs,
                knownSubscriptionSKUs,
                autoConsumeSKUs
            )
                .also { sInstance = it }
        }
    }

    /**
     * Our constructor.  Since we are a singleton, this is only used internally.
     * @param application Android application class.
     * @param knownInappSKUs SKUs of in-app purchases the source should know about
     * @param knownSubscriptionSKUs SKUs of subscriptions the source should know about
     */
    init {
        this.knownInappSKUs = if (knownInappSKUs == null) {
            ArrayList()
        } else {
            listOf(*knownInappSKUs)
        }
        this.knownSubscriptionSKUs = if (knownSubscriptionSKUs == null) {
            ArrayList()
        } else {
            listOf(*knownSubscriptionSKUs)
        }
        knownAutoConsumeSKUs = HashSet()
        if (autoConsumeSKUs != null) {
            knownAutoConsumeSKUs.addAll(listOf(*autoConsumeSKUs))
        }
        initializeFlows()
        billingClient = BillingClient.newBuilder(application)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(this)
        this.chatTokensStateModel = chatRequestsStateModel
    }
}