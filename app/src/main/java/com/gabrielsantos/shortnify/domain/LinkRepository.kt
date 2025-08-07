package com.gabrielsantos.shortnify.domain

import kotlinx.coroutines.flow.Flow

interface LinkRepository {
    suspend fun shortLink(link: String): Result<Unit>
    fun getShortnedLinks(): Flow<List<LinkItem>>
}