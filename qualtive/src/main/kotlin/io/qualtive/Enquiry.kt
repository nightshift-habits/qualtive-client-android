package io.qualtive

/**
 * An enquiry (feedback form) definition from Qualtive.
 */
public data class Enquiry public constructor(
    public val id: Long,
    public val slug: String,
    public val name: String,
    public val pages: List<Page>,
    public val submittedPages: List<SubmittedPage>,
    public val theme: Theme,
    public val container: Container,
    public val isUserContactDetailsRequired: Boolean,
) {

    /**
     * Creates a flat list of empty [Entry.Content] ready to be filled by the user.
     *
     * Pages are flattened. Static page content that is not part of an entry (body, image,
     * contact details) is omitted.
     */
    public fun entryContentTemplate(): List<Entry.Content> = pages.flatMap { page ->
        page.content.mapNotNull { content ->
            when (content) {
                is Page.Content.Title ->
                    Entry.Content.Title(text = content.text, definition = content)

                is Page.Content.Score ->
                    Entry.Content.Score(value = null, definition = content)

                is Page.Content.Text ->
                    Entry.Content.Text(value = null, definition = content)

                is Page.Content.Select ->
                    Entry.Content.Select(value = null, definition = content)

                is Page.Content.Multiselect ->
                    Entry.Content.Multiselect(values = emptyList(), definition = content)

                is Page.Content.Attachments ->
                    Entry.Content.Attachments(attachments = emptyList(), definition = content)

                is Page.Content.Body,
                is Page.Content.Image,
                is Page.Content.ContactDetails,
                -> null
            }
        }
    }
}
