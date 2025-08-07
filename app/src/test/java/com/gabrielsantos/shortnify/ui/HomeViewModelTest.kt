package com.gabrielsantos.shortnify.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.gabrielsantos.shortnify.domain.LinkItem
import com.gabrielsantos.shortnify.domain.LinkRepository
import com.gabrielsantos.shortnify.domain.ShortLinkUseCase
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
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var repository: LinkRepository
    private lateinit var shortLinkUseCase: ShortLinkUseCase
    private lateinit var homeViewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        shortLinkUseCase = mockk()
    }

    @Test
    fun `uiState should start with Loading state`() = testScope.runTest {
        // Given
        every { repository.getShortnedLinks() } returns flowOf(emptyList())

        // When
        createViewModel()

        // Then
        assert(homeViewModel.uiState.value is HomeUIState.Loading)
    }

    @Test
    fun `uiState should emit Empty when repository returns empty list`() = testScope.runTest {
        // Given
        every { repository.getShortnedLinks() } returns flowOf(emptyList())

        // When
        createViewModel()
        forceStateFlowToStart()

        // Then
        assert(homeViewModel.uiState.value is HomeUIState.Empty)
    }

    @Test
    fun `uiState should emit Success when repository returns links`() = testScope.runTest {
        // Given
        val mockLinks = listOf(
            mockk<LinkItem>(),
            mockk<LinkItem>()
        )
        every { repository.getShortnedLinks() } returns flowOf(mockLinks)

        // When
        createViewModel()
        forceStateFlowToStart()

        // Then
        val state = homeViewModel.uiState.value
        assert(state is HomeUIState.Success)
        assert((state as HomeUIState.Success).links == mockLinks)
    }

    @Test
    fun `uiState should emit Error when repository throws exception`() = testScope.runTest {
        // Given
        val errorMessage = "Network error"
        every { repository.getShortnedLinks() } returns flow {
            throw RuntimeException(errorMessage)
        }

        // When
        createViewModel()
        forceStateFlowToStart()

        // Then
        val state = homeViewModel.uiState.value
        assert(state is HomeUIState.Error)
        assert((state as HomeUIState.Error).message == errorMessage)
    }

    @Test
    fun `onIntent with OnShortLink should emit loading and success events`() = testScope.runTest {
        // Given
        every { repository.getShortnedLinks() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("https://example.com") } returns Result.success(
            ShortLinkUseCase.ShortLinkResultState.Success
        )
        createViewModel()

        val events = mutableListOf<HomeUIEvent>()
        val job = launch {
            homeViewModel.event.toList(events)
        }

        // When
        homeViewModel.onIntent(HomeUIIntent.OnShortLink("https://example.com"))
        advanceUntilIdle()

        // Then
        assert(events.contains(HomeUIEvent.OnShortLinkLoading))
        assert(events.contains(HomeUIEvent.OnShortLinkSuccess))

        job.cancel()
    }

    @Test
    fun `onIntent with OnShortLink should emit loading and invalid link events`() = testScope.runTest {
        // Given
        every { repository.getShortnedLinks() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("invalid") } returns Result.success(
            ShortLinkUseCase.ShortLinkResultState.InvalidLinkError
        )
        createViewModel()

        val events = mutableListOf<HomeUIEvent>()
        val job = launch {
            homeViewModel.event.toList(events)
        }

        // When
        homeViewModel.onIntent(HomeUIIntent.OnShortLink("invalid"))
        advanceUntilIdle()

        // Then
        assert(events.contains(HomeUIEvent.OnShortLinkLoading))
        assert(events.contains(HomeUIEvent.OnShortLinkInvalidLink))

        job.cancel()
    }

    @Test
    fun `onIntent with OnShortLink should emit loading and network error events`() = testScope.runTest {
        // Given
        every { repository.getShortnedLinks() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("https://example.com") } returns Result.success(
            ShortLinkUseCase.ShortLinkResultState.NetworkError
        )
        createViewModel()

        val events = mutableListOf<HomeUIEvent>()
        val job = launch {
            homeViewModel.event.toList(events)
        }

        // When
        homeViewModel.onIntent(HomeUIIntent.OnShortLink("https://example.com"))
        advanceUntilIdle()

        // Then
        assert(events.contains(HomeUIEvent.OnShortLinkLoading))
        assert(events.contains(HomeUIEvent.OnShortLinkNetworkError))

        job.cancel()
    }

    @Test
    fun `onIntent with OnShortLink should emit network error when use case fails`() = testScope.runTest {
        // Given
        every { repository.getShortnedLinks() } returns flowOf(emptyList())
        coEvery { shortLinkUseCase("https://example.com") } returns Result.failure(
            RuntimeException("Network failure")
        )
        createViewModel()

        val events = mutableListOf<HomeUIEvent>()
        val job = launch {
            homeViewModel.event.toList(events)
        }

        // When
        homeViewModel.onIntent(HomeUIIntent.OnShortLink("https://example.com"))
        advanceUntilIdle()

        // Then
        assert(events.contains(HomeUIEvent.OnShortLinkLoading))
        assert(events.contains(HomeUIEvent.OnShortLinkNetworkError))

        job.cancel()
    }

    @Test
    fun `onIntent with OnNavigateToLink should emit navigate event`() = testScope.runTest {
        // Given
        every { repository.getShortnedLinks() } returns flowOf(emptyList())
        createViewModel()

        val events = mutableListOf<HomeUIEvent>()
        val job = launch {
            homeViewModel.event.toList(events)
        }

        // When
        val url = "https://example.com"
        homeViewModel.onIntent(HomeUIIntent.OnNavigateToLink(url))
        advanceUntilIdle()

        // Then
        assert(events.contains(HomeUIEvent.OnNavigateToLink(url)))

        job.cancel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        homeViewModel =
            HomeViewModel(shortLinkUseCase = shortLinkUseCase, linkRepository = repository)
    }

    private fun TestScope.forceStateFlowToStart() {
        val states = mutableListOf<HomeUIState>()
        val job = launch {
            homeViewModel.uiState.take(2).toList(states)
        }
        advanceUntilIdle()
        job.cancel()
    }

}
