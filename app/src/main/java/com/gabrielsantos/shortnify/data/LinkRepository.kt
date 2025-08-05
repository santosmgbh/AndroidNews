package com.gabrielsantos.shortnify.data

import kotlinx.coroutines.flow.Flow

interface LinkRepository {
    fun shortLink(link: String)
    fun getShortnedLinks(): Flow<List<LinkData>>
}