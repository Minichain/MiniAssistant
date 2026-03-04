package com.minichain.miniassistant.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

@Composable
fun PermissionsHandler(
  activity: Activity,
  onAllPermissionsGranted: () -> Unit
) {
  val context = LocalContext.current
  ActivityCompat.requestPermissions(
    activity,
    arrayOf(
      Manifest.permission.POST_NOTIFICATIONS,
      Manifest.permission.RECORD_AUDIO
    ),
    0
  )
  LaunchedEffect(Unit) {
    launch {
      while (!permissionsGranted(context)) {
        // Do nothing...
      }
      onAllPermissionsGranted()
    }
  }
}

private fun permissionsGranted(context: Context): Boolean =
  isPostNotificationsPermissionGranted(context) &&
  isRecordAudioPermissionGranted(context)

private fun isPostNotificationsPermissionGranted(context: Context): Boolean =
  ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private fun isRecordAudioPermissionGranted(context: Context): Boolean =
  ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
