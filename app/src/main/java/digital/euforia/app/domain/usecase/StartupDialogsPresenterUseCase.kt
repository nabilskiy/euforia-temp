package digital.euforia.app.domain.usecase

import javax.inject.Inject

class StartupDialogsPresenterUseCase @Inject constructor(

) {
    suspend operator fun invoke() {

    }

}
enum class StartupDialogResult{
    CRITICAL_UPDATE,
    FEEDBACK,
    RATING,
}