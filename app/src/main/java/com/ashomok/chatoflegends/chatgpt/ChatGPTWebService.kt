package com.ashomok.chatoflegends.chatgpt

import com.example.example.ResponseModel
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

private val CLIENT_SECRET = "vbbdcvsdjhbcvjhsdbcjhsdbchjvdshj4343535hvhw43vh"
private const val BASE_URL = "https://us-central1-pc-api-7572373447067384777-526.cloudfunctions.net/replyOnChatGPTAndroidApp/"

interface ChatGPTWebService {

    @POST("completions")
    fun postForResponseAsync(@Body requestModel: String): Call<ResponseModel?>?


    companion object {
        operator fun invoke(
        ): ChatGPTWebService {
            val okHttpClient = OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val newRequest: Request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $CLIENT_SECRET")
                        .build()
                    chain.proceed(newRequest)
                }.build()

            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ChatGPTWebService::class.java)
        }
    }
}