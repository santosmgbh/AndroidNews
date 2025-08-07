package com.gabrielsantos.shortnify.domain

import android.util.Patterns
import javax.inject.Inject

class ShortLink @Inject constructor(private val linkRepository: LinkRepository) {

    suspend operator fun invoke(link: String): Result<ShortLinkResultState> {
        if (!Patterns.WEB_URL.matcher(link).matches())
            return Result.success(ShortLinkResultState.InvalidLinkError)
        val result = linkRepository.shortLink(link = link).map {
            ShortLinkResultState.Success
        }
        return if (result.isSuccess) {
            result
        } else {
            Result.success(ShortLinkResultState.NetworkError)
        }
    }

    sealed class ShortLinkResultState {
        data object Success : ShortLinkResultState()
        data object InvalidLinkError : ShortLinkResultState()
        data object NetworkError : ShortLinkResultState()

    }
}