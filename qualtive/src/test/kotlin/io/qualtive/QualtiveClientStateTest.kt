package io.qualtive

import io.qualtive.internal.AttributeValue
import io.qualtive.internal.QualtiveImpl
import io.qualtive.internal.network.FakeHttpEngine
import io.qualtive.internal.network.HttpResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QualtiveClientStateTest {
    @Test
    fun storesRuntimeIdentityAndAttributes() {
        val client =
            QualtiveImpl(
                containerId = "ci-test",
                httpEngine = FakeHttpEngine { HttpResponse(404, ByteArray(0)) },
                config = QualtiveConfig(),
            )

        assertEquals(UserTrackingConsent.Denied, client.userTrackingConsent)
        assertEquals(MetadataCollection.NonPersonal, client.config.metadataCollection)
        assertNull(client.applicationContext)

        client.userTrackingConsent = UserTrackingConsent.Granted
        client.identify(userId = "user-1", name = "Ada", email = "ada@example.com")
        client.setAttribute("plan", "pro")
        client.setAttribute("vip", true)
        client.setAttribute("seats", 3)
        client.setAttribute("ratio", 0.5)

        assertEquals(UserTrackingConsent.Granted, client.userTrackingConsent)
        val identity = client.snapshotIdentity()
        assertEquals("user-1", identity.userId)
        assertEquals("Ada", identity.name)
        assertEquals("ada@example.com", identity.email)

        val attributes = client.snapshotAttributes()
        assertEquals(AttributeValue.Text("pro"), attributes["plan"])
        assertEquals(AttributeValue.Flag(true), attributes["vip"])
        assertEquals(AttributeValue.Number(3.0), attributes["seats"])
        assertEquals(AttributeValue.Number(0.5), attributes["ratio"])

        client.removeAttribute("plan")
        assertTrue("plan" !in client.snapshotAttributes())

        try {
            client.setAttribute(" ", "nope")
            throw AssertionError("expected blank attribute name to fail")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message!!.contains("blank"))
        }

        client.identify()
        val cleared = client.snapshotIdentity()
        assertNull(cleared.userId)
        assertNull(cleared.name)
        assertNull(cleared.email)
    }
}
