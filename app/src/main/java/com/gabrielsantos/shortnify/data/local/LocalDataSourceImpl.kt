package com.gabrielsantos.shortnify.data.local

import com.gabrielsantos.shortnify.data.LinkData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSourceImpl @Inject constructor(): LocalDataSource {

    private val _shortenedUrls = MutableStateFlow<List<LinkData>>(emptyList())

    override fun getShortenedUrls(): Flow<List<LinkData>> {
        return _shortenedUrls
    }

    override fun addShortenedUrl(shortenedUrl: String) {
        val linkData: LinkData = if (_shortenedUrls.value.isEmpty()) {
            LinkData(id = 0, url = shortenedUrl)
        } else {
            LinkData(id = _shortenedUrls.value.last().id + 1, url = shortenedUrl)
        }
        _shortenedUrls.update { currentList -> currentList + linkData }
    }

    override fun clearShortenedUrls() {
        _shortenedUrls.value = emptyList()
    }
}