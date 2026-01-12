package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.repository.PackageRepository
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.publication.PublicationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetSimilarPublicationsUseCase @Inject constructor(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(
        publicationType: PublicationType,
        categoryId: Int?,
        excludePublicationId: Int,
        limit: Int = 30
    ): ResultWrapper<List<PublicationInfo>> {
        return withContext(Dispatchers.IO) {
            packageRepository.getSimilarPublications(
                publicationType = publicationType,
                categoryId = categoryId
            ).map { publicationsInfos ->
                publicationsInfos.filter { it.id != excludePublicationId }
            }
        }
    }
}