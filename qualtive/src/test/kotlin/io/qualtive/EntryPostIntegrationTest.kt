package io.qualtive

import io.qualtive.internal.QualtiveImpl
import io.qualtive.internal.network.HttpUrlConnectionEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.Locale

class EntryPostIntegrationTest {
    @Test
    fun postSuccessFromLiveApi() = runTest {
        val client =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = HttpUrlConnectionEngine(),
                config = QualtiveConfig(locale = Locale.US),
            )

        val entry =
            client.post(
                enquiryId = "android",
                content =
                listOf(
                    Entry.Content.Score(
                        value = 75,
                        definition =
                        Page.Content.Score(
                            scoreType = ScoreType.Smilies5,
                            leadingText = null,
                            trailingText = null,
                        ),
                    ),
                    Entry.Content.Text(value = "Hello world!"),
                    Entry.Content.Select(value = "Selected"),
                    Entry.Content.Multiselect(values = listOf("Multi 1", "Multi 2")),
                ),
                user = User(id = "ci-android"),
                customAttributes = mapOf("Age" to "23"),
                options =
                PostOptions(
                    metadataCollection = MetadataCollection.None,
                    userTrackingConsent = UserTrackingConsent.Denied,
                ),
            )

        val id = entry.id
        assertTrue(id != null && id > 0)
    }

    @Test
    fun postSuccessFromLiveApiWithWorkspace() = runTest {
        val client =
            QualtiveImpl(
                containerId = "ci-test",
                workspaceId = "ci-test-2",
                httpEngine = HttpUrlConnectionEngine(),
                config = QualtiveConfig(locale = Locale.US),
            )

        val entry =
            client.post(
                enquiryId = "android-2",
                content =
                listOf(
                    Entry.Content.Score(
                        value = 75,
                        definition =
                        Page.Content.Score(
                            scoreType = ScoreType.Smilies5,
                            leadingText = null,
                            trailingText = null,
                        ),
                    ),
                    Entry.Content.Text(value = "Hello world!"),
                    Entry.Content.Select(value = "Selected"),
                    Entry.Content.Multiselect(values = listOf("Multi 1", "Multi 2")),
                ),
                user = User(id = "ci-android"),
                customAttributes = mapOf("Age" to "23"),
                options =
                PostOptions(
                    metadataCollection = MetadataCollection.None,
                    userTrackingConsent = UserTrackingConsent.Denied,
                ),
            )

        val id = entry.id
        assertTrue(id != null && id > 0)
    }

    @Test
    fun postNotFoundFromLiveApi() = runTest {
        val client =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = HttpUrlConnectionEngine(),
                config = QualtiveConfig(locale = Locale.US),
            )

        try {
            client.post(
                enquiryId = "does-not-exists",
                content = listOf(Entry.Content.Text(value = "Hello")),
            )
            fail("expected NotFound")
        } catch (_: QualtiveException.NotFound) {
            // expected
        }
    }
}
