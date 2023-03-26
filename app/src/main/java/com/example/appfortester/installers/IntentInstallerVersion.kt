package com.example.appfortester.installers

import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.appfortester.BuildConfig
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Constants.FILE_BASE_PATH
import com.example.appfortester.utils.Constants.FILE_NAME
import java.io.File

class IntentInstallerVersion(private val context: Context) {

    fun intentInstallation() {
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
//        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()

        val uri = Uri.parse(FILE_BASE_PATH + path)
        Log.e("info", "intentInstallation - $uri")
        val file = File(path, FILE_NAME)
        if (file.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val contentUri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + Constants.PROVIDER_PATH,
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                intent.data = contentUri
                context.startActivity(intent)
            } else {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.setDataAndType(
                    uri,
                    "application/vnd.android.package-archive"
                )
                context.startActivity(intent)
            }
        }}
//        if (file.exists()){
//
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.setDataAndType(uriFromFile(context, file),
//                "application/vnd.android.package-archive"
//            )
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
//            intent.data = uriFromFile(context, file)
//            try {
//                Log.e("info", "intentInstallation - try to start")
//                context.startActivity(intent)
//            } catch (e: ActivityNotFoundException) {
//                e.printStackTrace()
//                Log.e("info", "Error in opening the file!")
//            }
//        } else {
//            Log.e("info", "intentInstallation - file not exists")
//            Toast.makeText(context, "Installing!!!", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun uriFromFile(context: Context, file: File): Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(
                context,
                com.example.appfortester.BuildConfig.APPLICATION_ID + ".provider",
                file
            );
        } else {
            return Uri.fromFile(file);
        }
    }
//    fun intentInstallation(
//        destination: String,
//        uri: Uri
//    ) {
//        // set BroadcastReceiver to install app when .apk is downloaded
//        val onComplete = object : BroadcastReceiver() {
//            override fun onReceive(
//                context: Context,
//                intent: Intent
//            ) {
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//
//                    val contentUri = FileProvider.getUriForFile(
//                        context,
//                        com.example.appfortester.BuildConfig.APPLICATION_ID + ".provider",
//                        File(destination)
//                    )
//                    val install = Intent(Intent.ACTION_VIEW)
//                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
//                    install.data = contentUri
//                    context.startActivity(install)
//                    context.unregisterReceiver(this)
//                    // finish()
//                } else {
//                    val install = Intent(Intent.ACTION_VIEW)
//                    install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                    install.setDataAndType(
//                        uri,
//                        Constants.APP_INSTALL_PATH
//                    )
//                    context.startActivity(install)
//                    context.unregisterReceiver(this)
//                    // finish()
//                }
//            }
//        }
//        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
//    }
}