package com.gabrielsantos.shortnify.data.network

import javax.inject.Inject

class LinkAPIImpl @Inject constructor(): LinkAPI {
    override fun shortLink(link: String): String {
        return link + "/short"
    }
}