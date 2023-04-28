package com.example.appfortester

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.download.library.DownloadImpl
import com.download.library.DownloadListenerAdapter
import com.download.library.Extra
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Constants.DOWNLOAD_COMPLETE
import com.example.appfortester.utils.Constants.DOWNLOAD_LINK
import com.example.appfortester.utils.Constants.FILE_NAME
import com.example.appfortester.utils.Constants.INTENT_EXTRA_NAME
import com.example.appfortester.utils.Constants.TEST_PACKAGE_NAME
import com.ixuea.android.downloader.DownloadService
import com.ixuea.android.downloader.callback.DownloadListener
import com.ixuea.android.downloader.domain.DownloadInfo
import com.ixuea.android.downloader.exception.DownloadException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class LibreDownloader(private val context: Context) {

    private val downloadManager by lazy {
        DownloadService.getDownloadManager(context.applicationContext)
    }

    private val myDownloadBroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == DOWNLOAD_COMPLETE){
                CoroutineScope(Dispatchers.Main).launch {
                    startInstallation()
                }
            }
        }
    }

    suspend fun secondLibreDownloading(){
        IntentFilter(DOWNLOAD_COMPLETE).also {
            context.registerReceiver(myDownloadBroadcastReceiver, it)
        }

        withContext(Dispatchers.IO){
            DownloadImpl.getInstance(context.applicationContext)
                .url(DOWNLOAD_LINK)
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
                        val intent = Intent(DOWNLOAD_COMPLETE)
                        intent.putExtra(INTENT_EXTRA_NAME, FILE_NAME)
                        context.sendBroadcast(intent)
                        return super.onResult(throwable, path, url, extra)
                    }
                })
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    suspend fun startInstallation() {
        if (!isPackageInstalled(TEST_PACKAGE_NAME)){

        withContext(Dispatchers.IO) {
            val resolver: ContentResolver = context.applicationContext.contentResolver
            val installer: PackageInstaller = context.applicationContext.packageManager.packageInstaller
            val fileLocation = File(context.cacheDir, FILE_NAME)
            var fileLength = 0L
//                val file = File(fileLocation)
            if (fileLocation.isFile) {
                fileLength = fileLocation.length()
            } else {
                Log.d("info", "File is not existing... How it's working?!")
            }
            var sessionId = 0
            var session: PackageInstaller.Session? = null

            resolver.openInputStream(Uri.fromFile(fileLocation)).use { apkStream ->
                val params =
                    PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                try {
                    sessionId = installer.createSession(params)
                } catch (e: Exception) {
                    Log.d("info", "Couldn't create session")
                }

                try {
                    session = installer.openSession(sessionId)
                } catch (e: Exception) {
                    Log.d("info", "Couldn't open session")
                    return@withContext
                }

                session!!.openWrite(Constants.PACKAGE, 0, fileLength).use { outputStream ->
                    apkStream?.copyTo(outputStream)
                    session!!.fsync(outputStream)
                }

                val myBroadcastReceiver = object : BroadcastReceiver(){
                    override fun onReceive(context: Context, intent: Intent) {
                        when(val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)){
                            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                                val activityIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                                if (activityIntent != null) {
                                    context.startActivity(activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            }
                            PackageInstaller.STATUS_SUCCESS -> {
                                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                                Log.d("info", "$status,$msg")
                            }
                        }
                    }
                }

                IntentFilter(PackageInstaller.EXTRA_STATUS).also {
                    context.registerReceiver(myBroadcastReceiver, it)
                }

                val intent = Intent(PackageInstaller.EXTRA_STATUS)
                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getBroadcast(
                        context.applicationContext,
                        Constants.REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_MUTABLE
                    )
                } else {
                    PendingIntent.getBroadcast(
                        context.applicationContext,
                        Constants.REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
                session!!.commit(pendingIntent.intentSender)
                session!!.close()
            }
        }
        } else {
            Toast.makeText(context, "Application already exists", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        val packageManager = context.packageManager
        return try {
            packageManager.getPackageInfo(packageName, 0)
            Log.d("info", "$packageName is exists")
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("info", "$packageName doesn't exist")
            false
        }
    }

    fun startDownload(){
        val filePath = File(context.cacheDir, FILE_NAME)
        val downloadInfo = DownloadInfo.Builder().setUrl(DOWNLOAD_LINK)
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