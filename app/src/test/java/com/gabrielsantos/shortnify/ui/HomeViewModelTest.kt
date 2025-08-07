package com.gabrielsantos.shortnify.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.gabrielsantos.shortnify.domain.ShortLinkUseCase
import com.gabrielsantos.shortnify.ui.entities.HomeUIState
import com.gabrielsantos.shortnify.domain.LinkItem
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
        forceStateFlowToStart()

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
        forceStateFlowToStart()

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
        forceStateFlowToStart()

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
        forceStateFlowToStart()

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
        forceStateFlowToStart()

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
        forceStateFlowToStart()

        val currentState = homeViewModel.uiState.value
        assertTrue(currentState is HomeUIState.Error)
        assertEquals("Unexpected Error", (currentState as HomeUIState.Error).message)
    }

    @Test
    fun `shortLink success`() = testScope.runTest {
        // When shortLinkUseCase emits ShortLinkUIState.Success, verify _shortLinkUIState is updated accordingly.
        every { getShortnedLinksUseCase() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("test-link") } returns flowOf(ShortLinkUIState.Success)

        createViewModel()
        homeViewModel.shortLink("test-link")
        forceStateFlowToStart()

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
        forceStateFlowToStart()

        val currentState = homeViewModel.uiState.value
        assertTrue(currentState is HomeUIState.Error)
        assertEquals("Unexpected Error", (currentState as HomeUIState.Error).message)
    }

    private fun TestScope.forceStateFlowToStart() {
        // Force the StateFlow to start by subscribing to it
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
    }

}