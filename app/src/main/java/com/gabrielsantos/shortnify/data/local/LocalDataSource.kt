package com.gabrielsantos.shortnify.data.local

import com.gabrielsantos.shortnify.data.LinkData
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun getShortenedUrls(): Flow<List<LinkData>>
    fun addShortenedUrl(shortenedUrl: String)
    fun clearShortenedUrls()
}