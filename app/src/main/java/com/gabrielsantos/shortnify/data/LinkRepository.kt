package com.gabrielsantos.shortnify.data

import kotlinx.coroutines.flow.Flow

interface LinkRepository {
    suspend fun shortLink(link: String): Flow<NetworkRequestState>
    fun getShortnedLinks(): Flow<List<LinkData>>
}