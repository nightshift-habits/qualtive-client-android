package io.qualtive.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidDeviceAttributesCollectorSmokeTest {
    @Test
    fun collectReturnsExpectedAndroidKeys() {
        val context = RuntimeEnvironment.getApplication()
        val attributes =
            AndroidDeviceAttributesCollector(context).collect(Locale.US)

        assertEquals("Android", attributes["Platform"])
        assertEquals("Android", attributes["OS"])
        assertTrue(attributes["OS Version"].orEmpty().isNotBlank())
        assertTrue(attributes["Device Model"].orEmpty().isNotBlank())
        assertTrue(attributes["Device Type"] == "Phone" || attributes["Device Type"] == "Tablet")
        assertEquals(context.packageName, attributes["App ID"])
        assertEquals("en", attributes["Language"])
        assertEquals("US", attributes["Region"])
    }
}
