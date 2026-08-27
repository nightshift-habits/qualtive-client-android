package io.qualtive.internal.enquiry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class EnquiryDto(
    val id: Long,
    val slug: String,
    val name: String,
    val pages: List<PageDto> = emptyList(),
    val submittedPages: List<SubmittedPageDto> = emptyList(),
    val theme: ThemeDto,
    val container: ContainerDto,
    val isUserContactDetailsRequired: Boolean = false,
)

@Serializable
internal data class PageDto(
    @Serializable(with = PageContentListSerializer::class)
    val content: List<PageContentDto> = emptyList(),
)

@Serializable
internal sealed class PageContentDto {
    @Serializable
    @SerialName("title")
    data class Title(
        val text: String,
    ) : PageContentDto()

    @Serializable
    @SerialName("body")
    data class Body(
        val text: String,
    ) : PageContentDto()

    @Serializable
    @SerialName("image")
    data class Image(
        val attachment: AttachmentDto,
    ) : PageContentDto()

    @Serializable
    @SerialName("score")
    data class Score(
        val scoreType: String,
        val leadingText: String? = null,
        val trailingText: String? = null,
    ) : PageContentDto()

    @Serializable
    @SerialName("text")
    data class Text(
        val placeholder: String? = null,
        val storageTarget: StorageTargetDto,
    ) : PageContentDto()

    @Serializable
    @SerialName("select")
    data class Select(
        val options: List<String> = emptyList(),
        val allowsCustomInput: Boolean = false,
    ) : PageContentDto()

    @Serializable
    @SerialName("multiselect")
    data class Multiselect(
        val options: List<String> = emptyList(),
    ) : PageContentDto()

    @Serializable
    @SerialName("attachments")
    data object Attachments : PageContentDto()

    @Serializable
    @SerialName("contactDetails")
    data class ContactDetails(
        val title: String,
        val placeholder: String? = null,
    ) : PageContentDto()
}

@Serializable
internal sealed class StorageTargetDto {
    @Serializable
    @SerialName("text")
    data object Text : StorageTargetDto()

    @Serializable
    @SerialName("attribute")
    data class Attribute(
        val attribute: String,
    ) : StorageTargetDto()
}

@Serializable
internal data class AttachmentDto(
    val url: String,
)

@Serializable
internal data class SubmittedPageDto(
    @Serializable(with = SubmittedContentListSerializer::class)
    val content: List<SubmittedContentDto> = emptyList(),
    @Serializable(with = ConditionListSerializer::class)
    val conditions: List<ConditionDto> = emptyList(),
)

@Serializable
internal sealed class SubmittedContentDto {
    @Serializable
    @SerialName("title")
    data class Title(
        val text: String,
    ) : SubmittedContentDto()

    @Serializable
    @SerialName("body")
    data class Body(
        val text: String,
    ) : SubmittedContentDto()

    @Serializable
    @SerialName("image")
    data class Image(
        val attachment: AttachmentDto,
        @SerialName("linkURL")
        val linkUrl: String? = null,
    ) : SubmittedContentDto()

    @Serializable
    @SerialName("confirmationText")
    data class ConfirmationText(
        val text: String,
    ) : SubmittedContentDto()

    @Serializable
    @SerialName("name")
    data object Name : SubmittedContentDto()

    @Serializable
    @SerialName("userInput")
    data object UserInput : SubmittedContentDto()

    @Serializable
    @SerialName("userInputScore")
    data object UserInputScore : SubmittedContentDto()

    @Serializable
    @SerialName("link")
    data class Link(
        val text: String,
        val url: String,
    ) : SubmittedContentDto()

    @Serializable
    @SerialName("reviewLinks")
    data class ReviewLinks(
        val links: List<ReviewLinkDto> = emptyList(),
    ) : SubmittedContentDto()
}

@Serializable
internal data class ReviewLinkDto(
    val title: String,
    val url: String,
    val logo: ReviewLinkLogoDto? = null,
    val icon: ReviewLinkIconDto? = null,
)

@Serializable
internal data class ReviewLinkLogoDto(
    val urlVector: String,
    val urlVectorDark: String,
)

@Serializable
internal data class ReviewLinkIconDto(
    val urlRaster: String,
    val urlRasterDark: String,
)

@Serializable
internal sealed class ConditionDto {
    @Serializable
    @SerialName("score")
    data class Score(
        val ranges: List<ScoreRangeDto> = emptyList(),
    ) : ConditionDto()
}

@Serializable
internal data class ScoreRangeDto(
    val lower: Int? = null,
    val upper: Int? = null,
)

@Serializable
internal data class ThemeDto(
    val background: BackgroundDto,
    val font: FontDto,
    val cornerStyle: String,
    val isBackgroundAttachmentVisibleInResponses: Boolean = true,
    val isBackgroundColorVisibleInResponses: Boolean = true,
)

@Serializable
internal sealed class BackgroundDto {
    @Serializable
    @SerialName("predefined")
    data class Predefined(
        val value: String,
    ) : BackgroundDto()

    @Serializable
    @SerialName("custom")
    data class Custom(
        val attachment: CustomBackgroundAttachmentDto? = null,
        val color: CustomBackgroundColorDto,
    ) : BackgroundDto()
}

@Serializable
internal data class CustomBackgroundAttachmentDto(
    val id: Long,
    val contentType: String,
    val url: String,
)

@Serializable
internal data class CustomBackgroundColorDto(
    val value: String,
)

@Serializable
internal sealed class FontDto {
    @Serializable
    @SerialName("predefined")
    data class Predefined(
        val value: String,
    ) : FontDto()

    @Serializable
    @SerialName("custom")
    data class Custom(
        val url: String,
    ) : FontDto()
}

@Serializable
internal data class ContainerDto(
    val id: String,
    val isWhiteLabel: Boolean = false,
    val customLogos: List<CustomLogoDto> = emptyList(),
    val visibilityMode: String,
)

@Serializable
internal data class CustomLogoDto(
    val size: String,
    val intendedBackground: String,
    val primaryColor: String,
    val urlVector: String,
)
