package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.db.entity.Package
import digital.euforia.app.data.repository.PackageRepository
import digital.euforia.app.domain.model.plan.RankedPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetTopProgramsFlowUseCase @Inject constructor(
    private val packageRepository: PackageRepository
) {

    suspend operator fun invoke(): Flow<List<RankedPackage>> {
        withContext(Dispatchers.IO) {}
        return packageRepository.getTopPackagesFlow().mapLatest { packagesList ->
            packagesList.mapIndexed { index, pkg ->
                RankedPackage(
                    rank = index + 1,
                    id = pkg.pkg.id,
                    title = pkg.pkg.name,
                    description = pkg.pkg.description.orEmpty(),
                    imageUrl = pkg.meditations.firstOrNull()?.imageCoverUrl.orEmpty(),
                )
            }

        }
    }
}