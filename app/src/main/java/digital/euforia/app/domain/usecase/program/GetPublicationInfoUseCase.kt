package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.repository.PackageRepository
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.material.PublicationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPublicationInfoUseCase @Inject constructor(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(
        publicationType: PublicationType,
        publicationId: Int
    ): ResultWrapper<PublicationInfo> {
        return withContext(Dispatchers.IO) {
            packageRepository.getPublicationByIdAndType(
                publicationType = publicationType,
                id = publicationId
            )
        }
    }
}