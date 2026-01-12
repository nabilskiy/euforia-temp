package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.repository.PackageRepository
import digital.euforia.app.domain.model.PublicationInfo
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.publication.PublicationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPublicationInfosUseCase @Inject constructor(
    private val repository: PackageRepository
) {

    suspend operator fun invoke(
        publicationType: PublicationType,
        ids: List<Int>
    ): ResultWrapper<List<PublicationInfo>> {

        return withContext(Dispatchers.IO) {
            repository.getByIds(publicationType, ids)
        }
    }
}