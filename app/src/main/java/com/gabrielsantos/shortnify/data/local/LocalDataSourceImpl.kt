package com.gabrielsantos.shortnify.data.local

import com.gabrielsantos.shortnify.data.LinkData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class LocalDataSourceImpl @Inject constructor(): LocalDataSource {

    private val _shortenedUrls = MutableStateFlow<List<LinkData>>(emptyList())

    override fun getShortenedUrls(): Flow<List<LinkData>> {
        return _shortenedUrls
    }

    override fun addShortenedUrl(shortenedUrl: LinkData) {
        _shortenedUrls.update { currentList -> currentList + shortenedUrl }
    }

    override fun clearShortenedUrls() {
        _shortenedUrls.value = emptyList()
    }
}