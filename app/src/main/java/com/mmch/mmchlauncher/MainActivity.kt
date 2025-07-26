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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.mmch.mmchlauncher.ui.theme.MMCHLauncherTheme

/**
 * MainActivity: The main entry point for the MMCH Launcher application.
 * This launcher provides a simple, grid-based interface for launching installed applications.
 *
 * Features:
 * - Displays installed apps in a grid layout
 * - Shows system wallpaper with overlay
 * - Includes app search functionality
 * - Handles runtime permissions for external storage
 * - Prevents accidental launcher exit
 */
class MainActivity : ComponentActivity() {

    /**
     * Permission launcher for requesting READ_EXTERNAL_STORAGE.
     * Handles the result of the permission request and sets up the appropriate content.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupContent()
        } else {
            setupContentWithoutWallpaper()
        }
    }


    /**
     * Initializes the launcher activity and checks for required permissions.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissionAndSetup()
    }

    /**
     * Checks for READ_EXTERNAL_STORAGE permission and sets up the launcher accordingly.
     * If permission is granted, displays content with wallpaper.
     * If not granted, requests permission through the launcher.
     */
    private fun checkPermissionAndSetup() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                setupContent()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    /**
     * Sets up the main content of the launcher with wallpaper support.
     * Retrieves installed apps, system wallpaper, and initializes the UI.
     */
    private fun setupContent() {
        val installedApps = getInstalledApps(this).sortedBy { it.name }
        val wallpaperManager = WallpaperManager.getInstance(this)
        val wallpaperDrawable = wallpaperManager.drawable
        val wallpaperBitmap = (wallpaperDrawable as? BitmapDrawable)?.bitmap
        val wallpaperImageBitmap = wallpaperBitmap?.asImageBitmap()

        setContent {
            MMCHLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black // Set surface color to black
                ) {
                    AppList(installedApps, wallpaperImageBitmap)
                }
            }
        }
    }

    /**
     * Sets up the main content without wallpaper when permission is denied.
     * Provides a fallback UI with just the app grid on a solid background.
     */
    private fun setupContentWithoutWallpaper() {
        val installedApps = getInstalledApps(this).sortedBy { it.name }
        setContent {
            MMCHLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppList(installedApps, null)
                }
            }
        }
    }

    /**
     * Main composable function that displays the app list and search interface.
     *
     * @param apps List of installed applications to display
     * @param wallpaper Optional system wallpaper to use as background
     */
    @Composable
    fun AppList(apps: List<AppInfo>, wallpaper: ImageBitmap?) {
        val context = LocalContext.current
        var filterText by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            wallpaper?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.3f
                )
            }
            Column {
                TextField(
                    value = filterText,
                    onValueChange = { filterText = it },
                    label = { Text("Search apps", color = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.DarkGray,
                        focusedContainerColor = Color.DarkGray.copy(alpha = 0.7f),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    )
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filteredApps = apps.filter {
                        it.name.contains(filterText, ignoreCase = true)
                    }
                    items(filteredApps) { app ->
                        AppGridItem(app, context)
                    }
                }
            }
        }
    }

    /**
     * Composable function that renders a single app item in the grid.
     * Displays the app icon and name in a vertical layout.
     *
     * @param app AppInfo object containing the application details
     * @param context Context used for launching the application
     */
    @Composable
    fun AppGridItem(app: AppInfo, context: Context) {
        Column(
            modifier = Modifier
                .clickable { app.launch(context) }
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            app.icon?.toBitmap()?.asImageBitmap()?.let { iconBitmap ->
                Image(
                    bitmap = iconBitmap,
                    contentDescription = app.name,
                    modifier = Modifier.size(56.dp)
                )
            }
            Text(
                text = app.name,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(80.dp)
            )
        }
    }

    /**
     * Overrides the back button press to prevent accidental exits from the launcher.
     * This ensures the launcher remains active as the home screen.
     */
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Do nothing to prevent exiting the launcher
    }
}