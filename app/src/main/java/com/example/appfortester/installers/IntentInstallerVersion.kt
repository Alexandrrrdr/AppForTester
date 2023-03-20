package com.example.appfortester.installers

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.viewbinding.BuildConfig
import com.example.appfortester.utils.Constants
import java.io.File

class IntentInstallerVersion(private val context: Context) {

    fun intentInstallation(
        destination: String,
        uri: Uri
    ) {
        Log.d("info", com.example.appfortester.BuildConfig.APPLICATION_ID)

        // set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    val contentUri = FileProvider.getUriForFile(
                        context,
                        com.example.appfortester.BuildConfig.APPLICATION_ID + ".provider",
                        File(destination)
                    )
                    val install = Intent(Intent.ACTION_VIEW)
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    install.data = contentUri
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                    // finish()
                } else {
                    val install = Intent(Intent.ACTION_VIEW)
                    install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    install.setDataAndType(
                        uri,
                        Constants.APP_INSTALL_PATH
                    )
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                    // finish()
                }
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}