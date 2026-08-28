package io.qualtive

import io.qualtive.internal.QualtiveImpl
import io.qualtive.internal.network.HttpUrlConnectionEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.Locale

class EnquiryFetchIntegrationTest {
    @Test
    fun fetchSuccessFromLiveApi() = runTest {
        val client =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = HttpUrlConnectionEngine(),
                config = QualtiveConfig(locale = Locale.US),
            )

        val enquiry = client.fetchEnquiry("android")
        assertEquals(5326232893784064L, enquiry.id)
        assertEquals("android", enquiry.slug)
        assertEquals("Android?", enquiry.name)
        assertTrue(enquiry.pages.isNotEmpty())
        assertTrue(enquiry.pages[0].content.isNotEmpty())
        assertTrue(enquiry.submittedPages.isNotEmpty())
        assertEquals("ci-test", enquiry.container.id)
    }

    @Test
    fun fetchSuccessFromLiveApiWithWorkspace() = runTest {
        val client =
            QualtiveImpl(
                containerId = "ci-test",
                workspaceId = "ci-test-2",
                httpEngine = HttpUrlConnectionEngine(),
                config = QualtiveConfig(locale = Locale.US),
            )

        val enquiry = client.fetchEnquiry("android-2")
        assertEquals(5844484854120448L, enquiry.id)
        assertEquals("android-2", enquiry.slug)
        assertEquals("Android?", enquiry.name)
        assertTrue(enquiry.pages.isNotEmpty())
        assertTrue(enquiry.pages[0].content.isNotEmpty())
        assertTrue(enquiry.submittedPages.isNotEmpty())
        assertEquals("ci-test", enquiry.container.id)
    }

    @Test
    fun fetchNotFoundFromLiveApi() = runTest {
        val client =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = HttpUrlConnectionEngine(),
                config = QualtiveConfig(locale = Locale.US),
            )

        try {
            client.fetchEnquiry("does-not-exists")
            fail("expected NotFound")
        } catch (_: QualtiveException.NotFound) {
            // expected
        }
    }
}
