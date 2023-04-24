package com.example.appfortester

import android.content.Context
import android.util.Log
import com.example.appfortester.installers.PackageInstallerVersion
import com.example.appfortester.utils.Constants.FILE_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moxy.InjectViewState
import moxy.MvpPresenter
import java.io.File


@InjectViewState
class MainPresenter(
    private val downloader: LibreDownloader,
    private val context: Context,
    private val packageInstaller: PackageInstallerVersion
) : MvpPresenter<MainView>() {

    fun downloadFile() {
//        val cachPath = context.applicationContext.cacheDir
        val cachePath = File(context.applicationContext.cacheDir.absolutePath, FILE_NAME)
        if (cachePath.exists()) {
            Log.d("info", " download - file exists, start installation")
            CoroutineScope(Dispatchers.Main).launch {
                packageInstaller.startInstallApp()
            }
        } else {
            Log.d("info", "download - Start downloading")
            CoroutineScope(Dispatchers.Main).launch {
                downloader.startDownload()
            }
        }
    }
}