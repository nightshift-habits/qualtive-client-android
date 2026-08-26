package io.qualtive.internal.entry

import io.qualtive.Entry
import io.qualtive.Page
import io.qualtive.ScoreType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryPostEncoderTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun encodesContentTypesAndOmitsAttributeTargetedText() {
        val content =
            listOf(
                Entry.Content.Title(text = "Shown title"),
                Entry.Content.Score(
                    value = 80,
                    definition =
                        Page.Content.Score(
                            scoreType = ScoreType.Stars5,
                            leadingText = "Bad",
                            trailingText = "Good",
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
                            storageTarget = Page.Content.Text.StorageTarget.Attribute("Age"),
                        ),
                ),
                Entry.Content.Select(value = "A"),
                Entry.Content.Multiselect(values = listOf("X", "Y")),
                Entry.Content.Attachments(
                    attachments = listOf(Entry.AttachmentReference(id = 99)),
                ),
            )

        val encoded = EntryPostEncoder.encodeContent(content)
        assertEquals(6, encoded.size)

        val types = encoded.map { it.jsonObject["type"]!!.jsonPrimitive.content }
        assertEquals(
            listOf("title", "score", "text", "select", "multiselect", "attachments"),
            types,
        )

        val score = encoded[1].jsonObject
        assertEquals("80", score["value"]!!.jsonPrimitive.content)
        assertEquals("stars5", score["scoreType"]!!.jsonPrimitive.content)
        assertEquals("Bad", score["leadingText"]!!.jsonPrimitive.content)
        assertEquals("Good", score["trailingText"]!!.jsonPrimitive.content)

        val attachments = encoded[5].jsonObject["values"]!!.jsonArray
        assertEquals(99L, attachments[0].jsonObject["id"]!!.jsonPrimitive.long)
    }

    @Test
    fun attributesFromContentUsesStorageTarget() {
        val content =
            listOf(
                Entry.Content.Text(
                    value = "32",
                    definition =
                        Page.Content.Text(
                            placeholder = null,
                            storageTarget = Page.Content.Text.StorageTarget.Attribute("Age"),
                        ),
                ),
                Entry.Content.Text(
                    value = "   ",
                    definition =
                        Page.Content.Text(
                            placeholder = null,
                            storageTarget = Page.Content.Text.StorageTarget.Attribute("Skip"),
                        ),
                ),
                Entry.Content.Text(
                    value = "kept as text",
                    definition =
                        Page.Content.Text(
                            placeholder = null,
                            storageTarget = Page.Content.Text.StorageTarget.Text,
                        ),
                ),
            )

        assertEquals(mapOf("Age" to "32"), EntryPostEncoder.attributesFromContent(content))
    }

    @Test
    fun stringifiesAndMergesAttributesWithLaterWins() {
        val custom =
            EntryPostEncoder.stringifyCustomAttributes(
                mapOf(
                    "vip" to true,
                    "seats" to 3,
                    "ratio" to 0.5,
                    "plan" to "pro",
                    "Platform" to "Custom",
                ),
            )
        assertEquals("true", custom["vip"])
        assertEquals("3", custom["seats"])
        assertEquals("0.5", custom["ratio"])
        assertEquals("pro", custom["plan"])

        val merged =
            EntryPostEncoder.mergeAttributes(
                device = mapOf("Platform" to "Android", "OS" to "Android"),
                custom = custom,
                fromContent = mapOf("Age" to "32", "Platform" to "FromContent"),
            )
        assertEquals("FromContent", merged["Platform"])
        assertEquals("Android", merged["OS"])
        assertEquals("32", merged["Age"])
        assertEquals("pro", merged["plan"])
    }

    @Test
    fun rejectsBlankOrUnsupportedAttributeValues() {
        try {
            EntryPostEncoder.stringifyCustomAttributes(mapOf(" " to "x"))
            throw AssertionError("expected blank name to fail")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message!!.contains("blank"))
        }

        try {
            EntryPostEncoder.stringifyCustomAttributes(mapOf("bad" to listOf(1)))
            throw AssertionError("expected unsupported type to fail")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message!!.contains("String, Boolean, or Number"))
        }
    }

    @Test
    fun encodeRequestOmitsNullClientId() {
        val body =
            EntryPostEncoder.encodeRequest(
                questionId = "android",
                content = listOf(Entry.Content.Text(value = "Hi")),
                userId = "u1",
                name = "Ada",
                email = "ada@example.com",
                clientId = null,
                timeZoneIdentifier = "Europe/Stockholm",
                attributes = mapOf("plan" to "pro"),
            )
        val parsed = json.parseToJsonElement(body.toString(Charsets.UTF_8)).jsonObject
        val user = parsed["user"]!!.jsonObject
        assertEquals("u1", user["id"]!!.jsonPrimitive.content)
        assertNull(user["clientId"])
        assertEquals(
            "android",
            parsed["attributeHints"]!!.jsonObject["clientLibrary"]!!.jsonPrimitive.content,
        )
        assertEquals(JsonPrimitive("Hi"), parsed["content"]!!.jsonArray[0].jsonObject["value"])
        assertTrue(parsed["content"]!!.jsonArray[0] is JsonObject)
    }
}
