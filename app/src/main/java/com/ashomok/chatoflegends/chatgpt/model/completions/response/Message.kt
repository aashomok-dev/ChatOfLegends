package com.example.example

import com.google.gson.annotations.SerializedName


data class Message(

    @SerializedName("role") var role: String? = null,
    @SerializedName("content") var content: String? = null

)