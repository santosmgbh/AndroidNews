package com.gabrielsantos.shortnify.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.gabrielsantos.shortnify.R
import com.gabrielsantos.shortnify.ui.components.LinkInputField
import com.gabrielsantos.shortnify.ui.components.LinkList
import com.gabrielsantos.shortnify.ui.helper.CollectAsEvent
import com.gabrielsantos.shortnify.ui.theme.ShortnifyTheme

@Composable
internal fun HomeScreen(viewModel: HomeViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var isSending by rememberSaveable { mutableStateOf(false) }
    var inputText by rememberSaveable { mutableStateOf("") }

    CollectAsEvent(viewModel.event) { event ->
        when (event) {
            HomeUIEvent.OnShortLinkSuccess -> {
                isSending = false
                inputText = ""
                snackBarHostState.showSnackbar("Link shorted successfully!")
            }

            is HomeUIEvent.OnShortLinkNetworkError -> {
                isSending = false
                snackBarHostState.showSnackbar("Something went wrong. Please try again later!")
            }

            HomeUIEvent.OnShortLinkInvalidLink -> {
                isSending = false
                snackBarHostState.showSnackbar("The link provided is invalid!")
            }

            is HomeUIEvent.OnNavigateToLink -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = event.url.toUri()
                context.startActivity(intent)
            }

            HomeUIEvent.OnShortLinkLoading -> {
                isSending = true
            }

        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.app_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )

            LinkInputField(
                inputText = inputText,
                isLoading = isSending,
                onSend = { link -> viewModel.onIntent(HomeUIIntent.OnShortLink(link)) }
            )

            Row(modifier = Modifier.weight(1f)) {
                when (val currentState = uiState) {
                    HomeUIState.Loading -> {
                        ProgressView()
                    }
                    is HomeUIState.Success -> {
                        LinkList(
                            links = currentState.links,
                            onClickItem = { url ->
                                viewModel.onIntent(HomeUIIntent.OnNavigateToLink(url))
                            })
                    }
                    is HomeUIState.Error -> {
                        ErrorView()
                    }
                    HomeUIState.Empty -> {
                        EmptyView()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.loading),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun EmptyView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_links_yet_try_adding_one),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.sorry_something_went_wrong),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun GreetingPreview() {
    ShortnifyTheme {
        LinkInputField(onSend = {})
    }
}