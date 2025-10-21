package digital.euforia.app.domain.model.onboarding

import digital.euforia.app.R

enum class Gender(val value: String, val textRes: Int, val iconRes: Int? = null) {
    FEMALE("female", R.string.voice_for_female, R.drawable.ic_female),
    MALE("male", R.string.voice_for_male, R.drawable.ic_male),
    UNSPECIFIED("other", R.string.intro_genter_unspecified)
}