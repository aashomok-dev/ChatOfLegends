package com.ashomok.lullabies.billing_kotlin

@Suppress("unused")
object AppSku {

    const val REQUESTS_10_SKU_ID = "requests_batch_5"
    const val REQUESTS_100_SKU_ID = "requests_batch_100"
    const val REQUESTS_10_BATCH_SIZE = 10
    const val REQUESTS_100_BATCH_SIZE = 100
    const val REQUESTS_INFINITE = Int.MAX_VALUE

    @JvmField
    val PREMIUM_MONTHLY_SKU_ID: String = "one_month_subscription"

    @JvmField
    val PREMIUM_YEARLY_SKU_ID: String = "one_year_subscription"

    var INAPP_SKUS = arrayOf(REQUESTS_10_SKU_ID, REQUESTS_100_SKU_ID)

    var SUBSCRIPTION_SKUS = arrayOf(PREMIUM_MONTHLY_SKU_ID, PREMIUM_YEARLY_SKU_ID)

    var AUTO_CONSUME_SKUS = arrayOf(REQUESTS_10_SKU_ID, REQUESTS_100_SKU_ID)
}
