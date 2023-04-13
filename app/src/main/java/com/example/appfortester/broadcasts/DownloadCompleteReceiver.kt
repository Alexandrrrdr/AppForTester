package com.example.appfortester.broadcasts

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.appfortester.installers.PackageInstallerVersion
import com.example.appfortester.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DownloadCompleteReceiver(): BroadcastReceiver() {

    private lateinit var installer: PackageInstallerVersion
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("Range")
    override fun onReceive(context: Context, intent: Intent) {
        installer = PackageInstallerVersion(context = context)
        val action: String = intent.action!!
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action && intent.extras != null) {
            val extras: Bundle = intent.extras!!

            val idFromDownloadManager = intent.getLongExtra("id", 0L)
            Log.d("info", "idFromDownloadManager - $idFromDownloadManager")

            val q = DownloadManager.Query()
            val downloadId: Long = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID)
            Log.d("info", "downloadId - $downloadId")
            q.setFilterById(downloadId)
            val c = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).query(q)
            if (c.moveToFirst()) {
                val status: Int = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when(status){
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        val downloadRequestId = intent.getLongExtra(Constants.DATA_SENDING, 0L)
                        Log.d("info", "Broadcast receiver - Download complete! Id is $downloadRequestId")
                        CoroutineScope(Dispatchers.Main).launch {
                            installer.startInstallApp()
                        }
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