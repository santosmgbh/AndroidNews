package com.gabrielsantos.shortnify.ui.entities

sealed class ShortLinkUIState {
    data object None: ShortLinkUIState()
    data object Loading: ShortLinkUIState()
    data object Success: ShortLinkUIState()
    data object Error: ShortLinkUIState()
}
