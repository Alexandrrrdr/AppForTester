package com.example.appfortester

import android.content.Context
import android.net.Uri
import android.util.Log
import com.download.library.DownloadImpl
import com.download.library.DownloadListenerAdapter
import com.download.library.Extra
import com.example.appfortester.utils.Constants.FILE_NAME
import com.example.appfortester.utils.Constants.MAIN_URL
import com.ixuea.android.downloader.DownloadService
import com.ixuea.android.downloader.callback.DownloadListener
import com.ixuea.android.downloader.domain.DownloadInfo
import com.ixuea.android.downloader.exception.DownloadException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class LibreDownloader(private val context: Context) {

    private val downloadManager by lazy {
        DownloadService.getDownloadManager(context.applicationContext)
    }

    suspend fun secondLibreDownloading(){
        withContext(Dispatchers.IO){
            DownloadImpl.getInstance(context.applicationContext)
                .url(MAIN_URL)
                .setUniquePath(false)
                .setForceDownload(true)
                .target(File(context.cacheDir, FILE_NAME))
                .enqueue(object : DownloadListenerAdapter(){
                    override fun onStart(url: String?, userAgent: String?, contentDisposition: String?, mimetype: String?, contentLength: Long, extra: Extra?) {
                        super.onStart(url, userAgent, contentDisposition, mimetype, contentLength, extra)
                        Log.d("info", "onStart")
                    }

                    override fun onProgress(url: String?, downloaded: Long, length: Long, usedTime: Long) {
                        super.onProgress(url, downloaded, length, usedTime)
                        Log.d("info", "onProgress, downloaded - $downloaded, length - $length, used time - $usedTime")
                    }

                    override fun onResult(throwable: Throwable?, path: Uri?, url: String?, extra: Extra?
                    ): Boolean {
                        Log.d("info", "onResult ${extra?.userAgent}")
                        return super.onResult(throwable, path, url, extra)
                    }
                })
        }

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
                Log.d("info", "onDownloading $progress")
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