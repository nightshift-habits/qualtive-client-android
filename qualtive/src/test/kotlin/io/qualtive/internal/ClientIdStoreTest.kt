package io.qualtive.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClientIdStoreTest {
    @Test
    fun persistingStoreCreatesReusesAndClears() {
        val memory = mutableMapOf<String, String>()
        val store =
            PersistingClientIdStore(
                store =
                    object : StringKeyValueStore {
                        override fun getString(key: String): String? = memory[key]

                        override fun putString(
                            key: String,
                            value: String,
                        ) {
                            memory[key] = value
                        }

                        override fun remove(key: String) {
                            memory.remove(key)
                        }
                    },
                generateId = { "generated-id" },
            )

        assertEquals("generated-id", store.getOrCreate())
        assertEquals("generated-id", store.getOrCreate())
        assertEquals("generated-id", memory[PersistingClientIdStore.STORAGE_KEY])

        store.clear()
        assertTrue(memory.isEmpty())

        assertEquals("generated-id", store.getOrCreate())
    }

    @Test
    fun persistingStoreReusesExistingValue() {
        val memory = mutableMapOf(PersistingClientIdStore.STORAGE_KEY to "existing-id")
        val store =
            PersistingClientIdStore(
                store =
                    object : StringKeyValueStore {
                        override fun getString(key: String): String? = memory[key]

                        override fun putString(
                            key: String,
                            value: String,
                        ) {
                            memory[key] = value
                        }

                        override fun remove(key: String) {
                            memory.remove(key)
                        }
                    },
                generateId = { "should-not-be-used" },
            )

        assertEquals("existing-id", store.getOrCreate())
    }

    @Test
    fun inMemoryStoreCreatesReusesAndClears() {
        val store = InMemoryClientIdStore()
        val first = store.getOrCreate()
        val second = store.getOrCreate()
        assertTrue(first.isNotBlank())
        assertEquals(first, second)

        store.clear()
        val third = store.getOrCreate()
        assertNotEquals(first, third)
    }
}
