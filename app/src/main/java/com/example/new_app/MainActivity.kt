package com.example.new_app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.new_app.common.usable.saveImageUriPermission
import com.example.new_app.theme.New_AppTheme

class MainActivity : AppCompatActivity() {
    private val storagePermissionCode = 101

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //https://console.firebase.google.com/u/0/project/example-f27a3/authentication/emails
        //https://console.firebase.google.com/u/0/project/example-f27a3/settings/general/android:com.example.new_app

        //todo - swipe to delete task
        //todo - swipe down to refresh task list
        //todo - add a delete all button
        //todo - add a second screen that shows deleted tasks and a restore button
        //todo - add a second screen that shows completed tasks and a restore button
        //todo - settings screen
        //todo - account screen with optional username - first name - last name
        //todo - edit task screen
        //todo - add a search bar to search for tasks
        //todo - swipe to right to complete task - swipe to left to delete task
        //todo - notification when task is due
        //todo - snack-bar not always showing
        //todo - make sure the image is deleted when the task is deleted

        //todo - check if you can do something about the image not showing up after deleting the app and reinstalling it
        //todo - image orientation is not always correct

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            checkStoragePermission()
        }

        setContent {
            TaskApp(
                saveImageUriPermission = { uri -> saveImageUriPermission(this, uri) }
            )
        }

        restoreImageUriPermissions(this)
    }


    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), storagePermissionCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == storagePermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission is required to access images.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun restoreImageUriPermissions(context: Context) {
        val sharedPreferences = context.getSharedPreferences("uri_permissions", Context.MODE_PRIVATE)
        sharedPreferences.all.forEach { (key, value) ->
            if (value is String) {
                val uri = Uri.parse(value)
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }
    }
}





