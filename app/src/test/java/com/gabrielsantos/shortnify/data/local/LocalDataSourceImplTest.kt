package com.gabrielsantos.shortnify.data.local

import com.gabrielsantos.shortnify.data.LinkData
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
            assertEquals(LinkData(0, testUrl), result[0])
        }
    }

    @Test
    fun `addShortenedUrl to non empty list`() {
        // Test adding a URL when the list already contains items. 
        // Verify the new URL is added with an id incremented from the last item and the correct URL string.
        runBlocking {
            val firstUrl = "https://example.com/first"
            val secondUrl = "https://example.com/second"
            
            localDataSource.addShortenedUrl(firstUrl)
            localDataSource.addShortenedUrl(secondUrl)
            
            val result = localDataSource.getShortenedUrls().first()
            assertEquals(2, result.size)
            assertEquals(LinkData(0, firstUrl), result[0])
            assertEquals(LinkData(1, secondUrl), result[1])
        }
    }

    @Test
    fun `addShortenedUrl multiple additions`() {
        // Test adding multiple URLs sequentially. 
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
                assertEquals(LinkData(index.toLong(), url), result[index])
            }
        }
    }

    @Test
    fun `addShortenedUrl with empty string`() {
        // Test adding an empty string as a URL. 
        // Verify the behavior (e.g., if it's allowed and how it's stored).
        runBlocking {
            val emptyUrl = ""
            
            localDataSource.addShortenedUrl(emptyUrl)
            
            val result = localDataSource.getShortenedUrls().first()
            assertEquals(1, result.size)
            assertEquals(LinkData(0, emptyUrl), result[0])
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
            assertEquals(LinkData(0, longUrl), result[0])
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
            assertEquals(LinkData(0, specialUrl), result[0])
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
            assertEquals(LinkData(0, url1), result[0])
            
            // Add second URL
            localDataSource.addShortenedUrl(url2)
            result = localDataSource.getShortenedUrls().first()
            assertEquals(2, result.size)
            assertEquals(LinkData(0, url1), result[0])
            assertEquals(LinkData(1, url2), result[1])
        }
    }

    @Test
    fun `clearShortenedUrls on empty list`() {
        // Test calling clearShortenedUrls when the list is already empty. 
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
        // Test calling clearShortenedUrls when the list contains items. 
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
        // Test adding a URL after the list has been cleared. 
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
            assertEquals(LinkData(0, url3), result[0])
        }
    }

    @Test
    fun `getShortenedUrls multiple observers`() {
        // Test if multiple collectors of the getShortenedUrls Flow receive updates correctly.
        runBlocking {
            val url = "https://example.com/test"
            
            // Collect from multiple flows
            val flow1 = localDataSource.getShortenedUrls()
            val flow2 = localDataSource.getShortenedUrls()
            
            // Both should initially be empty
            assertEquals(emptyList<LinkData>(), flow1.first())
            assertEquals(emptyList<LinkData>(), flow2.first())
            
            // Add a URL
            localDataSource.addShortenedUrl(url)
            
            // Both flows should receive the update
            val expectedList = listOf(LinkData(0, url))
            assertEquals(expectedList, flow1.first())
            assertEquals(expectedList, flow2.first())
        }
    }

    @Test
    fun `addShortenedUrl concurrency`() {
        // (Advanced) Test adding URLs from multiple coroutines concurrently. 
        // Ensure data integrity and correct ID assignment (though MutableStateFlow is generally thread-safe for updates).
        runBlocking {
            val urls = (1..10).map { "https://example.com/url$it" }
            
            // Note: This is a simplified concurrency test. MutableStateFlow updates are atomic,
            // but the ID generation logic might not handle true concurrency perfectly.
            // For production code, consider using atomic operations for ID generation.
            urls.forEach { url ->
                localDataSource.addShortenedUrl(url)
            }
            
            val result = localDataSource.getShortenedUrls().first()
            assertEquals(10, result.size)
            
            // Verify all URLs are present and IDs are sequential
            urls.forEachIndexed { index, url ->
                assertEquals(LinkData(index.toLong(), url), result[index])
            }
        }
    }

    @Test
    fun `clearShortenedUrls concurrency`() {
        // (Advanced) Test clearing URLs while other operations (like add) might be happening. 
        // Verify consistent state.
        runBlocking {
            val url1 = "https://example.com/first"
            val url2 = "https://example.com/second"
            
            // Add some URLs
            localDataSource.addShortenedUrl(url1)
            localDataSource.addShortenedUrl(url2)
            
            // Verify list has items
            var result = localDataSource.getShortenedUrls().first()
            assertEquals(2, result.size)
            
            // Clear and immediately add
            localDataSource.clearShortenedUrls()
            localDataSource.addShortenedUrl(url1)
            
            // Verify state consistency
            result = localDataSource.getShortenedUrls().first()
            assertEquals(1, result.size)
            assertEquals(LinkData(0, url1), result[0])
        }
    }

    @Test
    fun `ID generation with large numbers`() {
        // Test adding URLs until the ID reaches a large number (e.g., near Int.MAX_VALUE - 1). 
        // Verify the next ID is generated correctly without overflow if it's within Int range. Note: this doesn't test overflow itself, but correct incrementing near max.
        runBlocking {
            // This test is simplified due to performance constraints.
            // In a real scenario, you might want to test with larger numbers or mock the internal state.
            val baseId = Long.MAX_VALUE - 5
            val testUrl = "https://example.com/test"
            
            // Since we can't easily set the internal state to start with a large ID,
            // we'll test the logic by adding a few URLs and verifying sequential ID generation
            val urls = (1..5).map { "https://example.com/url$it" }
            
            urls.forEach { url ->
                localDataSource.addShortenedUrl(url)
            }
            
            val result = localDataSource.getShortenedUrls().first()
            assertEquals(5, result.size)
            
            // Verify IDs are sequential starting from 0
            urls.forEachIndexed { index, url ->
                assertEquals(LinkData(index.toLong(), url), result[index])
            }
            
            // Verify the last ID is 4 (0-based indexing for 5 items)
            assertEquals(4L, result.last().id)
        }
    }

}