package com.gabrielsantos.shortnify.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabrielsantos.shortnify.R
import com.gabrielsantos.shortnify.ui.entities.HomeUIState
import com.gabrielsantos.shortnify.ui.entities.LinkItem
import com.gabrielsantos.shortnify.ui.theme.ShortnifyTheme

@Composable
internal fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LinkInputField(onSend = { link -> viewModel.shortLink(link) })
            }
            Row(modifier = Modifier.weight(1f)) { // LinkList takes remaining space
                when (val currentState = uiState) {
                    HomeUIState.Loading -> Text(text = "Loading")
                    is HomeUIState.Success -> LinkList(links = currentState.links)
                    is HomeUIState.Error -> Text(text = "Error")
                }
            }
        }
    }
}

@Composable
fun LinkInputField(onSend: (link: String) -> Unit) {
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        onValueChange = { newText -> text = newText },
        label = { Text(stringResource(R.string.enter_your_link)) },
    )
    IconButton(onClick = {
        onSend(text)
        text = ""
    }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = stringResource(R.string.send)
        )
    }
}

@Composable
fun LinkList(links: List<LinkItem>, modifier: Modifier = Modifier) {
    Surface {
        LazyColumn(modifier = modifier.fillMaxWidth()) {
            items(items = links, key = { link -> link.id }) { link ->
                LinkItem(link.url)
            }
        }
    }
}

@Composable
fun LinkItem(link: String) {
    Text(
        text = link,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun GreetingPreview() {
    ShortnifyTheme {
//        HomeScreen()
    }
}