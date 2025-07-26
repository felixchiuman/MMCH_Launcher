package com.mmch.mmchlauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

/**
 * Data class representing an installed application.
 *
 * @property name Display name of the application
 * @property packageName Package identifier of the application
 * @property icon Drawable containing the application's icon
 */
data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?
)

/**
 * Retrieves a list of installed applications that have a launcher intent.
 *
 * @param context Context used to access package manager
 * @return List of AppInfo objects representing launchable applications
 */
@SuppressLint("QueryPermissionsNeeded")
fun getInstalledApps(context: Context): List<AppInfo> {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val activities = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)

    return activities.map { resolveInfo ->
        AppInfo(
            name = resolveInfo.loadLabel(context.packageManager).toString(),
            packageName = resolveInfo.activityInfo.packageName,
            icon = resolveInfo.loadIcon(context.packageManager)
        )
    }
}

/**
 * Extension function to launch an application.
 *
 * @param context Context used to start the activity
 */
fun AppInfo.launch(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
    context.startActivity(intent)
}