package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.repository.PackageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncPackagesUseCase @Inject constructor(
    private val packageRepository: PackageRepository
) {

    suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            packageRepository.syncTopPackages()
        }
    }
}