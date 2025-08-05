package com.gabrielsantos.shortnify.data.network

interface LinkAPI {
    fun shortLink(link: String): String
}