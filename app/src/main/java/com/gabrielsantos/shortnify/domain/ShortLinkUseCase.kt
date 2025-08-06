package com.gabrielsantos.shortnify.domain

import com.gabrielsantos.shortnify.data.LinkRepository
import com.gabrielsantos.shortnify.data.NetworkRequestState
import com.gabrielsantos.shortnify.ui.entities.ShortLinkUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShortLinkUseCase @Inject constructor(private val linkRepository: LinkRepository) {

    suspend operator fun invoke(link: String) = linkRepository.shortLink(link).mapToShortLinkUIState()
}

private fun Flow<NetworkRequestState>.mapToShortLinkUIState(): Flow<ShortLinkUIState> {
    return this.map {
        when (it) {
            NetworkRequestState.Loading -> ShortLinkUIState.Loading
            NetworkRequestState.Success -> ShortLinkUIState.Success
            NetworkRequestState.Error -> ShortLinkUIState.Error
        }
    }
}
