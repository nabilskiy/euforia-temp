package digital.euforia.app.domain.model.onboarding

import digital.euforia.app.R

enum class Language(
    val tag: String,
    val titleRes: Int,
    val iconRes: Int,
    val isSupported: Boolean = true,
    val hasSample: Boolean = false,
) {

    EN(tag = "en", titleRes = R.string.lang_en, iconRes = R.drawable.ic_flag_en, hasSample = true),
    ES(tag = "es", titleRes = R.string.lang_es, iconRes = R.drawable.ic_flag_es, hasSample = true),
//    UK(tag = "uk", titleRes = R.string.lang_uk, iconRes = R.drawable.ic_flag_uk),
    PT(
        tag = "pt",
        titleRes = R.string.lang_pt,
        iconRes = R.drawable.ic_flag_pt,
        isSupported = false
    ),
    FR(
        tag = "fr",
        titleRes = R.string.lang_fr,
        iconRes = R.drawable.ic_flag_fr,
        isSupported = false
    ),
    DE(
        tag = "de",
        titleRes = R.string.lang_de,
        iconRes = R.drawable.ic_flag_de,
        isSupported = false
    ),
}

fun getByTag(tag: String?): Language {
    return Language.entries.firstOrNull { it.tag == tag } ?: Language.EN
}

fun getVoiceSamplesRes(): List<Int> {
    return listOf(
        R.raw.voice_intro_male_en,
        R.raw.voice_intro_female_en,
        R.raw.voice_intro_male_es,
        R.raw.voice_intro_female_es
    )
}

fun Language.getVoiceRes(gender: Gender): Int? {
    return when (this) {
        Language.EN -> {
            when (gender) {
                Gender.MALE -> R.raw.voice_intro_male_en
                else -> R.raw.voice_intro_female_en
            }
        }

        Language.ES -> {
            when (gender) {
                Gender.MALE -> R.raw.voice_intro_male_es
                else -> R.raw.voice_intro_female_es
            }
        }

        else -> null
    }
}