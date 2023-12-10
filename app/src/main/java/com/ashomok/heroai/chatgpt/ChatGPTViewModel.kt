package com.ashomok.heroai.chatgpt

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ashomok.heroai.R
import com.ashomok.heroai.chatgpt.model.completions.request.RequestModel
import com.ashomok.heroai.utils.LogHelper
import com.google.gson.Gson
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.await
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class ChatGPTViewModel(context: Context) : ViewModel(){
    private val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val responseLimited = context.getResources().getString(R.string.limited_response)
    val chatResponseLiveData = MutableLiveData<String?>()
    private val onNewRequestReceivedPublish = PublishSubject.create<RequestModel>()
    private var chatGPTWebService = ChatGPTWebService()
    private val TAG = LogHelper.makeLogTag(ChatGPTViewModel::class.java)

    init {
        onNewRequestReceivedPublish
            .subscribe {
                GlobalScope.launch (Dispatchers.Main) { getGPTResponse(it)}
            }
    }

    fun askGPTChat(requestModel: RequestModel): Boolean{
         if (isOnline()) {
            onNewRequestReceivedPublish.onNext(requestModel)
            return true
        }
        return false
    }

    private fun isOnline(): Boolean {
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private suspend fun getGPTResponse(requestModel: RequestModel) {
            try {
                val gson = Gson()
                val json = gson.toJson(requestModel)
                val charset: Charset = StandardCharsets.UTF_8
                val requestModelBytes: ByteArray = json.toByteArray(charset)
                val stringKey = "fgO8AorO79aW+UBr3XWVmKm+svwcHSdlwctM1yxw/2U="
                val decodedKey: ByteArray = Base64.getDecoder().decode(stringKey)
                val originalKey: SecretKey = SecretKeySpec(decodedKey, 0, 16, "AES")
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.ENCRYPT_MODE, originalKey,  IvParameterSpec(ByteArray(16)))
                val encrypted: ByteArray = cipher.doFinal(requestModelBytes)
                val response = chatGPTWebService.postForResponseAsync(Base64.getEncoder().encodeToString(encrypted))?.await()
                var responseContent = response?.data?.choices?.get(0)?.message?.content
                val finishReason = response?.data?.choices?.get(0)?.finishReason
                if (finishReason.equals("length")){
                    responseContent+= "[$responseLimited]"
                }
                chatResponseLiveData.value = responseContent

            } catch (e: Exception) {
                LogHelper.e(TAG, e.message)
                e.printStackTrace()
            }
    }
}