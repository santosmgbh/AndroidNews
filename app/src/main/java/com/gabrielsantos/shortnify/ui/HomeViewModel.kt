package com.gabrielsantos.shortnify.ui

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielsantos.shortnify.domain.GetShortnedLinksUseCase
import com.gabrielsantos.shortnify.domain.ShortLinkUseCase
import com.gabrielsantos.shortnify.ui.entities.HomeUIState
import com.gabrielsantos.shortnify.ui.entities.LinkItem
import com.gabrielsantos.shortnify.ui.entities.ShortLinkUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val shortLinkUseCase: ShortLinkUseCase, getShortnedLinksUseCase: GetShortnedLinksUseCase): ViewModel() {

    private val _shortLinkUIState: MutableStateFlow<ShortLinkUIState> = MutableStateFlow(ShortLinkUIState.None)

    private val _shortenedLinksAsync = getShortnedLinksUseCase()
        .map { links ->
            if (links.isNotEmpty()) {
                HomeUIState.Success(links)
            } else {
                HomeUIState.Empty
            }
        }
        .catch { exception ->
            emit(HomeUIState.Error(exception.message ?: "Unknown error"))
        }

    val uiState: StateFlow<HomeUIState> = combine(_shortLinkUIState, _shortenedLinksAsync)
    { shortLinkUIState, shortenedLinks ->
        when (shortLinkUIState) {
            ShortLinkUIState.None -> shortenedLinks
            ShortLinkUIState.Loading -> HomeUIState.Loading
            ShortLinkUIState.Success -> shortenedLinks
            ShortLinkUIState.Error -> HomeUIState.Error("Unexpected Error")
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = HomeUIState.Loading
        )

    fun shortLink(link: String) {
        viewModelScope.launch {
            shortLinkUseCase(link).collect{
                _shortLinkUIState.value = it
            }
        }
    }

    fun navigateToLink(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = url.toUri()
        context.startActivity(intent)
    }
}