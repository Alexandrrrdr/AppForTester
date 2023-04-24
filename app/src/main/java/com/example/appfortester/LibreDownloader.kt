package com.example.appfortester

import android.content.Context
import android.util.Log
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Constants.FILE_NAME
import com.ixuea.android.downloader.DownloadService
import com.ixuea.android.downloader.domain.DownloadInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LibreDownloader(private val context: Context) {

    suspend fun startDownload(){
        val downloadManager = DownloadService.getDownloadManager(context.applicationContext)

        val cachePath = File(context.applicationContext.cacheDir.absolutePath, FILE_NAME)

        val downloadInfo = DownloadInfo.Builder()
            .setUrl(Constants.MAIN_URL)
            .setPath(cachePath.absolutePath)
            .build()

        withContext(Dispatchers.IO){
            downloadManager.download(downloadInfo)
            Log.d("info", "${context.applicationContext.cacheDir}")
            Log.d("info", context.applicationContext.cacheDir.absolutePath)
            Log.d("info", "${downloadManager.getDownloadById(downloadInfo.id)}")

//            val downloadedFileId = downloadManager.getDownloadById(downloadInfo.id)
//            val intent = Intent(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
//            intent.putExtra("id", downloadedFileId)
//            context.sendBroadcast(intent)
        }
    }
}