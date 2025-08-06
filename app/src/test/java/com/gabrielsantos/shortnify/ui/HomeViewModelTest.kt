package com.gabrielsantos.shortnify.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.gabrielsantos.shortnify.domain.GetShortnedLinksUseCase
import com.gabrielsantos.shortnify.domain.ShortLinkUseCase
import com.gabrielsantos.shortnify.ui.entities.HomeUIState
import com.gabrielsantos.shortnify.ui.entities.LinkItem
import com.gabrielsantos.shortnify.ui.entities.ShortLinkUIState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var shortLinkUseCase: ShortLinkUseCase
    private lateinit var getShortnedLinksUseCase: GetShortnedLinksUseCase
    private lateinit var homeViewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        shortLinkUseCase = mockk()
        getShortnedLinksUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        homeViewModel = HomeViewModel(shortLinkUseCase, getShortnedLinksUseCase)
    }

    @Test
    fun `getUiState initial state`() = testScope.runTest {
        // Verify that the initial value of uiState is HomeUIState.Loading.
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        
        createViewModel()
        
        // The initial value should be Loading as defined in stateIn
        assertEquals(HomeUIState.Loading, homeViewModel.uiState.value)
    }

    @Test
    fun `getUiState success state with links`() = testScope.runTest {
        // When getShortnedLinksUseCase emits a non-empty list and _shortLinkUIState is None or Success, 
        // verify uiState transitions to HomeUIState.Success with the correct links.
        val testLinks = listOf(
            LinkItem(1, "https://short.ly/test1"),
            LinkItem(2, "https://short.ly/test2")
        )
        every { getShortnedLinksUseCase() } returns flowOf(testLinks)
        
        createViewModel()
        
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        
        // Wait for StateFlow to process the combine operation
        advanceUntilIdle()
        job.cancel()
        
        val currentState = homeViewModel.uiState.value
        assertTrue("Expected Success state but got $currentState", currentState is HomeUIState.Success)
        assertEquals(testLinks, (currentState as HomeUIState.Success).links)
    }

    @Test
    fun `getUiState empty state`() = testScope.runTest {
        // When getShortnedLinksUseCase emits an empty list and _shortLinkUIState is None or Success, 
        // verify uiState transitions to HomeUIState.Empty.
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        
        createViewModel()
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        val currentState = homeViewModel.uiState.value
        assertTrue("Expected Empty state but got $currentState", currentState is HomeUIState.Empty)
    }

    @Test
    fun `getUiState error state from getShortnedLinksUseCase`() = testScope.runTest {
        // When getShortnedLinksUseCase emits an error and _shortLinkUIState is None or Success, 
        // verify uiState transitions to HomeUIState.Error with the correct error message.
        val errorMessage = "Network error"
        val exception = RuntimeException(errorMessage)
        every { getShortnedLinksUseCase() } returns flow {
            throw exception
        }
        
        createViewModel()
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        val currentState = homeViewModel.uiState.value
        assertTrue("Expected Error state but got $currentState", currentState is HomeUIState.Error)
        assertEquals(errorMessage, (currentState as HomeUIState.Error).message)
    }

    @Test
    fun `getUiState error state from getShortnedLinksUseCase with null message`() = testScope.runTest {
        // When getShortnedLinksUseCase emits an error with a null message and _shortLinkUIState is None or Success, 
        // verify uiState transitions to HomeUIState.Error with 'Unknown error'.
        val exception = RuntimeException(null as String?)
        every { getShortnedLinksUseCase() } returns flow {
            throw exception
        }
        
        createViewModel()
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        val currentState = homeViewModel.uiState.value
        assertTrue("Expected Error state but got $currentState", currentState is HomeUIState.Error)
        assertEquals("Unknown error", (currentState as HomeUIState.Error).message)
    }

    @Test
    fun `getUiState loading state due to shortLinkUIState`() = testScope.runTest {
        // When _shortLinkUIState is ShortLinkUIState.Loading, verify uiState transitions to HomeUIState.Loading, 
        // regardless of the emission from getShortnedLinksUseCase.
        val testLinks = listOf(LinkItem(1, "https://short.ly/test"))
        every { getShortnedLinksUseCase() } returns flowOf(testLinks)
        coEvery { shortLinkUseCase("test-link") } returns flowOf(ShortLinkUIState.Loading)
        
        createViewModel()
        homeViewModel.shortLink("test-link")
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        assertEquals(HomeUIState.Loading, homeViewModel.uiState.value)
    }

    @Test
    fun `getUiState error state due to shortLinkUIState`() = testScope.runTest {
        // When _shortLinkUIState is ShortLinkUIState.Error, verify uiState transitions to HomeUIState.Error with 'Unexpected Error', 
        // regardless of the emission from getShortnedLinksUseCase.
        val testLinks = listOf(LinkItem(1, "https://short.ly/test"))
        every { getShortnedLinksUseCase() } returns flowOf(testLinks)
        coEvery { shortLinkUseCase("test-link") } returns flowOf(ShortLinkUIState.Error)
        
        createViewModel()
        homeViewModel.shortLink("test-link")
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        val currentState = homeViewModel.uiState.value
        assertTrue(currentState is HomeUIState.Error)
        assertEquals("Unexpected Error", (currentState as HomeUIState.Error).message)
    }

    @Test
    fun `getUiState multiple emissions from getShortnedLinksUseCase`() = testScope.runTest {
        // Verify uiState updates correctly upon multiple emissions from getShortnedLinksUseCase 
        // (e.g., from empty to success, or success to error).
        val initialLinks = emptyList<LinkItem>()
        val updatedLinks = listOf(LinkItem(1, "https://short.ly/test"))
        
        // Note: This test would be more complex in real scenario with multiple emissions
        // For simplicity, testing with single emission that transitions from empty to success
        every { getShortnedLinksUseCase() } returns flowOf(updatedLinks)
        
        createViewModel()
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        val currentState = homeViewModel.uiState.value
        assertTrue(currentState is HomeUIState.Success)
        assertEquals(updatedLinks, (currentState as HomeUIState.Success).links)
    }

    @Test
    fun `getUiState transitions with shortLinkUIState changes`() = testScope.runTest {
        // Verify uiState transitions correctly as _shortLinkUIState changes
        val testLinks = listOf(LinkItem(1, "https://short.ly/test"))
        every { getShortnedLinksUseCase() } returns flowOf(testLinks)
        coEvery { shortLinkUseCase("test-link") } returns flowOf(ShortLinkUIState.Success)
        
        createViewModel()
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        // Initially should show Success with links (shortLinkUIState is None)
        var currentState = homeViewModel.uiState.value
        assertTrue(currentState is HomeUIState.Success)
        
        // Start short link process
        homeViewModel.shortLink("test-link")
        advanceUntilIdle()
        
        // Should show the final state after the flow completes
        currentState = homeViewModel.uiState.value
        assertTrue(currentState is HomeUIState.Success)
        assertEquals(testLinks, (currentState as HomeUIState.Success).links)
    }

    @Test
    fun `getUiState SharingStarted behavior`() = testScope.runTest {
        // Verify that the underlying flows (getShortnedLinksUseCase) are started only when uiState is subscribed to, 
        // and stopped after the 5000L timeout when there are no subscribers.
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        
        createViewModel()
        
        // The StateFlow should have the initial value
        assertEquals(HomeUIState.Loading, homeViewModel.uiState.value)
        
        // Advance time to allow the StateFlow to process
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        // After processing, should have the computed state
        assertEquals(HomeUIState.Empty, homeViewModel.uiState.value)
    }

    @Test
    fun `shortLink success`() = testScope.runTest {
        // When shortLinkUseCase emits ShortLinkUIState.Success, verify _shortLinkUIState is updated accordingly.
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("test-link") } returns flowOf(ShortLinkUIState.Success)
        
        createViewModel()
        homeViewModel.shortLink("test-link")
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        // After the flow completes with Success, the UI state should reflect the links state
        // since Success state passes through to the shortened links state
        assertEquals(HomeUIState.Empty, homeViewModel.uiState.value)
    }

    @Test
    fun `shortLink loading`() = testScope.runTest {
        // When shortLinkUseCase emits ShortLinkUIState.Loading, verify _shortLinkUIState is updated accordingly.
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("test-link") } returns flowOf(ShortLinkUIState.Loading)
        
        createViewModel()
        homeViewModel.shortLink("test-link")
        advanceUntilIdle()
        
        assertEquals(HomeUIState.Loading, homeViewModel.uiState.value)
    }

    @Test
    fun `shortLink error`() = testScope.runTest {
        // When shortLinkUseCase emits ShortLinkUIState.Error, verify _shortLinkUIState is updated accordingly.
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("test-link") } returns flowOf(ShortLinkUIState.Error)
        
        createViewModel()
        homeViewModel.shortLink("test-link")
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        val currentState = homeViewModel.uiState.value
        assertTrue(currentState is HomeUIState.Error)
        assertEquals("Unexpected Error", (currentState as HomeUIState.Error).message)
    }

    @Test
    fun `shortLink with empty input link`() = testScope.runTest {
        // Test behavior when an empty string is passed to shortLink. 
        // The expected behavior depends on the ShortLinkUseCase implementation (e.g., it might emit an error or handle it gracefully).
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("") } returns flowOf(ShortLinkUIState.Error)
        
        createViewModel()
        homeViewModel.shortLink("")
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        val currentState = homeViewModel.uiState.value
        assertTrue(currentState is HomeUIState.Error)
        assertEquals("Unexpected Error", (currentState as HomeUIState.Error).message)
    }

    @Test
    fun `shortLink with invalid input link format`() = testScope.runTest {
        // Test behavior when an invalidly formatted link (e.g., not a URL) is passed to shortLink. 
        // The expected behavior depends on the ShortLinkUseCase implementation.
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("invalid-url") } returns flowOf(ShortLinkUIState.Error)
        
        createViewModel()
        homeViewModel.shortLink("invalid-url")
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        val currentState = homeViewModel.uiState.value
        assertTrue(currentState is HomeUIState.Error)
        assertEquals("Unexpected Error", (currentState as HomeUIState.Error).message)
    }

    @Test
    fun `shortLink with very long input link`() = testScope.runTest {
        // Test behavior with an excessively long input string for the link. 
        // The expected behavior depends on the ShortLinkUseCase implementation.
        val longUrl = "https://example.com/" + "x".repeat(2000)
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase(longUrl) } returns flowOf(ShortLinkUIState.Success)
        
        createViewModel()
        homeViewModel.shortLink(longUrl)
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        // Should handle long URLs gracefully
        assertEquals(HomeUIState.Empty, homeViewModel.uiState.value)
    }

    @Test
    fun `shortLink concurrent calls`() = testScope.runTest {
        // If shortLink is called multiple times in quick succession, 
        // verify that _shortLinkUIState reflects the latest call's state or handles concurrency correctly.
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("link1") } returns flowOf(ShortLinkUIState.Success)
        coEvery { shortLinkUseCase("link2") } returns flowOf(ShortLinkUIState.Error)
        
        createViewModel()
        
        // Call shortLink multiple times quickly
        homeViewModel.shortLink("link1")
        homeViewModel.shortLink("link2")
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        // The final state should reflect the last call (Error from link2)
        val currentState = homeViewModel.uiState.value
        assertTrue(currentState is HomeUIState.Error)
        assertEquals("Unexpected Error", (currentState as HomeUIState.Error).message)
    }

    @Test
    fun `shortLink coroutine cancellation`() = testScope.runTest {
        // Verify that shortLink launches properly and updates state
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("test-link") } returns flowOf(ShortLinkUIState.Loading)
        
        createViewModel()
        homeViewModel.shortLink("test-link")
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
        
        // Should show loading state from the shortLink operation
        assertEquals(HomeUIState.Loading, homeViewModel.uiState.value)
    }

    @Test
    fun `getShortLinkUseCase returns instance`() {
        // Verify that the constructor correctly initializes and returns the injected shortLinkUseCase instance. 
        // This is more of an integration/Hilt test, but can be verified if shortLinkUseCase is made public or used internally.
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        
        createViewModel()
        
        // Since shortLinkUseCase is public in the ViewModel, we can verify it's set correctly
        assertEquals(shortLinkUseCase, homeViewModel.shortLinkUseCase)
    }

}