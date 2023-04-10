package com.example.appfortester

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Environment
import com.example.appfortester.installers.IntentInstallerVersion
import com.example.appfortester.installers.PackageInstallerVersion
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Constants.DATA_SENDING
import com.example.appfortester.utils.Constants.FILE_BASE_PATH
import com.example.appfortester.utils.Constants.FILE_NAME
import com.example.appfortester.utils.Constants.MIME_TYPE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class Downloader(
    private val context: Context
) {

    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    private var downloadedFileId: Long? = null

    suspend fun downloadFile(linkUrl: String) {

        val networkAddress = Uri.parse(linkUrl)
        val destinationUri = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME
        val uri = Uri.parse("$FILE_BASE_PATH$destinationUri")
        val request = DownloadManager.Request(networkAddress)
            .setTitle(FILE_NAME)
            .setMimeType(MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationUri(uri)
        downloadedFileId = downloadManager.enqueue(request)
        val intent = Intent(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        intent.putExtra(DATA_SENDING, downloadedFileId)
        context.sendBroadcast(intent)
    }
}


