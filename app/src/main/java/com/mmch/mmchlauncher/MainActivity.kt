package com.mmch.mmchlauncher

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.mmch.mmchlauncher.ui.theme.MMCHLauncherTheme

/**
 * Extension function to launch an app given its package name.
 */
fun AppInfo.launch(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return
    context.startActivity(intent)
}

class MainActivity : ComponentActivity() {
    /**
     * Register for activity result to request multiple permissions.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
                // Permission granted, proceed with your logic
                setupContent()
            } else {
                // Permission denied, handle accordingly
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            // Permission already granted, proceed with your logic
            setupContent()
        }
    }

    /**
     * Setup the content of the activity.
     */
    private fun setupContent() {
        val installedApps = getInstalledApps(this).sortedBy { it.name } // Sort apps alphabetically
        val wallpaperManager = WallpaperManager.getInstance(this)
        val wallpaperDrawable = wallpaperManager.drawable
        val wallpaperBitmap = (wallpaperDrawable as BitmapDrawable).bitmap
        val wallpaperImageBitmap = wallpaperBitmap.asImageBitmap()

        setContent {
            MMCHLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppList(installedApps, wallpaperImageBitmap)
                }
            }
        }
    }

    /**
     * Composable function to display the list of apps.
     */
    @Composable
    fun AppList(apps: List<AppInfo>, wallpaper: ImageBitmap) {
        val context = LocalContext.current
        var filterText by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Image(
                bitmap = wallpaper,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
            )
            Column {
                TextField(
                    value = filterText,
                    onValueChange = { filterText = it },
                    label = { Text("Search apps") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                LazyColumn {
                    val filteredApps = apps.filter { it.name.contains(filterText, ignoreCase = true) }
                    items(filteredApps) { app ->
                        AppItem(app, context)
                    }
                }
            }
        }
    }

    /**
     * Composable function to display a single app item.
     */
    @Composable
    fun AppItem(app: AppInfo, context: Context) {
        Row(
            modifier = Modifier
                .clickable { app.launch(context) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                bitmap = app.icon?.toBitmap()?.asImageBitmap()!!,
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .width(48.dp)
                    .height(48.dp)
            )
            Text(
                text = app.name
            )
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Do nothing
    }
}