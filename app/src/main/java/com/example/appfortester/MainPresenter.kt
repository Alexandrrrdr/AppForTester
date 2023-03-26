package com.example.appfortester

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.appfortester.installers.IntentInstallerVersion
import com.example.appfortester.installers.PackageInstallerVersion
import com.example.appfortester.utils.Constants.FILE_NAME
import com.example.appfortester.utils.Constants.MAIN_URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moxy.InjectViewState
import moxy.MvpPresenter
import java.io.File


@InjectViewState
class MainPresenter(
    private val downloader: Downloader,
    private val context: Context
) : MvpPresenter<MainView>() {

    private val intentInstallerVersion = IntentInstallerVersion(context = context)
    private val packageInstallerVersion = PackageInstallerVersion(context = context)
    fun downloadFile() {
        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME
        val file = File(filePath)
        if (file.exists()) {
            fileIsDownloaded()
            Log.d("info", " download - File exists, start installation")
            downloader.installPackageLastVersion()
        } else {
            Log.d("info", "download - File don't exists")
//            context.registerReceiver(downloadBroadcastReceiver,
//                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
//            )
            Toast.makeText(context, "Download is started!", Toast.LENGTH_SHORT).show()
            CoroutineScope(Dispatchers.IO).launch {
                downloader.downloadFile(MAIN_URL)
            }
        }
    }

    private fun fileIsDownloaded(){
        viewState.downloaded(isDownloaded = true)
    }

    fun installFileViaPackageInstaller(){
        val filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + FILE_NAME
        CoroutineScope(Dispatchers.IO).launch{
            packageInstallerVersion.packageInstallerDownloader(apkUri = Uri.parse(filePath), context = context)
        }
    }

    fun installFileViaIntent(){
        intentInstallerVersion.intentInstallation()
    }

//    private val downloadBroadcastReceiver = object : BroadcastReceiver(){
//        override fun onReceive(context: Context, intent: Intent) {
//            fileIsDownloaded()
//            context.unregisterReceiver(this)
//        }
//    }
}