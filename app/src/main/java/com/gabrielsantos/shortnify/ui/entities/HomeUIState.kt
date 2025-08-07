package com.gabrielsantos.shortnify.ui.entities

import com.gabrielsantos.shortnify.domain.LinkItem

sealed class HomeUIState {
    data object Empty: HomeUIState()
    data object Loading: HomeUIState()
    data class Success(val links: List<LinkItem>): HomeUIState()
    data class Error(val message: String): HomeUIState()
}