package com.example.appfortester.broadcasts

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi

class DownloadCompletedReceiver(): BroadcastReceiver() {

    private lateinit var downloadManager: DownloadManager
    private var filePatch = ""

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceive(context: Context, intent: Intent) {
        downloadManager = context.getSystemService(DownloadManager::class.java)
        val broadcastDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        var downloadingId = 0L
        val intentAction = intent.action
        if (intentAction == "intentExtra"){
            downloadingId = intent.getLongExtra("extra", 0L)
        }
        if (broadcastDownloadId == downloadingId){
            if (getDownloadStatus(id = downloadingId, dm = downloadManager) == DownloadManager.STATUS_SUCCESSFUL){
                Toast.makeText(context, "Downloaded successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Download in progress", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("Range")
    private fun getDownloadStatus(id: Long, dm: DownloadManager): Int{
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor: Cursor = dm.query(query)

        if (cursor.moveToFirst()){
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            filePatch = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            val status = cursor.getInt(columnIndex)
            return status
        }
        return DownloadManager.ERROR_UNKNOWN
    }
}