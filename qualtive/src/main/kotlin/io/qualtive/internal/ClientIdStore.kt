package io.qualtive.internal

import android.content.Context
import java.util.UUID

internal interface ClientIdStore {
    fun getOrCreate(): String

    fun clear()
}

internal interface StringKeyValueStore {
    fun getString(key: String): String?

    fun putString(
        key: String,
        value: String,
    )

    fun remove(key: String)
}

internal class PersistingClientIdStore(
    private val store: StringKeyValueStore,
    private val storageKey: String = STORAGE_KEY,
    private val generateId: () -> String = { UUID.randomUUID().toString() },
) : ClientIdStore {
    override fun getOrCreate(): String {
        val existing = store.getString(storageKey)
        if (!existing.isNullOrEmpty()) {
            return existing
        }
        val id = generateId()
        store.putString(storageKey, id)
        return id
    }

    override fun clear() {
        store.remove(storageKey)
    }

    internal companion object {
        const val STORAGE_KEY: String = "_qualtiveCID"
    }
}

internal class SharedPreferencesStringKeyValueStore(
    context: Context,
    prefsName: String = PREFS_NAME,
) : StringKeyValueStore {
    private val preferences =
        context.applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(
        key: String,
        value: String,
    ) {
        preferences.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    internal companion object {
        const val PREFS_NAME: String = "io.qualtive.client"
    }
}

internal fun SharedPreferencesClientIdStore(context: Context): ClientIdStore =
    PersistingClientIdStore(
        store = SharedPreferencesStringKeyValueStore(context),
    )

internal class InMemoryClientIdStore : ClientIdStore {
    private var value: String? = null

    override fun getOrCreate(): String {
        val existing = value
        if (!existing.isNullOrEmpty()) {
            return existing
        }
        val id = UUID.randomUUID().toString()
        value = id
        return id
    }

    override fun clear() {
        value = null
    }
}

internal object NoOpClientIdStore : ClientIdStore {
    override fun getOrCreate(): String = error("client id store is unavailable")

    override fun clear() = Unit
}
