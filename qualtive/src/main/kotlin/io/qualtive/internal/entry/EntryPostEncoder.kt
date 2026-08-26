package io.qualtive.internal.entry

import io.qualtive.Entry
import io.qualtive.Page
import io.qualtive.ScoreType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal object EntryPostEncoder {
    private val json =
        Json {
            encodeDefaults = false
            explicitNulls = false
        }

    fun encodeRequest(
        questionId: String,
        content: List<Entry.Content>,
        userId: String?,
        name: String?,
        email: String?,
        clientId: String?,
        timeZoneIdentifier: String,
        attributes: Map<String, String>,
    ): ByteArray {
        val body =
            PostEntryRequest(
                questionId = questionId,
                content = encodeContent(content),
                user =
                    PostEntryUser(
                        id = userId,
                        name = name,
                        email = email,
                        clientId = clientId,
                        timeZoneIdentifier = timeZoneIdentifier,
                    ),
                attributes = attributes,
                attributeHints = PostEntryAttributeHints(clientLibrary = "android"),
            )
        return json.encodeToString(body).toByteArray(Charsets.UTF_8)
    }

    fun encodeContent(content: List<Entry.Content>): List<JsonElement> =
        content.mapNotNull { section ->
            when (section) {
                is Entry.Content.Title ->
                    buildJsonObject {
                        put("type", "title")
                        put("text", section.text)
                    }
                is Entry.Content.Score ->
                    buildJsonObject {
                        put("type", "score")
                        section.value?.let { put("value", it) }
                        val scoreType = section.definition?.scoreType ?: ScoreType.Smilies5
                        put("scoreType", scoreType.toApi())
                        section.definition?.leadingText?.let { put("leadingText", it) }
                        section.definition?.trailingText?.let { put("trailingText", it) }
                    }
                is Entry.Content.Text -> {
                    when (section.definition?.storageTarget) {
                        is Page.Content.Text.StorageTarget.Attribute -> null
                        else ->
                            buildJsonObject {
                                put("type", "text")
                                if (section.value != null) {
                                    put("value", section.value)
                                } else {
                                    put("value", JsonNull)
                                }
                            }
                    }
                }
                is Entry.Content.Select ->
                    buildJsonObject {
                        put("type", "select")
                        if (section.value != null) {
                            put("value", section.value)
                        } else {
                            put("value", JsonNull)
                        }
                    }
                is Entry.Content.Multiselect ->
                    buildJsonObject {
                        put("type", "multiselect")
                        put(
                            "values",
                            JsonArray(section.values.map { JsonPrimitive(it) }),
                        )
                    }
                is Entry.Content.Attachments ->
                    buildJsonObject {
                        put("type", "attachments")
                        put(
                            "values",
                            JsonArray(
                                section.attachments.map { attachment ->
                                    JsonObject(mapOf("id" to JsonPrimitive(attachment.id)))
                                },
                            ),
                        )
                    }
            }
        }

    fun attributesFromContent(content: List<Entry.Content>): Map<String, String> {
        val result = linkedMapOf<String, String>()
        for (section in content) {
            if (section !is Entry.Content.Text) continue
            val target = section.definition?.storageTarget
            if (target !is Page.Content.Text.StorageTarget.Attribute) continue
            val value = section.value
            if (value.isNullOrBlank()) continue
            result[target.attribute] = value
        }
        return result
    }

    fun stringifyCustomAttributes(attributes: Map<String, Any>): Map<String, String> {
        val result = linkedMapOf<String, String>()
        for ((key, value) in attributes) {
            require(key.isNotBlank()) { "attribute name must not be blank" }
            result[key] =
                when (value) {
                    is String -> value
                    is Boolean -> value.toString()
                    is Int -> value.toString()
                    is Long -> value.toString()
                    is Float -> stringifyNumber(value.toDouble())
                    is Double -> stringifyNumber(value)
                    is Number -> stringifyNumber(value.toDouble())
                    else ->
                        throw IllegalArgumentException(
                            "attribute \"$key\" must be String, Boolean, or Number",
                        )
                }
        }
        return result
    }

    fun mergeAttributes(
        device: Map<String, String>,
        custom: Map<String, String>,
        fromContent: Map<String, String>,
    ): Map<String, String> {
        val result = linkedMapOf<String, String>()
        result.putAll(device)
        result.putAll(custom)
        result.putAll(fromContent)
        return result
    }

    private fun stringifyNumber(value: Double): String =
        if (value % 1.0 == 0.0 && value in Long.MIN_VALUE.toDouble()..Long.MAX_VALUE.toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
}

@Serializable
internal data class PostEntryRequest(
    val questionId: String,
    val content: List<JsonElement>,
    val user: PostEntryUser,
    val attributes: Map<String, String>,
    val attributeHints: PostEntryAttributeHints,
)

@Serializable
internal data class PostEntryUser(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val clientId: String? = null,
    val timeZoneIdentifier: String,
)

@Serializable
internal data class PostEntryAttributeHints(
    val clientLibrary: String,
)

@Serializable
internal data class PostEntryResponse(
    val id: Long,
)

@Serializable
internal data class CreateAttachmentRequest(
    val contentType: String,
)

@Serializable
internal data class CreateAttachmentResponse(
    val id: Long,
    @SerialName("uploadUrl")
    val uploadUrl: String,
)
