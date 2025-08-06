package com.gabrielsantos.shortnify.ui.entities

sealed class ShortLinkUIState {
    object None: ShortLinkUIState()
    object Loading: ShortLinkUIState()
    object Success: ShortLinkUIState()
    object Error: ShortLinkUIState()
}
