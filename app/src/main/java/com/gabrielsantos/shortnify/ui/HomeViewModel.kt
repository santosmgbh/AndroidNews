package com.gabrielsantos.shortnify.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielsantos.shortnify.domain.LinkItem
import com.gabrielsantos.shortnify.domain.LinkRepository
import com.gabrielsantos.shortnify.domain.ShortLink
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val shortLinkUseCase: ShortLink,
    val linkRepository: LinkRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUIState> = linkRepository.getShortnedLinks()
        .map { links ->
            if (links.isNotEmpty()) HomeUIState.Success(links) else HomeUIState.Empty
        }
        .catch { exception ->
            emit(HomeUIState.Error(exception.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = HomeUIState.Loading
        )

    private val _event = Channel<HomeUIEvent>()
    val event = _event.receiveAsFlow()

    fun onIntent(intent: HomeUIIntent) {
        when (intent) {
            is HomeUIIntent.OnShortLink -> shortLink(intent.link)
            is HomeUIIntent.OnNavigateToLink -> navigateToLink(intent.url)
        }
    }

    private fun shortLink(link: String) {
        viewModelScope.launch {
            _event.send(HomeUIEvent.OnShortLinkLoading)
            val result = shortLinkUseCase(link)
            result.onSuccess {
                val event = when (it) {
                    ShortLink.ShortLinkResultState.InvalidLinkError -> HomeUIEvent.OnShortLinkInvalidLink
                    ShortLink.ShortLinkResultState.NetworkError -> HomeUIEvent.OnShortLinkNetworkError
                    ShortLink.ShortLinkResultState.Success -> HomeUIEvent.OnShortLinkSuccess
                }
                _event.send(event)
            }.onFailure {
                _event.send(HomeUIEvent.OnShortLinkNetworkError)
            }
        }
    }

    private fun navigateToLink(url: String) {
        viewModelScope.launch {
            _event.send(HomeUIEvent.OnNavigateToLink(url))
        }
    }
}

sealed class HomeUIState {
    data object Empty : HomeUIState()
    data object Loading : HomeUIState()
    data class Success(val links: List<LinkItem>) : HomeUIState()
    data class Error(val message: String?) : HomeUIState()
}

sealed class HomeUIIntent() {
    data class OnShortLink(val link: String) : HomeUIIntent()
    data class OnNavigateToLink(val url: String) : HomeUIIntent()
}
sealed class HomeUIEvent() {
    data object OnShortLinkLoading : HomeUIEvent()
    data object OnShortLinkSuccess : HomeUIEvent()
    data object OnShortLinkNetworkError : HomeUIEvent()
    data object OnShortLinkInvalidLink : HomeUIEvent()
    data class OnNavigateToLink(val url: String) : HomeUIEvent()
}
