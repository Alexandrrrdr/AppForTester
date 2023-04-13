package com.example.appfortester.installers

import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.appfortester.BuildConfig
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Constants.FILE_BASE_PATH
import com.example.appfortester.utils.Constants.FILE_NAME
import java.io.File

class IntentInstallerVersion() {

    suspend fun installViaIntentMethod(context: Context) {
        val destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME
        val uri = Uri.parse("$FILE_BASE_PATH$destination")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val contentUri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + Constants.PROVIDER_PATH,
                File(destination)
            )
            val install = Intent(Intent.ACTION_VIEW)
            install.setDataAndType(contentUri, Constants.MIME_TYPE)
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            try {
                context.startActivity(install)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace();
                Log.d("info", "Error in opening the file!");
            }
        } else {
            val install = Intent(Intent.ACTION_VIEW)
            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            install.setDataAndType(
                uri,
                Constants.APP_INSTALL_PATH
            )
            context.startActivity(install)
        }
    }
}