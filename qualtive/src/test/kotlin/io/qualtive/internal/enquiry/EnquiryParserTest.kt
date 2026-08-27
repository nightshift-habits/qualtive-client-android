package io.qualtive.internal.enquiry

import io.qualtive.Container
import io.qualtive.Enquiry
import io.qualtive.Page
import io.qualtive.ScoreType
import io.qualtive.SubmittedPage
import io.qualtive.Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnquiryParserTest {
    @Test
    fun parsesFullEnquiryPayload() {
        val enquiry =
            EnquiryParser.parse(
                """
                {
                  "id": 6290486614556672,
                  "slug": "web",
                  "name": "Web?",
                  "isUserContactDetailsRequired": true,
                  "pages": [
                    {
                      "content": [
                        {
                          "type": "score",
                          "scoreType": "stars5",
                          "leadingText": "Bad",
                          "trailingText": "Good"
                        },
                        {
                          "type": "title",
                          "text": "Hello"
                        },
                        {
                          "type": "body",
                          "text": "Body text"
                        },
                        {
                          "type": "image",
                          "attachment": { "url": "https://example.com/a.png" }
                        },
                        {
                          "type": "text",
                          "placeholder": "Write…",
                          "storageTarget": { "type": "attribute", "attribute": "Age" }
                        },
                        {
                          "type": "select",
                          "options": ["A", "B"],
                          "allowsCustomInput": true
                        },
                        {
                          "type": "multiselect",
                          "options": ["X", "Y"]
                        },
                        { "type": "attachments" },
                        {
                          "type": "contactDetails",
                          "title": "Email",
                          "placeholder": "you@example.com"
                        },
                        { "type": "futureThing", "foo": 1 }
                      ]
                    }
                  ],
                  "submittedPages": [
                    {
                      "conditions": [
                        {
                          "type": "score",
                          "ranges": [{ "lower": 0, "upper": 50 }]
                        },
                        { "type": "futureCondition" }
                      ],
                      "content": [
                        { "type": "confirmationText", "text": "Thanks!" },
                        { "type": "name" },
                        { "type": "userInput" },
                        { "type": "userInputScore" },
                        { "type": "link", "text": "Site", "url": "https://example.com" },
                        {
                          "type": "reviewLinks",
                          "links": [
                            {
                              "title": "Google",
                              "url": "https://google.com",
                              "logo": null,
                              "icon": null
                            }
                          ]
                        },
                        { "type": "futureSubmitted" }
                      ]
                    }
                  ],
                  "theme": {
                    "cornerStyle": "rounded",
                    "background": { "type": "predefined", "value": "plain" },
                    "font": { "type": "predefined", "value": "default" },
                    "isBackgroundAttachmentVisibleInResponses": true,
                    "isBackgroundColorVisibleInResponses": false
                  },
                  "container": {
                    "id": "ci-test",
                    "isWhiteLabel": false,
                    "logo": null,
                    "customLogos": [],
                    "version": "qualtive",
                    "visibilityMode": "private"
                  }
                }
                """.trimIndent(),
            )

        assertEquals(6290486614556672L, enquiry.id)
        assertEquals("web", enquiry.slug)
        assertEquals("Web?", enquiry.name)
        assertTrue(enquiry.isUserContactDetailsRequired)
        assertEquals(1, enquiry.pages.size)
        assertEquals(9, enquiry.pages[0].content.size)

        val score = enquiry.pages[0].content[0] as Page.Content.Score
        assertEquals(ScoreType.Stars5, score.scoreType)
        assertEquals("Bad", score.leadingText)
        assertEquals("Good", score.trailingText)

        val text = enquiry.pages[0].content[4] as Page.Content.Text
        assertEquals(
            Page.Content.Text.StorageTarget.Attribute("Age"),
            text.storageTarget,
        )

        val select = enquiry.pages[0].content[5] as Page.Content.Select
        assertTrue(select.allowsCustomInput)

        assertEquals(1, enquiry.submittedPages.size)
        assertEquals(1, enquiry.submittedPages[0].conditions.size)
        val condition =
            enquiry.submittedPages[0].conditions[0] as SubmittedPage.Condition.Score
        assertEquals(0, condition.ranges[0].lower)
        assertEquals(50, condition.ranges[0].upper)
        assertEquals(6, enquiry.submittedPages[0].content.size)

        assertEquals(Theme.CornerStyle.Rounded, enquiry.theme.cornerStyle)
        assertEquals(false, enquiry.theme.isBackgroundColorVisibleInResponses)
        assertEquals(Container.VisibilityMode.Private, enquiry.container.visibilityMode)
    }

    @Test
    fun skipsUnknownScoreType() {
        val enquiry =
            EnquiryParser.parse(
                """
                {
                  "id": 1,
                  "slug": "x",
                  "name": "X",
                  "pages": [
                    {
                      "content": [
                        {
                          "type": "score",
                          "scoreType": "emoji99",
                          "leadingText": null,
                          "trailingText": null
                        },
                        { "type": "title", "text": "Keep me" }
                      ]
                    }
                  ],
                  "submittedPages": [],
                  "theme": {
                    "cornerStyle": "square",
                    "background": { "type": "predefined", "value": "sponda" },
                    "font": { "type": "custom", "url": "https://fonts.example/a.woff2" },
                    "isBackgroundAttachmentVisibleInResponses": true,
                    "isBackgroundColorVisibleInResponses": true
                  },
                  "container": {
                    "id": "c",
                    "isWhiteLabel": true,
                    "logo": { "urlVector": "https://a.svg", "urlVectorDark": null },
                    "customLogos": [
                      {
                        "size": "wide",
                        "intendedBackground": "light",
                        "primaryColor": "#fff",
                        "urlVector": "https://logo.svg"
                      }
                    ],
                    "version": "qualtive",
                    "visibilityMode": "public"
                  }
                }
                """.trimIndent(),
            )

        assertEquals(1, enquiry.pages[0].content.size)
        assertTrue(enquiry.pages[0].content[0] is Page.Content.Title)
        assertEquals(Theme.CornerStyle.Square, enquiry.theme.cornerStyle)
        assertTrue(enquiry.theme.font is Theme.Font.Custom)
        assertTrue(enquiry.container.isWhiteLabel)
        assertEquals(Container.VisibilityMode.Public, enquiry.container.visibilityMode)
        assertEquals(1, enquiry.container.customLogos.size)
    }

    @Test
    fun entryContentTemplateFlattensInputTypes() {
        val enquiry =
            EnquiryParser.parse(
                """
                {
                  "id": 1,
                  "slug": "x",
                  "name": "X",
                  "pages": [
                    {
                      "content": [
                        { "type": "title", "text": "T" },
                        { "type": "body", "text": "B" },
                        {
                          "type": "score",
                          "scoreType": "thumbs",
                          "leadingText": null,
                          "trailingText": null
                        },
                        {
                          "type": "text",
                          "placeholder": null,
                          "storageTarget": { "type": "text" }
                        }
                      ]
                    },
                    {
                      "content": [
                        {
                          "type": "select",
                          "options": ["A"],
                          "allowsCustomInput": false
                        },
                        { "type": "attachments" }
                      ]
                    }
                  ],
                  "submittedPages": [],
                  "theme": {
                    "cornerStyle": "rounded",
                    "background": { "type": "predefined", "value": "plain" },
                    "font": { "type": "predefined", "value": "default" },
                    "isBackgroundAttachmentVisibleInResponses": true,
                    "isBackgroundColorVisibleInResponses": true
                  },
                  "container": {
                    "id": "c",
                    "isWhiteLabel": false,
                    "logo": null,
                    "customLogos": [],
                    "version": "qualtive",
                    "visibilityMode": "private"
                  }
                }
                """.trimIndent(),
            )

        val template = enquiry.entryContentTemplate()
        assertEquals(5, template.size)
    }
}
