package com.gabrielsantos.shortnify.data

import com.gabrielsantos.shortnify.data.local.LocalDataSource
import com.gabrielsantos.shortnify.data.network.LinkAPI
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LinkRepositoryImpl @Inject constructor(private val linkAPI: LinkAPI, private val localDataSource: LocalDataSource): LinkRepository {
    override fun shortLink(link: String) {
        val shortenedUrl = linkAPI.shortLink(link)
        localDataSource.addShortenedUrl(LinkData(id = 1, shortenedUrl))
    }

    override fun getShortnedLinks(): Flow<List<LinkData>> {
        return localDataSource.getShortenedUrls()
    }
}