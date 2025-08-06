package com.gabrielsantos.shortnify.data

sealed class NetworkRequestState {
    data object Loading: NetworkRequestState()
    data object Success: NetworkRequestState()
    data object Error: NetworkRequestState()
}
