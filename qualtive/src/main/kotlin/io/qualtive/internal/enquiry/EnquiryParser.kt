package io.qualtive.internal.enquiry

import io.qualtive.Attachment
import io.qualtive.Container
import io.qualtive.Enquiry
import io.qualtive.Page
import io.qualtive.QualtiveException
import io.qualtive.ScoreType
import io.qualtive.SubmittedPage
import io.qualtive.Theme
import io.qualtive.internal.Logger
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal object EnquiryParser {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            classDiscriminator = "type"
        }

    fun parse(body: String): Enquiry {
        val dto =
            try {
                json.decodeFromString(EnquiryDto.serializer(), body)
            } catch (error: SerializationException) {
                throw QualtiveException.Unexpected("Invalid enquiry JSON", error)
            } catch (error: IllegalArgumentException) {
                throw QualtiveException.Unexpected("Invalid enquiry JSON", error)
            }

        return dto.toDomain()
    }
}

private fun EnquiryDto.toDomain(): Enquiry = Enquiry(
    id = id,
    slug = slug,
    name = name,
    pages = pages.map { it.toDomain() },
    submittedPages = submittedPages.map { it.toDomain() },
    theme = theme.toDomain(),
    container = container.toDomain(),
    isUserContactDetailsRequired = isUserContactDetailsRequired,
)

private fun PageDto.toDomain(): Page = Page(
    content = content.mapNotNull { it.toDomain() },
)

private fun PageContentDto.toDomain(): Page.Content? = when (this) {
    is PageContentDto.Title -> Page.Content.Title(text = text)

    is PageContentDto.Body -> Page.Content.Body(text = text)

    is PageContentDto.Image ->
        Page.Content.Image(
            attachment = Attachment(url = attachment.url),
        )

    is PageContentDto.Score -> {
        val parsed =
            ScoreType.fromApi(scoreType)
                ?: run {
                    Logger.hintNewVersion("scoreType=$scoreType")
                    return null
                }
        Page.Content.Score(
            scoreType = parsed,
            leadingText = leadingText,
            trailingText = trailingText,
        )
    }

    is PageContentDto.Text ->
        Page.Content.Text(
            placeholder = placeholder,
            storageTarget = storageTarget.toDomain(),
        )

    is PageContentDto.Select ->
        Page.Content.Select(
            options = options,
            allowsCustomInput = allowsCustomInput,
        )

    is PageContentDto.Multiselect ->
        Page.Content.Multiselect(options = options)

    is PageContentDto.Attachments -> Page.Content.Attachments

    is PageContentDto.ContactDetails ->
        Page.Content.ContactDetails(
            title = title,
            placeholder = placeholder,
        )
}

private fun StorageTargetDto.toDomain(): Page.Content.Text.StorageTarget = when (this) {
    is StorageTargetDto.Text -> Page.Content.Text.StorageTarget.Text

    is StorageTargetDto.Attribute ->
        Page.Content.Text.StorageTarget.Attribute(attribute = attribute)
}

private fun SubmittedPageDto.toDomain(): SubmittedPage = SubmittedPage(
    content = content.map { it.toDomain() },
    conditions = conditions.map { it.toDomain() },
)

private fun SubmittedContentDto.toDomain(): SubmittedPage.Content = when (this) {
    is SubmittedContentDto.Title -> SubmittedPage.Content.Title(text = text)

    is SubmittedContentDto.Body -> SubmittedPage.Content.Body(text = text)

    is SubmittedContentDto.Image ->
        SubmittedPage.Content.Image(
            attachment = Attachment(url = attachment.url),
            linkUrl = linkUrl,
        )

    is SubmittedContentDto.ConfirmationText ->
        SubmittedPage.Content.ConfirmationText(text = text)

    is SubmittedContentDto.Name -> SubmittedPage.Content.Name

    is SubmittedContentDto.UserInput -> SubmittedPage.Content.UserInput

    is SubmittedContentDto.UserInputScore -> SubmittedPage.Content.UserInputScore

    is SubmittedContentDto.Link ->
        SubmittedPage.Content.Link(text = text, url = url)

    is SubmittedContentDto.ReviewLinks ->
        SubmittedPage.Content.ReviewLinks(
            links =
            links.map { link ->
                SubmittedPage.Content.ReviewLinks.Link(
                    title = link.title,
                    url = link.url,
                    logo =
                    link.logo?.let {
                        SubmittedPage.Content.ReviewLinks.Link.Logo(
                            urlVector = it.urlVector,
                            urlVectorDark = it.urlVectorDark,
                        )
                    },
                    icon =
                    link.icon?.let {
                        SubmittedPage.Content.ReviewLinks.Link.Icon(
                            urlRaster = it.urlRaster,
                            urlRasterDark = it.urlRasterDark,
                        )
                    },
                )
            },
        )
}

private fun ConditionDto.toDomain(): SubmittedPage.Condition = when (this) {
    is ConditionDto.Score ->
        SubmittedPage.Condition.Score(
            ranges =
            ranges.map {
                SubmittedPage.Condition.Score.Range(
                    lower = it.lower,
                    upper = it.upper,
                )
            },
        )
}

private fun ThemeDto.toDomain(): Theme = Theme(
    background = background.toDomain(),
    font = font.toDomain(),
    cornerStyle =
    when (cornerStyle) {
        "rounded" -> Theme.CornerStyle.Rounded

        "square" -> Theme.CornerStyle.Square

        else -> {
            Logger.hintNewVersion("cornerStyle=$cornerStyle")
            Theme.CornerStyle.Rounded
        }
    },
    isBackgroundAttachmentVisibleInResponses = isBackgroundAttachmentVisibleInResponses,
    isBackgroundColorVisibleInResponses = isBackgroundColorVisibleInResponses,
)

private fun BackgroundDto.toDomain(): Theme.Background = when (this) {
    is BackgroundDto.Predefined ->
        Theme.Background.Predefined(
            value =
            when (value) {
                "plain" -> Theme.Background.Predefined.Value.Plain

                "sponda" -> Theme.Background.Predefined.Value.Sponda

                else -> {
                    Logger.hintNewVersion("background value=$value")
                    Theme.Background.Predefined.Value.Plain
                }
            },
        )

    is BackgroundDto.Custom ->
        Theme.Background.Custom(
            attachment =
            attachment?.let {
                Theme.Background.Custom.Attachment(
                    id = it.id,
                    contentType = it.contentType,
                    url = it.url,
                )
            },
            color = Theme.Background.Custom.Color(value = color.value),
        )
}

private fun FontDto.toDomain(): Theme.Font = when (this) {
    is FontDto.Predefined -> Theme.Font.Predefined(value = value)
    is FontDto.Custom -> Theme.Font.Custom(url = url)
}

private fun ContainerDto.toDomain(): Container = Container(
    id = id,
    isWhiteLabel = isWhiteLabel,
    logo =
    logo?.let {
        Container.Logo(
            urlVector = it.urlVector,
            urlVectorDark = it.urlVectorDark,
        )
    },
    customLogos = customLogos.mapNotNull { it.toDomain() },
    version = version,
    visibilityMode =
    when (visibilityMode) {
        "public" -> Container.VisibilityMode.Public

        "private" -> Container.VisibilityMode.Private

        else -> {
            Logger.hintNewVersion("visibilityMode=$visibilityMode")
            Container.VisibilityMode.Private
        }
    },
)

private fun CustomLogoDto.toDomain(): Container.CustomLogo? {
    val parsedSize =
        when (size) {
            "wide" -> Container.CustomLogo.Size.Wide
            "square" -> Container.CustomLogo.Size.Square
            else -> return null
        }
    val parsedBackground =
        when (intendedBackground) {
            "light" -> Container.CustomLogo.IntendedBackground.Light
            "dark" -> Container.CustomLogo.IntendedBackground.Dark
            else -> return null
        }
    return Container.CustomLogo(
        size = parsedSize,
        intendedBackground = parsedBackground,
        primaryColor = primaryColor,
        urlVector = urlVector,
    )
}
