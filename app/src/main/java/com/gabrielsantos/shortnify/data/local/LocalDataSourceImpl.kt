package com.gabrielsantos.shortnify.data.local

import com.gabrielsantos.shortnify.domain.LinkItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSourceImpl @Inject constructor(): LocalDataSource {

    private val _shortenedUrls = MutableStateFlow<List<LinkItem>>(emptyList())

    override fun getShortenedUrls(): Flow<List<LinkItem>> {
        return _shortenedUrls
    }

    override fun addShortenedUrl(shortenedUrl: String) {
        val linkData: LinkItem = if (_shortenedUrls.value.isEmpty()) {
            LinkItem(id = 0, url = shortenedUrl)
        } else {
            LinkItem(id = _shortenedUrls.value.last().id + 1, url = shortenedUrl)
        }
        _shortenedUrls.update { currentList -> currentList + linkData }
    }

    override fun clearShortenedUrls() {
        _shortenedUrls.value = emptyList()
    }
}