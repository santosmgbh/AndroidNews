package com.gabrielsantos.shortnify.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielsantos.shortnify.domain.GetShortnedLinksUseCase
import com.gabrielsantos.shortnify.domain.ShortLinkUseCase
import com.gabrielsantos.shortnify.ui.entities.LinkItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val shortLinkUseCase: ShortLinkUseCase, getShortnedLinksUseCase: GetShortnedLinksUseCase): ViewModel() {

    private val _uiState: MutableStateFlow<HomeUIState> = MutableStateFlow(HomeUIState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getShortnedLinksUseCase().
            catch { exception ->
                _uiState.value = HomeUIState.Error(exception.message ?: "Unknown error") }
            .collect {
                _uiState.value = HomeUIState.Success(it)
            }
        }
    }

    fun shortLink(link: String) {
        viewModelScope.launch {
            shortLinkUseCase(link)
        }
    }
}

sealed class HomeUIState {
    object Loading: HomeUIState()
    data class Success(val links: List<LinkItem>): HomeUIState()
    data class Error(val message: String): HomeUIState()
}