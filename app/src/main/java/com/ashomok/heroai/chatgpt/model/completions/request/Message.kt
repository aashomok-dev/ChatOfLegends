package com.ashomok.heroai.chatgpt.model.completions.request

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("role") var role: String?,
    @SerializedName("content") var content: String? //todo Text instead String
)
