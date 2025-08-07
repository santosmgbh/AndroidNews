package com.gabrielsantos.shortnify.data

import com.gabrielsantos.shortnify.data.local.LocalDataSource
import com.gabrielsantos.shortnify.data.network.LinkAPI
import com.gabrielsantos.shortnify.data.network.LinkRequest
import com.gabrielsantos.shortnify.data.network.LinkResponse
import com.gabrielsantos.shortnify.data.network.Links
import com.gabrielsantos.shortnify.domain.LinkItem
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class LinkRepositoryImplTest {

    private lateinit var linkAPI: LinkAPI
    private lateinit var localDataSource: LocalDataSource
    private lateinit var linkRepository: LinkRepositoryImpl

    @Before
    fun setUp() {
        linkAPI = mockk()
        localDataSource = mockk()
        linkRepository = LinkRepositoryImpl(linkAPI, localDataSource)
    }

    @Test
    fun `shortLink returns success`() = runBlocking {
        // Given
        val originalLink = "https://example.com"
        val shortenedLink = "https://short.ly/abc"
        val shortUrlResponse = LinkResponse(alias = "123", links = Links(self = "www.selfLink.com", short = "www.shortlink.com"))

        coEvery { linkAPI.shortLink(LinkRequest(originalLink)) } returns shortUrlResponse
        coEvery { localDataSource.addShortenedUrl(shortenedLink) } returns Unit

        // When
        val result = linkRepository.shortLink(originalLink)

        // Then
        assertTrue(result.isSuccess)
        coVerify { localDataSource.addShortenedUrl(shortenedLink) }
    }

    @Test
    fun `shortLink returns failure`() = runBlocking {
        // Given
        val originalLink = "https://example.com"
        val exception = Exception("Network error")
        coEvery { linkAPI.shortLink(LinkRequest(originalLink)) } throws exception

        // When
        val result = linkRepository.shortLink(originalLink)

        // Then
        assertTrue(result.isFailure)
        coVerify { localDataSource.addShortenedUrl(any()) wasNot called }
    }

    @Test
    fun `getShortnedLinks returns flow from local data source`() {
        // Given
        val expectedFlow = mockk<Flow<List<LinkItem>>>()
        coEvery { localDataSource.getShortenedUrls() } returns expectedFlow

        // When
        val resultFlow = linkRepository.getShortnedLinks()

        // Then
        assertTrue(resultFlow === expectedFlow)
    }

    @Test
    fun `getShortnedLinks returns empty list when local data source is empty`() {

        // Given
        val expectedFlow = emptyFlow<List<LinkItem>>()
        coEvery { localDataSource.getShortenedUrls() } returns expectedFlow

        // When
        val result = linkRepository.getShortnedLinks()

        // Then
        assertTrue(result === expectedFlow)
    }
}