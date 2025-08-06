package com.gabrielsantos.shortnify.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class LinkRequest(
    @SerializedName("url") val url: String
)

interface LinkAPI {
    @POST("api/alias")
    suspend fun shortLink(
        @Body linkRequest: LinkRequest
    ): LinkResponse
}
