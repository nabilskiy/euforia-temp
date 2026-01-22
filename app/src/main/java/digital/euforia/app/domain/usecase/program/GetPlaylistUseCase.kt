package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.repository.MeditationRepository
import digital.euforia.app.data.repository.PackageRepository
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.player.PublicationsPlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPlaylistUseCase @Inject constructor(
    private val programRepository: PackageRepository,
    private val meditationRepository: MeditationRepository,
    private val publicationInfoMapper: PublicationInfoMapper
) {

    suspend operator fun invoke(categoryId: Int): ResultWrapper<PublicationsPlaylist> {
        return withContext(Dispatchers.IO) {
            programRepository.getProgramWithChildrenById(categoryId).map { pkgWithChildren ->
                val publicationInfos = pkgWithChildren?.meditations?.map {
                    publicationInfoMapper.fromMeditation(it, pkgWithChildren.pkg)
                } ?: emptyList()
                PublicationsPlaylist(publicationInfos)
            }
//            meditationRepository.getByMainCategoryId(categoryId).map { meditations ->
//                if (meditations.isNotEmpty()) {
//                    PublicationsPlaylist(publicationInfoMapper.fromMeditation() meditations)
//                } else {
//
//                }
//
//            }
//            val pkg = programRepository.getById(categoryId).dataOrNull
//            val meditationIds = pkg?.meditations?.map { it.id } ?: emptyList()
//            val existingMeditationIds = meditationRepository.getByIds(meditationIds).dataOrNull
//                ?.map { it.id } ?: emptyList() existingMeditationIds
        }
    }
}