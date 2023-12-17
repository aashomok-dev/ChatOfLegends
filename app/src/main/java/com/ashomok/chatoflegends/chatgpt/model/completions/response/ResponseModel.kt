package com.example.example

import com.ashomok.chatoflegends.chatgpt.model.completions.response.Data
import com.google.gson.annotations.SerializedName


data class ResponseModel(

    @SerializedName("infoMessage") var infoMessage: String? = null,
    @SerializedName("errorMessage") var errorMessage: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("data") var data: Data? = Data()

)