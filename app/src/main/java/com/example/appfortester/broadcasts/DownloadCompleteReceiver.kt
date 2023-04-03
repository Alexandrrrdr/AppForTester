package com.example.appfortester.broadcasts

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.appfortester.Downloader
import com.example.appfortester.installers.IntentInstallerVersion
import com.example.appfortester.installers.PackageInstallerVersion
import com.example.appfortester.utils.Constants.FILE_NAME
import java.io.File


class DownloadCompleteReceiver(): BroadcastReceiver() {

    private lateinit var installer: IntentInstallerVersion
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("Range")
    override fun onReceive(context: Context, intent: Intent) {
        installer = IntentInstallerVersion()
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(path, FILE_NAME)
        val action: String = intent.action!!
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action && intent.extras != null) {
            val extras: Bundle = intent.extras!!
            val q = DownloadManager.Query()
            val downloadId: Long = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID)
            q.setFilterById(downloadId)
            val c = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).query(q)
            if (c.moveToFirst()) {
                val status: Int = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when(status){
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        Log.d("info", "Broadcast receiver - Download complete!")
                        installer.installViaIntentMethod(context)
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        Log.d("info", "Broadcast receiver - Running!")
                    }
                    DownloadManager.STATUS_FAILED -> {
                        Log.d("info", "Broadcast receiver - FAILED!")
                    }
                }
            }
            c.close()
        }
    }
}