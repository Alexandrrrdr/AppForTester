package com.example.appfortester

import android.content.Context
import android.util.Log
import com.example.appfortester.utils.Constants.FILE_NAME
import com.example.appfortester.utils.Constants.MAIN_URL
import com.ixuea.android.downloader.DownloadService
import com.ixuea.android.downloader.callback.DownloadListener
import com.ixuea.android.downloader.domain.DownloadInfo
import com.ixuea.android.downloader.exception.DownloadException
import java.io.File


class LibreDownloader(private val context: Context) {

    private val downloadManager by lazy {
        DownloadService.getDownloadManager(context.applicationContext)
    }

    fun startDownload(){
        val filePath = File(context.cacheDir, FILE_NAME)
        val downloadInfo = DownloadInfo.Builder().setUrl(MAIN_URL)
            .setPath(filePath.absolutePath)
            .build()

        downloadInfo.downloadListener = object : DownloadListener {
            override fun onStart() {
                Log.d("info", "onStart start")
            }

            override fun onWaited() {
                Log.d("info", "onWaited waited")
            }

            override fun onPaused() {
                Log.d("info", "onPaused pause")
            }

            override fun onDownloading(progress: Long, size: Long) {
                Log.d("info", "onDownloading $size")
            }

            override fun onRemoved() {
                Log.d("info", "onRemoved removed")
            }

            override fun onDownloadSuccess() {
                Log.d("info", "onDownloadSuccess success")
            }

            override fun onDownloadFailed(e: DownloadException?) {
                Log.d("info", "onDownloadFailed ${e!!.cause}")
            }
        }
        downloadManager.download(downloadInfo)
    }
}