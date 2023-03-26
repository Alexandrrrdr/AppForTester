package com.example.appfortester.broadcasts

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.appfortester.Downloader


class DownloadCompleteReceiver(): BroadcastReceiver() {
    val packageInstalledAction =
        "com.example.testappinstaller.data.repository.SESSION_API_PACKAGE_INSTALLED"
    private lateinit var downloader: Downloader
    @SuppressLint("Range")
    override fun onReceive(context: Context, intent: Intent) {
        downloader = Downloader(context = context)
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
                        downloader.installViaPackageInstaller()
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