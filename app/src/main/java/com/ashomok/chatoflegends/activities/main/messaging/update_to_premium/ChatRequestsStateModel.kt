package com.ashomok.chatoflegends.activities.main.messaging.update_to_premium

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.ashomok.chatoflegends.R
import com.ashomok.chatoflegends.utils.LogHelper
import javax.inject.Singleton

/**
 * Created by iuliia on 3/9/18.
 */
@Singleton
class ChatRequestsStateModel private constructor(application: Application) {

    var context: Context = application.applicationContext

    private var sharedPreferences: SharedPreferences = application.getSharedPreferences(
        application.getString(R.string.preferences),
        Context.MODE_PRIVATE
    )

    val availableTokensLiveData: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    init {
        availableTokensLiveData.postValue(
            sharedPreferences.getInt(
                AVAILABLE_CHAT_REQUESTS_COUNT_TAG, INIT_CHAT_REQUESTS_COUNT
            )
        )
    }

    companion object {
        private var INSTANCE: ChatRequestsStateModel? = null

        fun getInstance(application: Application): ChatRequestsStateModel {
            if (INSTANCE == null) {
                INSTANCE = ChatRequestsStateModel(application)
            }
            return INSTANCE as ChatRequestsStateModel
        }

        private const val AVAILABLE_CHAT_REQUESTS_COUNT_TAG = "availableChatRequestsCount"
        private const val INIT_CHAT_REQUESTS_COUNT = 50
        val TAG: String = LogHelper.makeLogTag(ChatRequestsStateModel::class.simpleName)
    }

    private fun saveAvailableOcrRequests(available: Int) {
        sharedPreferences.edit().putInt(AVAILABLE_CHAT_REQUESTS_COUNT_TAG, available).apply()
        availableTokensLiveData.postValue(available)
    }

    fun incrementTokens(tokensToIncrement: Int) {
        LogHelper.d(TAG, "incrementRequests with " + tokensToIncrement)
        var availableTokens = availableTokensLiveData.value
        availableTokens = availableTokens?.plus(tokensToIncrement)
        if (availableTokens != null) {
            saveAvailableOcrRequests(availableTokens)
            LogHelper.d(
                TAG,
                " availableTokensToIncrement.postValue with$availableTokens"
            )
        }
    }

    fun consumeTokens(tokensToConsume: Int) {
        val amount = availableTokensLiveData.value!!
        saveAvailableOcrRequests(amount - tokensToConsume)
    }
}