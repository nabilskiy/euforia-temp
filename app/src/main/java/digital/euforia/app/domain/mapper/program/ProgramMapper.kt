package digital.euforia.app.domain.mapper.program

import digital.euforia.app.data.model.NetworkPackage
import digital.euforia.app.ui.programs.ProgramUi
import javax.inject.Inject

fun NetworkPackage.toProgramUi(): ProgramUi = ProgramUi(
    id = id,
    isPremium = pro,
    authorId = authorId,
    name = name,
    subtitle = subtitle,
    description = description,
    keywords = keywords,
    imageUrl = imageUrl,
    imagePreviewUrl = imagePreviewUrl,
    imageCoverUrl = imageCoverUrl,
    color1 = color1,
    color2 = color2,
    color3 = color3,
    resourceCount = meditations.size + exercises.size + articles.size,
)