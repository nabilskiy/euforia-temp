package digital.euforia.app.ui.util

inline fun <reified T> T.logTag(): String = T::class.java.simpleName