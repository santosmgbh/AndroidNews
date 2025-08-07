package com.gabrielsantos.shortnify.data

import com.gabrielsantos.shortnify.data.local.LocalDataSource
import com.gabrielsantos.shortnify.data.network.LinkAPI
import com.gabrielsantos.shortnify.data.network.LinkRequest
import com.gabrielsantos.shortnify.data.network.LinkResponse
import com.gabrielsantos.shortnify.data.network.Links
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
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
        localDataSource = mockk(relaxed = true) // relaxed = true to avoid stubbing every call
        linkRepository = LinkRepositoryImpl(linkAPI, localDataSource)
    }

    @Test
    fun `shortLink returns success`() = runBlocking {
        // Verify that shortLink returns success when the API call is successful.
        val originalLink = "https://example.com"
        val shortenedLink = "https://short.ly/abc"
        val shortUrlResponse = LinkResponse(alias = "123", links = Links(self = "www.selfLink.com", short = "www.shortlink.com"))

        coEvery { linkAPI.shortLink(LinkRequest(originalLink)) } returns shortUrlResponse
        coEvery { localDataSource.addShortenedUrl(shortenedLink) } returns Unit // Assuming it returns Unit

        val result = linkRepository.shortLink(originalLink)

        assertTrue(result.isSuccess)
    }

}