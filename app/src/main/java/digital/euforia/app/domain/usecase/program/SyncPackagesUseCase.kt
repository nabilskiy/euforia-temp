package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.config.EuforiaRemoteConfigFetcher
import digital.euforia.app.data.db.entity.Package
import digital.euforia.app.data.repository.PackageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncPackagesUseCase @Inject constructor(
    private val packageRepository: PackageRepository,
    private val configFetcher: EuforiaRemoteConfigFetcher
) {

    suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            val extraPackageId = configFetcher.getExtraPackageId()
            val ids = Package.TOP_PACKAGES_IDS + listOfNotNull(extraPackageId)
            packageRepository.syncPackages(ids)
        }
    }
}