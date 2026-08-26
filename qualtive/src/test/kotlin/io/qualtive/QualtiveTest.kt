package io.qualtive

import org.junit.Assert.assertEquals
import org.junit.Test

class QualtiveTest {
    @Test
    fun libraryName() {
        assertEquals("Qualtive", Qualtive::class.simpleName)
    }
}
