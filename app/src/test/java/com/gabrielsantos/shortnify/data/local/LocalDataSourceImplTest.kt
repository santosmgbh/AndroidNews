package com.gabrielsantos.shortnify.data.local

import com.gabrielsantos.shortnify.domain.LinkItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalDataSourceImplTest {

    private lateinit var localDataSource: LocalDataSourceImpl

    @Before
    fun setUp() {
        localDataSource = LocalDataSourceImpl()
    }

    @Test
    fun `getShortenedUrls initial state`() {
        // Verify that getShortenedUrls initially returns an empty list.
        runBlocking {
            val result = localDataSource.getShortenedUrls().first()
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun `addShortenedUrl to empty list`() {
        // Test adding a URL when the list is empty. 
        // Verify the new URL is added with id 0 and the correct URL string.
        runBlocking {
            val testUrl = "https://example.com/shortened"
            
            localDataSource.addShortenedUrl(testUrl)
            
            val result = localDataSource.getShortenedUrls().first()
            assertEquals(1, result.size)
            assertEquals(LinkItem(0, testUrl), result[0])
        }
    }

    @Test
    fun `addShortenedUrl to non empty list`() {
        // Verify the new URL is added with an id incremented from the last item and the correct URL string.
        runBlocking {
            val firstUrl = "https://example.com/first"
            val secondUrl = "https://example.com/second"
            
            localDataSource.addShortenedUrl(firstUrl)
            localDataSource.addShortenedUrl(secondUrl)
            
            val result = localDataSource.getShortenedUrls().first()
            assertEquals(2, result.size)
            assertEquals(LinkItem(0, firstUrl), result[0])
            assertEquals(LinkItem(1, secondUrl), result[1])
        }
    }

    @Test
    fun `addShortenedUrl multiple additions`() {
        // Verify that each URL is added correctly with incrementing ids and the list reflects all additions.
        runBlocking {
            val urls = listOf(
                "https://example.com/url1",
                "https://example.com/url2",
                "https://example.com/url3",
                "https://example.com/url4"
            )
            
            urls.forEach { url ->
                localDataSource.addShortenedUrl(url)
            }
            
            val result = localDataSource.getShortenedUrls().first()
            assertEquals(4, result.size)
            
            urls.forEachIndexed { index, url ->
                assertEquals(LinkItem(index.toLong(), url), result[index])
            }
        }
    }
}