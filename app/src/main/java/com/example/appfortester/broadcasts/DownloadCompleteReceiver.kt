package com.example.appfortester.broadcasts

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast


class DownloadCompleteReceiver(): BroadcastReceiver() {
    @SuppressLint("Range")
    override fun onReceive(context: Context, intent: Intent) {

        val action: String = intent.action!!
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action && intent.extras != null) {
            val extras: Bundle = intent.extras!!
            val q = DownloadManager.Query()
            val downloadId: Long = extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID)
            q.setFilterById(downloadId)
            val c = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).query(q)
            if (c.moveToFirst()) {
                val status: Int = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val downloadFilePath: String =
                        c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI))
                            .replace("file://", "")
                    val downloadTitle: String =
                        c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE))
                    c.close()
                    Toast.makeText(context, "DownloadPath-$downloadFilePath", Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, "DownloadTitle-$downloadTitle", Toast.LENGTH_SHORT).show()
                } else if (status == DownloadManager.STATUS_FAILED) {
                    val code = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON))
                    Toast.makeText(
                        context,
                        "donwload filed: $code",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}