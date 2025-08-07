package com.gabrielsantos.shortnify.data

import android.util.Log
import com.gabrielsantos.shortnify.data.local.LocalDataSource
import com.gabrielsantos.shortnify.data.network.LinkAPI
import com.gabrielsantos.shortnify.data.network.LinkRequest
import com.gabrielsantos.shortnify.domain.LinkRepository
import com.gabrielsantos.shortnify.domain.LinkItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepositoryImpl @Inject constructor(
    private val linkAPI: LinkAPI,
    private val localDataSource: LocalDataSource
) : LinkRepository {

    override suspend fun shortLink(link: String): Result<Unit> =
        try {
            val shortedLinkResponse = linkAPI.shortLink(LinkRequest(link))
            localDataSource.addShortenedUrl(shortedLinkResponse.links.short)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override fun getShortnedLinks(): Flow<List<LinkItem>> {
        return localDataSource.getShortenedUrls()
    }
}