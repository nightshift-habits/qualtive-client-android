package io.qualtive.internal.enquiry

import io.qualtive.internal.Logger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal object PageContentListSerializer : SkipUnknownListSerializer<PageContentDto>(
    elementSerializer = PageContentDto.serializer(),
    label = "page content",
)

internal object SubmittedContentListSerializer : SkipUnknownListSerializer<SubmittedContentDto>(
    elementSerializer = SubmittedContentDto.serializer(),
    label = "submitted content",
)

internal object ConditionListSerializer : SkipUnknownListSerializer<ConditionDto>(
    elementSerializer = ConditionDto.serializer(),
    label = "condition",
)

internal open class SkipUnknownListSerializer<T>(
    private val elementSerializer: KSerializer<T>,
    private val label: String,
) : KSerializer<List<T>> {
    private val listSerializer = ListSerializer(elementSerializer)

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): List<T> {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: error("SkipUnknownListSerializer only supports JSON")
        val array =
            when (val element = jsonDecoder.decodeJsonElement()) {
                is JsonArray -> element
                else -> {
                    Logger.hintNewVersion("$label list")
                    return emptyList()
                }
            }

        return array.mapNotNull { item -> decodeItem(jsonDecoder, item) }
    }

    override fun serialize(
        encoder: Encoder,
        value: List<T>,
    ) {
        val jsonEncoder =
            encoder as? JsonEncoder
                ?: error("SkipUnknownListSerializer only supports JSON")
        jsonEncoder.encodeJsonElement(
            JsonArray(
                value.map { item ->
                    jsonEncoder.json.encodeToJsonElement(elementSerializer, item)
                },
            ),
        )
    }

    private fun decodeItem(
        decoder: JsonDecoder,
        element: JsonElement,
    ): T? {
        val type =
            (element as? JsonObject)
                ?.get("type")
                ?.jsonPrimitive
                ?.contentOrNull
        return try {
            decoder.json.decodeFromJsonElement(elementSerializer, element)
        } catch (error: Exception) {
            Logger.hintNewVersion(
                if (type != null) {
                    "$label type=$type (${error.message})"
                } else {
                    "$label (${error.message})"
                },
            )
            null
        }
    }
}
