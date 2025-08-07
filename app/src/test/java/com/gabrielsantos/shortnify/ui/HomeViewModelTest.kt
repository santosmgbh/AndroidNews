package com.gabrielsantos.shortnify.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.gabrielsantos.shortnify.domain.LinkRepository
import com.gabrielsantos.shortnify.ui.entities.HomeUIState
import com.gabrielsantos.shortnify.ui.entities.ShortLinkUIState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private lateinit var repository: LinkRepository
    private lateinit var homeViewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        homeViewModel = HomeViewModel(linkRepository = repository)
    }

    @Test
    fun `getUiState initial state`() = testScope.runTest {
        // Verify that the initial value of uiState is HomeUIState.Loading.
        every { repository.getShortnedLinks() } returns flowOf(emptyList())

        createViewModel()

        // The initial value should be Loading as defined in stateIn
        assertEquals(HomeUIState.Loading, homeViewModel.uiState.value)
    }


    @Test
    fun `shortLink error`() = testScope.runTest {
        // When shortLinkUseCase emits ShortLinkUIState.Error, verify _shortLinkUIState is updated accordingly.
        every { repository.getShortnedLinks() } returns flowOf(emptyList())
        coEvery { repository.shortLink("test-link") } returns flowOf(ShortLinkUIState.Error)

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