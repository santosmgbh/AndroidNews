package com.gabrielsantos.shortnify.domain

import com.gabrielsantos.shortnify.data.LinkRepository
import javax.inject.Inject

class ShortLinkUseCase @Inject constructor(private val linkRepository: LinkRepository) {

    suspend operator fun invoke(link: String) = linkRepository.shortLink(link)
}