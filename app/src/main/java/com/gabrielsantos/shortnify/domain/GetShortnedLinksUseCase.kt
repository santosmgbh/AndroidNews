package com.gabrielsantos.shortnify.domain

import com.gabrielsantos.shortnify.data.LinkRepository
import com.gabrielsantos.shortnify.ui.entities.LinkItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetShortnedLinksUseCase @Inject constructor(private val linkRepository: LinkRepository) {

    operator fun invoke(): Flow<List<LinkItem>> {
        return linkRepository.getShortnedLinks().map {
            list -> list.map {
                LinkItem(it.id, it.url)
            }
        }
    }

}