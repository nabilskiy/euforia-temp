package digital.euforia.app.domain.util

fun getRandomFloat(min: Float, max: Float): Float {
    return (min + Math.random() * (max - min)).toFloat()
}