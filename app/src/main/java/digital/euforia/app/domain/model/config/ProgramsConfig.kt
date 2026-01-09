package digital.euforia.app.domain.model.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import digital.euforia.app.data.config.NetworkBlockData
import digital.euforia.app.data.config.NetworkPackageConfig

@JsonClass(generateAdapter = true)
data class ProgramsConfig(
    @Json(name = "type")
    val type: BlockType? = null,

    @Json(name = "data")
    val data: BlockData? = null
)

@JsonClass(generateAdapter = true)
data class BlockData(
    @Json(name = "title")
    val title: String? = null,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "maxItems")
    val maxItems: Int? = null,

    @Json(name = "headerAction")
    val headerAction: String? = null
)

enum class BlockType {
    @Json(name = "package_list")
    PACKAGE_LIST,

    @Json(name = "exercise_list")
    EXERCISE_LIST,

    @Json(name = "article_list")
    ARTICLE_LIST,

    @Json(name = "divider")
    DIVIDER
}


fun NetworkPackageConfig.toDomain(): ProgramsConfig {
    return ProgramsConfig(
        type = type.toBlockType(),
        data = data?.toDomain()
    )
}

fun NetworkBlockData.toDomain(): BlockData {
    return BlockData(
        title = title,
        description = description,
        maxItems = maxItems,
        headerAction = headerAction
    )
}

fun String.toBlockType(): BlockType = when (this) {
    "package_list" -> BlockType.PACKAGE_LIST
    "exercise_list" -> BlockType.EXERCISE_LIST
    "article_list" -> BlockType.ARTICLE_LIST
    else -> BlockType.DIVIDER
}

