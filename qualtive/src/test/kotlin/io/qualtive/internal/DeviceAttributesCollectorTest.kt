package io.qualtive.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class DeviceAttributesCollectorTest {
    @Test
    fun standardAttributesIncludeDeviceAppAndLocale() {
        val attributes =
            standardDeviceAttributes(
                osVersion = "14",
                deviceModel = "Pixel 8",
                deviceType = "Phone",
                appId = "io.qualtive.demo",
                appVersion = "1.2.3",
                appBuild = "45",
                locale = Locale.forLanguageTag("sv-SE"),
            )

        assertEquals("Android", attributes["Platform"])
        assertEquals("Android", attributes["OS"])
        assertEquals("14", attributes["OS Version"])
        assertEquals("Pixel 8", attributes["Device Model"])
        assertEquals("Phone", attributes["Device Type"])
        assertEquals("io.qualtive.demo", attributes["App ID"])
        assertEquals("1.2.3", attributes["App Version"])
        assertEquals("45", attributes["App Build"])
        assertEquals("sv", attributes["Language"])
        assertEquals("SE", attributes["Region"])
    }

    @Test
    fun standardAttributesOmitBlankOptionalFields() {
        val attributes =
            standardDeviceAttributes(
                osVersion = "14",
                deviceModel = "Pixel 8",
                deviceType = "Tablet",
                appId = null,
                appVersion = " ",
                appBuild = null,
                locale = Locale.forLanguageTag("en"),
            )

        assertEquals("Tablet", attributes["Device Type"])
        assertEquals("en", attributes["Language"])
        assertTrue("App ID" !in attributes)
        assertTrue("App Version" !in attributes)
        assertTrue("App Build" !in attributes)
        assertTrue("Region" !in attributes)
    }

    @Test
    fun deviceTypeUsesSmallestWidthBreakpoint() {
        assertEquals("Phone", deviceTypeForSmallestWidthDp(599))
        assertEquals("Tablet", deviceTypeForSmallestWidthDp(600))
    }

    @Test
    fun emptyCollectorReturnsNothing() {
        assertTrue(EmptyDeviceAttributesCollector.collect(Locale.US).isEmpty())
    }
}
