package io.qualtive.internal.network

import io.qualtive.QualtiveConfig
import io.qualtive.QualtiveException
import io.qualtive.internal.QualtiveImpl
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class QualtiveFetchTest {
    @Test
    fun fetchEnquirySuccess() =
        runTest {
            val engine =
                FakeHttpEngine { request ->
                    assertEquals("GET", request.method)
                    assertTrue(request.url.endsWith("/feedback/enquiries/android/"))
                    assertEquals("ci-test", request.headers["X-Container"])
                    assertEquals("en-US", request.headers["Accept-Language"])
                    HttpResponse(
                        statusCode = 200,
                        body =
                            """
                            {
                              "id": 5326232893784064,
                              "slug": "android",
                              "name": "Android?",
                              "pages": [
                                {
                                  "content": [
                                    {
                                      "type": "score",
                                      "scoreType": "smilies5",
                                      "leadingText": null,
                                      "trailingText": null
                                    }
                                  ]
                                }
                              ],
                              "submittedPages": [
                                {
                                  "conditions": [],
                                  "content": [{ "type": "confirmationText", "text": "Thanks" }]
                                }
                              ],
                              "theme": {
                                "cornerStyle": "rounded",
                                "background": { "type": "predefined", "value": "plain" },
                                "font": { "type": "predefined", "value": "default" },
                                "isBackgroundAttachmentVisibleInResponses": true,
                                "isBackgroundColorVisibleInResponses": true
                              },
                              "container": {
                                "id": "ci-test",
                                "isWhiteLabel": false,
                                "logo": null,
                                "customLogos": [],
                                "version": "qualtive",
                                "visibilityMode": "private"
                              },
                              "isUserContactDetailsRequired": false
                            }
                            """.trimIndent().toByteArray(),
                    )
                }

            val client =
                QualtiveImpl(
                    containerId = "ci-test",
                    httpEngine = engine,
                    config = QualtiveConfig(locale = Locale.US),
                )

            val enquiry = client.fetchEnquiry("android")
            assertEquals(5326232893784064L, enquiry.id)
            assertEquals("android", enquiry.slug)
            assertEquals(1, engine.requests.size)
        }

    @Test
    fun fetchEnquiryIncludesPreviewToken() =
        runTest {
            val engine =
                FakeHttpEngine {
                    HttpResponse(statusCode = 404, body = ByteArray(0))
                }
            val client =
                QualtiveImpl(
                    containerId = "ci-test",
                    httpEngine = engine,
                    config = QualtiveConfig(locale = Locale.US),
                )

            try {
                client.fetchEnquiry("android", previewToken = "token value")
                fail("expected NotFound")
            } catch (_: QualtiveException.NotFound) {
                // expected
            }

            assertTrue(
                engine.requests.single().url.contains("previewToken=token%20value"),
            )
        }

    @Test
    fun fetchEnquiryNotFound() =
        runTest {
            val client =
                QualtiveImpl(
                    containerId = "ci-test",
                    httpEngine = FakeHttpEngine { HttpResponse(404, ByteArray(0)) },
                    config = QualtiveConfig(),
                )
            try {
                client.fetchEnquiry("missing")
                fail("expected NotFound")
            } catch (_: QualtiveException.NotFound) {
                // expected
            }
        }

    @Test
    fun fetchEnquiryRemoteMaintenance() =
        runTest {
            val client =
                QualtiveImpl(
                    containerId = "ci-test",
                    httpEngine = FakeHttpEngine { HttpResponse(503, ByteArray(0)) },
                    config = QualtiveConfig(),
                )
            try {
                client.fetchEnquiry("android")
                fail("expected RemoteMaintenance")
            } catch (_: QualtiveException.RemoteMaintenance) {
                // expected
            }
        }

    @Test
    fun fetchEnquiryConnectionFailure() =
        runTest {
            val client =
                QualtiveImpl(
                    containerId = "ci-test",
                    httpEngine =
                        FakeHttpEngine {
                            throw IOException("boom")
                        },
                    config = QualtiveConfig(),
                )
            try {
                client.fetchEnquiry("android")
                fail("expected Connection")
            } catch (error: QualtiveException.Connection) {
                assertTrue(error.cause is IOException)
            }
        }
}
