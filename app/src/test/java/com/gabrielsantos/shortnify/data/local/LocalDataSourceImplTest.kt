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
    fun `getShortenedUrls initial state`() = runBlocking {
        val result = localDataSource.getShortenedUrls().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `addShortenedUrl to empty list`() = runBlocking {
        // Given
        val testUrl = "https://example.com/shortened"
        val expected = LinkItem(0, testUrl)

        // When
        localDataSource.addShortenedUrl(testUrl)

        // Then
        val result = localDataSource.getShortenedUrls().first()
        assertEquals(1, result.size)
        assertEquals(expected, result[0])
    }

    @Test
    fun `addShortenedUrl to non empty list`() = runBlocking {
        // Given
        val firstUrl = "https://example.com/first"
        val secondUrl = "https://example.com/second"

        val expectedFirst = LinkItem(0, firstUrl)
        val expectedSecond = LinkItem(1, secondUrl)

        // When
        localDataSource.addShortenedUrl(firstUrl)
        localDataSource.addShortenedUrl(secondUrl)

        // Then
        val result = localDataSource.getShortenedUrls().first()
        assertEquals(2, result.size)
        assertEquals(expectedFirst, result[0])
        assertEquals(expectedSecond, result[1])
    }

    @Test
    fun `addShortenedUrl multiple additions`() = runBlocking {
        // Given
        val urls = listOf(
            "https://example.com/url1",
            "https://example.com/url2",
            "https://example.com/url3",
            "https://example.com/url4"
        )
        val expectedList = listOf(
            LinkItem(0, urls[0]),
            LinkItem(1, urls[1]),
            LinkItem(2, urls[2]),
            LinkItem(3, urls[3])
        )

        // When
        urls.forEach { url ->
            localDataSource.addShortenedUrl(url)
        }

        // Then
        val result = localDataSource.getShortenedUrls().first()
        assertEquals(4, result.size)
        assertEquals(expectedList, result)
    }
}