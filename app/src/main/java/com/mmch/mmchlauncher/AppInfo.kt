package com.mmch.mmchlauncher

import android.graphics.drawable.Drawable

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable?
)

fun getInstalledApps(context: Context): List<AppInfo> {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val flags = PackageManager.MATCH_ALL.toLong()
    val activities: List<ResolveInfo> = context.packageManager.queryIntentActivities(intent, 0)

    return activities.map { resolveInfo: ResolveInfo ->
        AppInfo(
            name = resolveInfo.loadLabel(context.packageManager).toString(),
            packageName = resolveInfo.activityInfo.packageName,
            icon = resolveInfo.loadIcon(context.packageManager)
        )
    }
}