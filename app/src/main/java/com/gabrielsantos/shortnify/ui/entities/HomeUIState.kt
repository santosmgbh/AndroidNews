package com.gabrielsantos.shortnify.ui.entities

sealed class HomeUIState {
    data object Empty: HomeUIState()
    data object Loading: HomeUIState()
    data class Success(val links: List<LinkItem>): HomeUIState()
    data class Error(val message: String): HomeUIState()
}