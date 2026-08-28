package io.qualtive

import io.qualtive.internal.DeviceAttributesCollector
import io.qualtive.internal.InMemoryClientIdStore
import io.qualtive.internal.QualtiveImpl
import io.qualtive.internal.network.FakeHttpEngine
import io.qualtive.internal.network.HttpRequestBody
import io.qualtive.internal.network.HttpResponse
import io.qualtive.internal.network.readAllBytes
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.util.Locale

class QualtivePostTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun postOptionsDefaultToMetadataAndTrackingEnabled() {
        val options = PostOptions()
        assertEquals(MetadataCollection.NonPersonal, options.metadataCollection)
        assertEquals(UserTrackingConsent.Granted, options.userTrackingConsent)
    }

    @Test
    fun postSendsPerEntryUserAttributesAndOptions() = runTest {
        val clientIdStore = InMemoryClientIdStore()
        val engine =
            FakeHttpEngine { request ->
                assertEquals("POST", request.method)
                assertTrue(request.url.endsWith("/feedback/entries/"))
                assertEquals("ci-test", request.headers["X-Container"])
                assertFalse(request.headers.containsKey("X-Workspace"))

                val body =
                    json
                        .parseToJsonElement(
                            request.body.readAllBytes()!!.toString(Charsets.UTF_8),
                        ).jsonObject
                assertEquals("android", body["questionId"]!!.jsonPrimitive.content)

                val user = body["user"]!!.jsonObject
                assertEquals("user-1", user["id"]!!.jsonPrimitive.content)
                assertEquals("Ada", user["name"]!!.jsonPrimitive.content)
                assertEquals("ada@example.com", user["email"]!!.jsonPrimitive.content)
                assertTrue(user.containsKey("clientId"))

                val attributes = body["attributes"]!!.jsonObject
                assertEquals("Android", attributes["Platform"]!!.jsonPrimitive.content)
                assertEquals("pro", attributes["plan"]!!.jsonPrimitive.content)
                assertEquals("true", attributes["vip"]!!.jsonPrimitive.content)
                assertEquals("3", attributes["seats"]!!.jsonPrimitive.content)
                assertEquals("32", attributes["Age"]!!.jsonPrimitive.content)

                assertEquals(
                    "android",
                    body["attributeHints"]!!.jsonObject["clientLibrary"]!!.jsonPrimitive.content,
                )

                val content = body["content"]!!.jsonArray
                assertEquals(
                    listOf("score", "text", "attachments"),
                    content.map { it.jsonObject["type"]!!.jsonPrimitive.content },
                )

                HttpResponse(statusCode = 201, body = """{"id":123}""".toByteArray())
            }

        val client =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = engine,
                config = QualtiveConfig(locale = Locale.US),
                clientIdStore = clientIdStore,
                deviceAttributesCollector = FixedDeviceAttributes(),
            )

        val entry =
            client.post(
                enquiryId = "android",
                content =
                listOf(
                    Entry.Content.Score(
                        value = 80,
                        definition =
                        Page.Content.Score(
                            scoreType = ScoreType.Stars5,
                            leadingText = null,
                            trailingText = null,
                        ),
                    ),
                    Entry.Content.Text(
                        value = "Hello",
                        definition =
                        Page.Content.Text(
                            placeholder = null,
                            storageTarget = Page.Content.Text.StorageTarget.Text,
                        ),
                    ),
                    Entry.Content.Text(
                        value = "32",
                        definition =
                        Page.Content.Text(
                            placeholder = null,
                            storageTarget =
                            Page.Content.Text.StorageTarget.Attribute("Age"),
                        ),
                    ),
                    Entry.Content.Attachments(
                        attachments = listOf(Entry.AttachmentReference(id = 99)),
                    ),
                ),
                user = User(id = "user-1", name = "Ada", email = "ada@example.com"),
                customAttributes =
                mapOf(
                    "plan" to "pro",
                    "vip" to true,
                    "seats" to 3,
                ),
                options =
                PostOptions(
                    metadataCollection = MetadataCollection.NonPersonal,
                    userTrackingConsent = UserTrackingConsent.Granted,
                ),
            )

        assertEquals(123L, entry.id)
        assertEquals(1, engine.requests.size)
    }

    @Test
    fun postSendsWorkspaceHeaderWhenSet() = runTest {
        val engine =
            FakeHttpEngine { request ->
                assertEquals("my-department", request.headers["X-Workspace"])
                HttpResponse(statusCode = 201, body = """{"id":1}""".toByteArray())
            }

        val client =
            QualtiveImpl(
                containerId = "ci-test",
                workspaceId = "my-department",
                httpEngine = engine,
                config = QualtiveConfig(locale = Locale.US),
                clientIdStore = InMemoryClientIdStore(),
                deviceAttributesCollector = FixedDeviceAttributes(emptyMap()),
            )

        client.post("android", listOf(Entry.Content.Text(value = "Hi")))
        assertEquals("my-department", engine.requests.single().headers["X-Workspace"])
    }

    @Test
    fun postOmitsClientIdAndDeviceAttributesWhenOptionsRestrictPrivacy() = runTest {
        val clientIdStore = InMemoryClientIdStore()
        clientIdStore.getOrCreate()

        val engine =
            FakeHttpEngine { request ->
                val body =
                    json
                        .parseToJsonElement(
                            request.body.readAllBytes()!!.toString(Charsets.UTF_8),
                        ).jsonObject
                assertNull(body["user"]!!.jsonObject["clientId"])
                assertEquals(
                    "android",
                    body["attributeHints"]!!.jsonObject["clientLibrary"]!!.jsonPrimitive.content,
                )
                assertTrue(body["attributes"]!!.jsonObject.isEmpty())
                HttpResponse(statusCode = 201, body = """{"id":1}""".toByteArray())
            }

        val client =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = engine,
                config = QualtiveConfig(locale = Locale.US),
                clientIdStore = clientIdStore,
                deviceAttributesCollector = FixedDeviceAttributes(),
            )

        client.post(
            enquiryId = "android",
            content = listOf(Entry.Content.Text(value = "Hi")),
            options =
            PostOptions(
                metadataCollection = MetadataCollection.None,
                userTrackingConsent = UserTrackingConsent.Denied,
            ),
        )
    }

    @Test
    fun postNotFoundAndConnectionErrors() = runTest {
        val notFound =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = FakeHttpEngine { HttpResponse(404, ByteArray(0)) },
                config = QualtiveConfig(),
                clientIdStore = InMemoryClientIdStore(),
                deviceAttributesCollector = FixedDeviceAttributes(emptyMap()),
            )
        try {
            notFound.post("missing", listOf(Entry.Content.Text(value = "x")))
            fail("expected NotFound")
        } catch (_: QualtiveException.NotFound) {
            // expected
        }

        val connection =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = FakeHttpEngine { throw IOException("boom") },
                config = QualtiveConfig(),
                clientIdStore = InMemoryClientIdStore(),
                deviceAttributesCollector = FixedDeviceAttributes(emptyMap()),
            )
        try {
            connection.post("android", listOf(Entry.Content.Text(value = "x")))
            fail("expected Connection")
        } catch (error: QualtiveException.Connection) {
            assertTrue(error.cause is IOException)
        }
    }

    @Test
    fun uploadAttachmentPostsThenPutsStreamingBody() = runTest {
        val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
        var call = 0
        val engine =
            FakeHttpEngine { request ->
                when (call++) {
                    0 -> {
                        assertEquals("POST", request.method)
                        assertTrue(request.url.endsWith("/feedback/attachments/"))
                        assertFalse(request.headers.containsKey("X-Workspace"))
                        val body =
                            json
                                .parseToJsonElement(
                                    request.body.readAllBytes()!!.toString(Charsets.UTF_8),
                                ).jsonObject
                        assertEquals(
                            "image/png",
                            body["contentType"]!!.jsonPrimitive.content,
                        )
                        HttpResponse(
                            statusCode = 201,
                            body =
                            """
                                    {"id":55,"uploadUrl":"https://uploads.example/put"}
                            """.trimIndent().toByteArray(),
                        )
                    }

                    else -> {
                        assertEquals("PUT", request.method)
                        assertEquals("https://uploads.example/put", request.url)
                        assertEquals("image/png", request.headers["Content-Type"])
                        assertFalse(request.headers.containsKey("X-Workspace"))
                        assertFalse(request.headers.containsKey("X-Container"))
                        assertFalse(request.followRedirects)
                        val streaming = request.body as HttpRequestBody.Streaming
                        assertEquals(4L, streaming.contentLength)
                        assertTrue(request.body.readAllBytes()!!.contentEquals(bytes))
                        HttpResponse(statusCode = 200, body = ByteArray(0))
                    }
                }
            }

        val client =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = engine,
                config = QualtiveConfig(),
            )

        val reference =
            client.uploadAttachment(
                bytes = bytes,
                contentType = AttachmentContentType.ImagePng,
            )
        assertEquals(55L, reference.id)
        assertEquals(2, engine.requests.size)
    }

    @Test
    fun uploadAttachmentSendsWorkspaceHeaderOnCreateOnly() = runTest {
        var call = 0
        val engine =
            FakeHttpEngine { request ->
                when (call++) {
                    0 -> {
                        assertEquals("POST", request.method)
                        assertEquals("my-department", request.headers["X-Workspace"])
                        HttpResponse(
                            statusCode = 201,
                            body = """{"id":9,"uploadUrl":"https://uploads.example/put"}""".toByteArray(),
                        )
                    }

                    else -> {
                        assertEquals("PUT", request.method)
                        assertFalse(request.headers.containsKey("X-Workspace"))
                        HttpResponse(statusCode = 200, body = ByteArray(0))
                    }
                }
            }

        val client =
            QualtiveImpl(
                containerId = "ci-test",
                workspaceId = "my-department",
                httpEngine = engine,
                config = QualtiveConfig(),
            )

        val reference =
            client.uploadAttachment(
                bytes = byteArrayOf(1),
                contentType = AttachmentContentType.ImagePng,
            )
        assertEquals(9L, reference.id)
    }

    @Test
    fun uploadAttachmentCreateAndPutFailures() = runTest {
        val createFail =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = FakeHttpEngine { HttpResponse(503, ByteArray(0)) },
                config = QualtiveConfig(),
            )
        try {
            createFail.uploadAttachment(ByteArray(0), AttachmentContentType.ImagePng)
            fail("expected RemoteMaintenance")
        } catch (_: QualtiveException.RemoteMaintenance) {
            // expected
        }

        var call = 0
        val putFail =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine =
                FakeHttpEngine {
                    when (call++) {
                        0 ->
                            HttpResponse(
                                201,
                                """{"id":1,"uploadUrl":"https://uploads.example/put"}"""
                                    .toByteArray(),
                            )

                        else -> HttpResponse(500, ByteArray(0))
                    }
                },
                config = QualtiveConfig(),
            )
        try {
            putFail.uploadAttachment(byteArrayOf(1), AttachmentContentType.ImageJpeg)
            fail("expected Unexpected")
        } catch (_: QualtiveException.Unexpected) {
            // expected
        }
    }
}

private class FixedDeviceAttributes(
    private val attributes: Map<String, String> =
        mapOf("Platform" to "Android", "OS" to "Android"),
) : DeviceAttributesCollector {
    override fun collect(locale: Locale): Map<String, String> = attributes
}
