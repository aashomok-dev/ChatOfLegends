package com.ashomok.heroai.chatgpt.model.completions.response

import com.example.example.Choices
import com.example.example.Usage
import com.google.gson.annotations.SerializedName

data class Data (
    @SerializedName("id") var id: String? = null,
    @SerializedName("object") var objectResponce: String? = null,
    @SerializedName("created") var created: Int? = null,
    @SerializedName("model") var model: String? = null,
    @SerializedName("usage") var usage: Usage? = Usage(),
    @SerializedName("choices") var choices: ArrayList<Choices> = arrayListOf()
)