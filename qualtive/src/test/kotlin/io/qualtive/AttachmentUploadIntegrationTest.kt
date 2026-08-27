package io.qualtive

import io.qualtive.internal.QualtiveImpl
import io.qualtive.internal.network.HttpUrlConnectionEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AttachmentUploadIntegrationTest {
    @Test
    fun uploadPngThenPostWithAttachmentFromLiveApi() = runTest {
        val client =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = HttpUrlConnectionEngine(),
                config = QualtiveConfig(locale = Locale.US),
            )

        val samplePng =
            checkNotNull(
                javaClass.classLoader!!.getResourceAsStream("sample.png"),
            ) { "missing test resource sample.png" }.use { it.readBytes() }

        val attachment =
            client.uploadAttachment(
                bytes = samplePng,
                contentType = AttachmentContentType.ImagePng,
            )
        assertTrue(attachment.id > 0)

        val entry =
            client.post(
                enquiryId = "android",
                content =
                listOf(
                    Entry.Content.Text(value = "Attachment upload integration"),
                    Entry.Content.Attachments(attachments = listOf(attachment)),
                ),
                user = User(id = "ci-android-attachment"),
                options =
                PostOptions(
                    metadataCollection = MetadataCollection.None,
                    userTrackingConsent = UserTrackingConsent.Denied,
                ),
            )

        val id = entry.id
        assertTrue(id != null && id > 0)
    }
}
