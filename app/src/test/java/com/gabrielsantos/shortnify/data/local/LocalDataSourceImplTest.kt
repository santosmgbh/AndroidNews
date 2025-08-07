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

    @Test
    fun `addShortenedUrl with empty string`() {
        // Verify the behavior (e.g., if it's allowed and how it's stored).
        runBlocking {
            val emptyUrl = ""
            
            localDataSource.addShortenedUrl(emptyUrl)
            
            val result = localDataSource.getShortenedUrls().first()
            assertEquals(1, result.size)
            assertEquals(LinkItem(0, emptyUrl), result[0])
        }
    }

    @Test
    fun `addShortenedUrl with very long string`() {
        // Test adding a very long string as a URL. 
        // Verify the system handles it without issues (e.g., truncation, errors).
        runBlocking {
            val longUrl = "https://example.com/" + "x".repeat(1000)
            
            localDataSource.addShortenedUrl(longUrl)
            
            val result = localDataSource.getShortenedUrls().first()
            assertEquals(1, result.size)
            assertEquals(LinkItem(0, longUrl), result[0])
        }
    }

    @Test
    fun `addShortenedUrl with special characters`() {
        // Test adding a URL containing special characters. 
        // Verify proper storage and retrieval.
        runBlocking {
            val specialUrl = "https://example.com/path?param=value&special=@#$%^&*()!+=[]{}|;':\",./<>?"
            
            localDataSource.addShortenedUrl(specialUrl)
            
            val result = localDataSource.getShortenedUrls().first()
            assertEquals(1, result.size)
            assertEquals(LinkItem(0, specialUrl), result[0])
        }
    }

    @Test
    fun `getShortenedUrls reflects additions`() {
        // After adding URLs, verify that getShortenedUrls emits the updated list.
        runBlocking {
            val url1 = "https://example.com/first"
            val url2 = "https://example.com/second"
            
            // Initially empty
            var result = localDataSource.getShortenedUrls().first()
            assertTrue(result.isEmpty())
            
            // Add first URL
            localDataSource.addShortenedUrl(url1)
            result = localDataSource.getShortenedUrls().first()
            assertEquals(1, result.size)
            assertEquals(LinkItem(0, url1), result[0])
            
            // Add second URL
            localDataSource.addShortenedUrl(url2)
            result = localDataSource.getShortenedUrls().first()
            assertEquals(2, result.size)
            assertEquals(LinkItem(0, url1), result[0])
            assertEquals(LinkItem(1, url2), result[1])
        }
    }

    @Test
    fun `clearShortenedUrls on empty list`() {
        // Verify the list remains empty and no errors occur.
        runBlocking {
            // Verify initial empty state
            var result = localDataSource.getShortenedUrls().first()
            assertTrue(result.isEmpty())
            
            // Clear empty list
            localDataSource.clearShortenedUrls()
            
            // Verify still empty
            result = localDataSource.getShortenedUrls().first()
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun `clearShortenedUrls on non empty list`() {
        // Verify the list becomes empty.
        runBlocking {
            val url1 = "https://example.com/first"
            val url2 = "https://example.com/second"
            
            // Add some URLs
            localDataSource.addShortenedUrl(url1)
            localDataSource.addShortenedUrl(url2)
            
            // Verify list has items
            var result = localDataSource.getShortenedUrls().first()
            assertEquals(2, result.size)
            
            // Clear the list
            localDataSource.clearShortenedUrls()
            
            // Verify list is empty
            result = localDataSource.getShortenedUrls().first()
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun `getShortenedUrls after clear`() {
        // After clearing the list, verify that getShortenedUrls emits an empty list.
        runBlocking {
            val url = "https://example.com/test"
            
            // Add a URL
            localDataSource.addShortenedUrl(url)
            var result = localDataSource.getShortenedUrls().first()
            assertEquals(1, result.size)
            
            // Clear the list
            localDataSource.clearShortenedUrls()
            
            // Verify getShortenedUrls returns empty list
            result = localDataSource.getShortenedUrls().first()
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun `addShortenedUrl after clear`() {
        // Verify the new URL is added with id 0.
        runBlocking {
            val url1 = "https://example.com/first"
            val url2 = "https://example.com/second"
            val url3 = "https://example.com/after-clear"
            
            // Add some URLs
            localDataSource.addShortenedUrl(url1)
            localDataSource.addShortenedUrl(url2)
            
            // Clear the list
            localDataSource.clearShortenedUrls()
            
            // Add a new URL after clearing
            localDataSource.addShortenedUrl(url3)
            
            // Verify the new URL starts with id 0
            val result = localDataSource.getShortenedUrls().first()
            assertEquals(1, result.size)
            assertEquals(LinkItem(0, url3), result[0])
        }
    }
}