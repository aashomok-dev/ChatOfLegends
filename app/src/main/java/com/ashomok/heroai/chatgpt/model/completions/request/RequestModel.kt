package com.ashomok.heroai.chatgpt.model.completions.request

import com.google.gson.annotations.SerializedName

data class RequestModel(
    @SerializedName("model") var model: String?,
    @SerializedName("messages") var messages: List<Message>?,
    @SerializedName("temperature") var temperature: Float?,
    @SerializedName("max_tokens") var maxTokens: Int?
)