package digital.euforia.app.domain.usecase.plan

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.repository.PackageRepository
import digital.euforia.app.domain.model.plan.ExtraPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetExtraPackageFlowUseCase @Inject constructor(
    private val packageRepository: PackageRepository,
    private val configFetcher: EuforiaRemoteConfigFetcher
) {
    suspend operator fun invoke(): Flow<ExtraPackage?> {
        return withContext(Dispatchers.IO) {
            val extraPackageId = configFetcher.getExtraPackageId().orEmpty()
            packageRepository.getPackageByIdFlow(extraPackageId)
                .mapLatest { packageWithMeditations ->
                    packageWithMeditations?.let {
                        ExtraPackage(
                            id = packageWithMeditations.pkg.id,
                            imgUrls = packageWithMeditations.meditations.mapNotNull { it.imageCoverUrl }
                        )
                    }
                }
        }
    }
}
