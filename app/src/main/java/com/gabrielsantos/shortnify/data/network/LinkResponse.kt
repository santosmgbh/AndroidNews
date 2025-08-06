package com.gabrielsantos.shortnify.data.network

import com.google.gson.annotations.SerializedName

data class LinkResponse(
    @SerializedName("alias")
    val alias: String,
    @SerializedName("_links")
    val links: Links
)

data class Links(
    @SerializedName("self")
    val self: String,
    @SerializedName("short")
    val short: String
)