package com.gabrielsantos.shortnify.data

import android.util.Log
import com.gabrielsantos.shortnify.data.local.LocalDataSource
import com.gabrielsantos.shortnify.data.network.LinkAPI
import com.gabrielsantos.shortnify.data.network.LinkRequest
import com.gabrielsantos.shortnify.data.network.LinkResponse
import com.gabrielsantos.shortnify.data.network.Links
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.IOException

class LinkRepositoryImplTest {

    private lateinit var linkAPI: LinkAPI
    private lateinit var localDataSource: LocalDataSource
    private lateinit var linkRepository: LinkRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.e(any(), any<String>()) } returns 0 // Mock Log.e
        every { Log.e(any(), any<String>(), any()) } returns 0 // Mock Log.e with Throwable
        linkAPI = mockk()
        localDataSource = mockk(relaxed = true) // relaxed = true to avoid stubbing every call
        linkRepository = LinkRepositoryImpl(linkAPI, localDataSource)
    }

    @Test
    fun `shortLink happyPath emitsLoadingThenSuccess`() = runBlocking {
        // Verify that shortLink emits NetworkRequestState.Loading initially, and then NetworkRequestState.Success after a successful API call and local data source update.
        val originalLink = "https://example.com"
        val shortenedLink = "https://short.ly/abc"
        val shortUrlResponse = LinkResponse(alias = "123", links = Links(self = "www.selfLink.com", short = "www.shortlink.com"))

        coEvery { linkAPI.shortLink(LinkRequest(originalLink)) } returns shortUrlResponse
        coEvery { localDataSource.addShortenedUrl(shortenedLink) } returns Unit // Assuming it returns Unit

        val emissions = linkRepository.shortLink(originalLink).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is NetworkRequestState.Loading)
        assertTrue(emissions[1] is NetworkRequestState.Success)
    }

    @Test
    fun `shortLink networkInterruption emitsError`() {
        // Simulate a network interruption during the API call. Verify that the flow emits NetworkRequestState.Error.
        val originalLink = "https://example.com"
        val networkException = IOException("Network error")

        coEvery { linkAPI.shortLink(LinkRequest(originalLink)) } throws networkException

        runBlocking {
            val emissions = linkRepository.shortLink(originalLink).toList()

            assertEquals(2, emissions.size)
            assertTrue(emissions[0] is NetworkRequestState.Loading)
            assertTrue(emissions[1] is NetworkRequestState.Error)
        }
    }

    @Test
    fun `getShortnedLinks callsLocalDataSource`() {
        // Verify that getShortnedLinks directly calls and returns the Flow from localDataSource.getShortenedUrls.
        val expectedLinks = listOf(LinkData(1, "https://sh1.co"))
        coEvery { localDataSource.getShortenedUrls() } returns flowOf(expectedLinks)

        runBlocking {
            val resultFlow = linkRepository.getShortnedLinks()
            assertEquals(expectedLinks, resultFlow.first())
            coVerify(exactly = 1) { localDataSource.getShortenedUrls() }
        }
    }

    @Test
    fun `getShortnedLinks emptyList emitsEmptyFlow`() {
        // Verify that if localDataSource.getShortenedUrls returns a Flow of an empty list, getShortnedLinks also emits a Flow of an empty list.
        val emptyList = emptyList<LinkData>()
        coEvery { localDataSource.getShortenedUrls() } returns flowOf(emptyList)

        runBlocking {
            val resultFlow = linkRepository.getShortnedLinks()
            assertTrue(resultFlow.first().isEmpty())
        }
    }

    @Test
    fun `getShortnedLinks populatedList emitsPopulatedFlow`() {
        // Verify that if localDataSource.getShortenedUrls returns a Flow of a non-empty list of LinkData, getShortnedLinks emits the same Flow.
        val populatedList = listOf(
            LinkData(1, "https://short.ly/ex1"),
            LinkData(2, "https://short.ly/ex2")
        )
        coEvery { localDataSource.getShortenedUrls() } returns flowOf(populatedList)

        runBlocking {
            val resultFlow = linkRepository.getShortnedLinks()
            assertEquals(populatedList, resultFlow.first())
        }
    }

}