package com.example.example

import com.google.gson.annotations.SerializedName


data class Choices(

    @SerializedName("message") var message: Message? = Message(),
    @SerializedName("finish_reason") var finishReason: String? = null,
    @SerializedName("index") var index: Int? = null

)