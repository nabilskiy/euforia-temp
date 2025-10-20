package digital.euforia.app.data.util

import java.security.MessageDigest

val String.md5 get() = MessageDigest.getInstance("MD5")
    .digest(this.toByteArray())
    .joinToString("") { "%02x".format(it) }

fun Boolean.toInt() = if (this) 1 else 0