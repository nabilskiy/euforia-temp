package digital.euforia.app.domain.usecase.onboarding

import android.text.TextUtils
import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import javax.inject.Inject


const val EMAIL_MIN_LENGTH = 6

class ValidateEmailUseCase @Inject constructor() {
    operator fun invoke(email: String): Boolean =
        !(TextUtils.isEmpty(email) || !EMAIL_ADDRESS.matcher(email).matches() ||
                email.length < EMAIL_MIN_LENGTH)
}