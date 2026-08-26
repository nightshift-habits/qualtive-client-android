package io.qualtive.internal

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.util.Locale

internal interface DeviceAttributesCollector {
    fun collect(locale: Locale): Map<String, String>
}

internal class AndroidDeviceAttributesCollector(
    private val context: Context,
) : DeviceAttributesCollector {
    override fun collect(locale: Locale): Map<String, String> =
        standardDeviceAttributes(
            osVersion = Build.VERSION.RELEASE.orEmpty().ifBlank { "unknown" },
            deviceModel = Build.MODEL.orEmpty().ifBlank { "unknown" },
            deviceType = deviceTypeForSmallestWidthDp(context.resources.configuration.smallestScreenWidthDp),
            appId = appId(context),
            appVersion = appVersion(context),
            appBuild = appBuild(context),
            locale = locale,
        )

    private fun appId(context: Context): String? = context.packageName.takeIf { it.isNotBlank() }

    private fun appVersion(context: Context): String? =
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName?.takeIf { it.isNotBlank() }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

    @Suppress("DEPRECATION")
    private fun appBuild(context: Context): String? =
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode.toString()
            } else {
                info.versionCode.toString()
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
}

internal fun deviceTypeForSmallestWidthDp(smallestWidthDp: Int): String =
    if (smallestWidthDp >= 600) {
        "Tablet"
    } else {
        "Phone"
    }

internal fun standardDeviceAttributes(
    osVersion: String,
    deviceModel: String,
    deviceType: String,
    appId: String?,
    appVersion: String?,
    appBuild: String?,
    locale: Locale,
): Map<String, String> {
    val result = linkedMapOf<String, String>()
    result["Platform"] = "Android"
    result["OS"] = "Android"
    result["OS Version"] = osVersion
    result["Device Model"] = deviceModel
    result["Device Type"] = deviceType
    appId?.takeIf { it.isNotBlank() }?.let { result["App ID"] = it }
    appVersion?.takeIf { it.isNotBlank() }?.let { result["App Version"] = it }
    appBuild?.takeIf { it.isNotBlank() }?.let { result["App Build"] = it }
    locale.language.takeIf { it.isNotBlank() }?.let { result["Language"] = it }
    locale.country.takeIf { it.isNotBlank() }?.let { result["Region"] = it }
    return result
}

internal object EmptyDeviceAttributesCollector : DeviceAttributesCollector {
    override fun collect(locale: Locale): Map<String, String> = emptyMap()
}
