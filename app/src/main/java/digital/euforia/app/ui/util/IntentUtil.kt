package digital.euforia.app.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import digital.euforia.app.R
import digital.euforia.app.ui.subscription.Configuration


private const val MARKET_APP_LINK = "market://details?id="
private const val MARKET_DEEP_LINK = "https://play.google.com/store/apps/details?id="
private const val SUPPORT_EMAIL = "support@euforia.digital"
private const val DEVELOPER_LINK = "https://oncreate.com/"
private const val TIKTOK_PACKAGE = "com.zhiliaoapp.musically"
private const val X_PACKAGE = "com.twitter.android"
private const val INSTAGRAM_PACKAGE = "com.instagram.android"
private const val FACEBOOK_PACKAGE = "com.facebook.katana"
private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
private const val SOCIAL_USERNAME = "euforia.digital"
private const val FACEBOOK_PAGE_ID = "euforia.mobileapp"
private const val PLAY_STORE_SUBSCRIPTION_URL =
    "https://play.google.com/store/account/subscriptions"
private const val PLAY_STORE_SUBSCRIPTION_DEEPLINK_URL =
    "https://play.google.com/store/account/subscriptions?sku=%s&package=%s";
private const val ABOUT_URL = "https://euforia.digital/info/subscriptions"


/**
 * Launches an email intent targeted to the support address.
 * Uses ACTION_SENDTO with a mailto: URI so only email apps are shown.
 */
fun sendSupportEmail(
    context: Context,
    subject: String? = null,
    body: String? = null
) {
    val email = Configuration.SUPPORT_EMAIL
    val mailUri = Uri.parse("mailto:$email")
    val intent = Intent(Intent.ACTION_SENDTO, mailUri).apply {
        // In case application context is passed
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject.orEmpty())
        if (!body.isNullOrBlank()) putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No email app available – do nothing
    }
}

fun openDeveloperLink(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DEVELOPER_LINK)).apply {
        // In case application context is passed
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No activity can handle the intent; nothing to do
    }
}

/**
 * Opens the given URL in a browser using ACTION_VIEW.
 * Safe to call with Application context.
 */
private fun openWebLink(context: Context, url: String?) {
    if (url.isNullOrBlank()) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No activity can handle the intent; ignore
    } catch (_: Exception) {
        // Ignore malformed URIs
    }
}

fun openUserAgreement(context: Context) {
    openWebLink(context, context.getString(R.string.link_terms))
}

fun openPrivacyPolicy(context: Context) {
    openWebLink(context, context.getString(R.string.link_privacy))
}

fun openCopyrightNotice(context: Context) {
    openWebLink(context, context.getString(R.string.link_copyright))
}

fun openAboutEuforia(context: Context) {
    openWebLink(context, context.getString(R.string.link_about))
}

fun openAboutSubscriptions(context: Context) {
    openWebLink(context, ABOUT_URL)
}

/**
 * Opens a specific TikTok account. Tries to open the TikTok app first, then falls back to a browser.
 *
 * Accepted inputs:
 *  - plain username (e.g., "euforia")
 *  - username with leading @ (e.g., "@euforia")
 *  - full profile URL (e.g., "https://www.tiktok.com/@euforia")
 */
fun openTikTokAccount(context: Context) {
    val username = extractTikTokUsername(SOCIAL_USERNAME) ?: return

    // Try deep link via TikTok app
    val deepLinks = listOf(
        "tiktok://user/@$username",
        "tiktok://profile/@$username",
        // official documented format
        "tiktok://user?uniqueId=$username"
    )

    for (link in deepLinks) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
            setPackage(TIKTOK_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(appIntent)
            return
        } catch (_: ActivityNotFoundException) {
            // Try next option
        } catch (_: Exception) {
            // Ignore malformed URI or other issues and try next
        }
    }

    // Try HTTPS URL with package hint (may still open TikTok app if installed)
    val httpsWithPackage = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.tiktok.com/@$username")
    ).apply {
        setPackage(TIKTOK_PACKAGE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(httpsWithPackage)
        return
    } catch (_: ActivityNotFoundException) {
        // Fall back to browser without package
    }

    // Final fallback: open in any browser
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.tiktok.com/@$username")
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    try {
        context.startActivity(webIntent)
    } catch (_: ActivityNotFoundException) {
        // No activity can handle the intent; nothing to do
    }
}

private fun extractTikTokUsername(input: String): String? {
    // Trim spaces
    var s = input.trim()
    if (s.isEmpty()) return null

    // If it's a URL, try to parse username from path
    if (s.startsWith("http://", true) || s.startsWith("https://", true)) {
        return try {
            val uri = Uri.parse(s)
            // Typical path: /@username or sometimes /@username/video/...
            val segments = uri.pathSegments
            if (segments.isNotEmpty()) {
                var candidate = segments[0]
                if (candidate.startsWith("@")) candidate = candidate.substring(1)
                candidate.takeIf { it.isNotBlank() }
            } else null
        } catch (_: Exception) {
            null
        }
    }

    // Otherwise treat as plain username, remove leading '@'
    if (s.startsWith("@")) s = s.substring(1)
    // Remove any trailing slashes or spaces
    s = s.trim('/', ' ')
    return s.takeIf { it.isNotBlank() }
}

/**
 * Opens the X (formerly Twitter) account in the app if available, otherwise in the browser.
 */
fun openXAccount(context: Context) {
    val username = normalizeUsername(SOCIAL_USERNAME) ?: return

    // Try X/Twitter app deep link
    val appIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("twitter://user?screen_name=$username")
    ).apply {
        setPackage(X_PACKAGE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(appIntent)
        return
    } catch (_: ActivityNotFoundException) {
        // Fall back
    }

    // Try HTTPS with package hint
    val httpsWithPackage = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://x.com/$username")
    ).apply {
        setPackage(X_PACKAGE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(httpsWithPackage)
        return
    } catch (_: ActivityNotFoundException) {
        // Fall back to browser
    }

    // Final fallback: any browser
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://x.com/$username")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(webIntent)
    } catch (_: ActivityNotFoundException) {
    }
}

/**
 * Opens the Instagram account in the app if available, otherwise in the browser.
 */
fun openInstagramAccount(context: Context) {
    val username = normalizeUsername(SOCIAL_USERNAME) ?: return

    // Try Instagram deep link
    val appIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("http://instagram.com/_u/$username")
    ).apply {
        setPackage(INSTAGRAM_PACKAGE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(appIntent)
        return
    } catch (_: ActivityNotFoundException) {
        // Fall back
    }

    // Alternative deep link
    val altAppIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("instagram://user?username=$username")
    ).apply {
        setPackage(INSTAGRAM_PACKAGE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(altAppIntent)
        return
    } catch (_: ActivityNotFoundException) {
    }

    // Browser fallback
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/$username"))
        .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    try {
        context.startActivity(webIntent)
    } catch (_: ActivityNotFoundException) {
    }
}

/**
 * Opens the Facebook page/account in the app if available, otherwise in the browser.
 */
fun openFacebookAccount(context: Context) {
    val username = normalizeUsername(FACEBOOK_PAGE_ID) ?: return
    val pageUrl = "https://www.facebook.com/$username"

    // Try open via Facebook app using facewebmodal which supports username/URL
    val fbAppIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("fb://facewebmodal/f?href=$pageUrl")
    ).apply {
        setPackage(FACEBOOK_PACKAGE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(fbAppIntent)
        return
    } catch (_: ActivityNotFoundException) {
        // Fall back
    }

    // Try HTTPS with package hint
    val httpsWithPackage = Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl)).apply {
        setPackage(FACEBOOK_PACKAGE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(httpsWithPackage)
        return
    } catch (_: ActivityNotFoundException) {
    }

    // Final: open in any browser
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(webIntent)
    } catch (_: ActivityNotFoundException) {
    }
}

/**
 * Opens the YouTube channel/profile in the app if available, otherwise in the browser.
 */
fun openYouTubeAccount(context: Context) {
    val username = normalizeUsername(SOCIAL_USERNAME) ?: return

    // Try a few common YouTube deep links
    val candidates = listOf(
        // New handles style
        "https://www.youtube.com/@$username",
        // Legacy user and custom channel URL patterns
        "https://www.youtube.com/user/$username",
        "https://www.youtube.com/c/$username"
    )

    // Prefer opening in app by hinting the YouTube package
    for (url in candidates) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage(YOUTUBE_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(appIntent)
            return
        } catch (_: ActivityNotFoundException) {
        }
    }

    // Final fallback: open the primary handle URL in browser
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.youtube.com/@$username")
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    try {
        context.startActivity(webIntent)
    } catch (_: ActivityNotFoundException) {
    }
}

// Helper to normalize a shared social username value (remove '@', trim spaces and slashes)
private fun normalizeUsername(input: String): String? {
    var s = input.trim()
    if (s.isEmpty()) return null
    if (s.startsWith("@")) s = s.substring(1)
    s = s.trim('/', ' ')
    return s.takeIf { it.isNotBlank() }
}

fun openPlayStoreSubscriptions(context: Context, sku: String?) {
    val url = sku?.let {
        PLAY_STORE_SUBSCRIPTION_DEEPLINK_URL.format(
            it,
            context.packageName
        )
    } ?: PLAY_STORE_SUBSCRIPTION_URL

    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
    )
}

fun openAppPage(context: Context, packageName: String = context.packageName) {
    val marketIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$packageName")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(marketIntent)
    } catch (e: ActivityNotFoundException) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
    }
}

/**
 * Opens the dialer with the provided phone number prefilled.
 * Uses ACTION_DIAL so no CALL_PHONE permission is required.
 */
fun callPhoneNumber(context: Context, phone: String?) {
    val number = phone?.trim()?.takeIf { it.isNotEmpty() } ?: return
    // Keep digits and common dialing symbols (+, #, *, pauses ',', waits ';')
    val sanitized = number.replace(Regex("[^0-9+#*;,]"), "")
    if (sanitized.isEmpty()) return

    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No dialer available; ignore
    }
}

fun openSystemSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}