package com.gabrielsantos.shortnify.domain

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ShortLinkUseCaseTest {

    private lateinit var linkRepository: LinkRepository
    private lateinit var shortLinkUseCase: ShortLinkUseCase

    @Before
    fun setup() {
        linkRepository = mockk()
        shortLinkUseCase = ShortLinkUseCase(linkRepository)
    }

    @Test
    fun `invoke with invalid URL should return InvalidLinkError`() = runTest {
        // Given
        val invalidUrl = "invalid-url"

        coEvery { linkRepository.shortLink(invalidUrl) } returns Result.success(Unit)

        // When
        val result = shortLinkUseCase(invalidUrl)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(ShortLinkUseCase.ShortLinkResultState.InvalidLinkError, result.getOrNull())
    }

    @Test
    fun `invoke with empty string should return InvalidLinkError`() = runTest {
        // Given
        val emptyUrl = ""

        // When
        val result = shortLinkUseCase.invoke(emptyUrl)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(ShortLinkUseCase.ShortLinkResultState.InvalidLinkError, result.getOrNull())
    }

    @Test
    fun `invoke with valid URL and successful repository response should return Success`() = runTest {
        // Given
        val validUrl = "https://www.example.com"
        coEvery { linkRepository.shortLink(validUrl) } returns Result.success(Unit)

        // When
        val result = shortLinkUseCase.invoke(validUrl)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(ShortLinkUseCase.ShortLinkResultState.Success, result.getOrNull())
    }

    @Test
    fun `invoke with valid URL and failed repository response should return NetworkError`() = runTest {
        // Given
        val validUrl = "https://www.example.com"
        coEvery { linkRepository.shortLink(validUrl) } returns Result.failure(Exception("Network error"))

        // When
        val result = shortLinkUseCase.invoke(validUrl)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(ShortLinkUseCase.ShortLinkResultState.NetworkError, result.getOrNull())
    }

    @Test
    fun `invoke with valid HTTP URL should work correctly`() = runTest {
        // Given
        val validUrl = "http://www.example.com"
        coEvery { linkRepository.shortLink(validUrl) } returns Result.success(Unit)

        // When
        val result = shortLinkUseCase.invoke(validUrl)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(ShortLinkUseCase.ShortLinkResultState.Success, result.getOrNull())
    }

    @Test
    fun `invoke with malformed URL should return InvalidLinkError`() = runTest {
        // Given
        val malformedUrl = "htp://example"

        // When
        val result = shortLinkUseCase.invoke(malformedUrl)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(ShortLinkUseCase.ShortLinkResultState.InvalidLinkError, result.getOrNull())
    }
}