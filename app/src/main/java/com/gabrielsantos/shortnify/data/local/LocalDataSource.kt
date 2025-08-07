package com.gabrielsantos.shortnify.data.local

import com.gabrielsantos.shortnify.domain.LinkItem
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getShortenedUrls(): Flow<List<LinkItem>>
    fun addShortenedUrl(shortenedUrl: String)
    fun clearShortenedUrls()
}