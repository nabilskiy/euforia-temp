package digital.euforia.app.domain.usecase.program

import digital.euforia.app.data.api.EuforiaApi
import digital.euforia.app.domain.mapper.program.PublicationInfoMapper
import digital.euforia.app.domain.util.ResultWrapper
import digital.euforia.app.ui.programs.player.PublicationsPlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPlaylistUseCase @Inject constructor(
    private val publicationInfoMapper: PublicationInfoMapper,
    private val api: EuforiaApi
) {

    suspend operator fun invoke(packageId: Int): ResultWrapper<PublicationsPlaylist> {
        return withContext(Dispatchers.IO) {
            api.getPackage(packageId).map { networkPackage ->
                val publicationInfos = networkPackage.meditations.map { networkMeditation ->
                    publicationInfoMapper.fromNetworkMeditation(
                        meditation = networkMeditation,
                        videoCoverUrl = networkPackage.videoCoverUrl
                    )
                }
                PublicationsPlaylist(publicationInfos)
            }
        }
    }
}