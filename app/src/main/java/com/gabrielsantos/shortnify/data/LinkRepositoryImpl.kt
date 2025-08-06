package com.gabrielsantos.shortnify.data

import android.util.Log
import com.gabrielsantos.shortnify.data.local.LocalDataSource
import com.gabrielsantos.shortnify.data.network.LinkAPI
import com.gabrielsantos.shortnify.data.network.LinkRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepositoryImpl @Inject constructor(
    private val linkAPI: LinkAPI,
    private val localDataSource: LocalDataSource
) : LinkRepository {

    override suspend fun shortLink(link: String): Flow<NetworkRequestState> =
        flow {
            emit(NetworkRequestState.Loading)
            try {
                val shortedLinkResponse = linkAPI.shortLink(LinkRequest(link))
                localDataSource.addShortenedUrl(shortedLinkResponse.links.short)
                emit(NetworkRequestState.Success)
            } catch (e: Exception) {
                Log.e("Error", e.message.toString())
                emit(NetworkRequestState.Error)
            }
        }

    override fun getShortnedLinks(): Flow<List<LinkData>> {
        return localDataSource.getShortenedUrls()
    }
}