package com.gabrielsantos.shortnify.data

sealed class NetworkRequestState {
    object Loading: NetworkRequestState()
    object Success: NetworkRequestState()
    object Error: NetworkRequestState()
}
