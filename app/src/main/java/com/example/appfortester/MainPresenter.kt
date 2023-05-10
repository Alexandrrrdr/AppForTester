package com.example.appfortester

import android.content.Context
import android.util.Log
import com.example.appfortester.installers.PackageInstallerUninstall
import com.example.appfortester.utils.Constants.FILE_NAME
import com.example.appfortester.utils.Constants.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.InjectViewState
import moxy.MvpPresenter
import java.io.File


@InjectViewState
class MainPresenter(
    private val context: Context,
    private val libreDownloader: LibreDownloader,
    private val packageInstallerUninstall: PackageInstallerUninstall
) : MvpPresenter<MainView>() {

    fun downloadFile() {
        val file = File(context.cacheDir, FILE_NAME)
        var isDownloadedFile = false
        if (file.exists()) {
            Log.d(TAG, " download - file exists, start installation")
            CoroutineScope(Dispatchers.IO).launch {
                libreDownloader.startInstallation()

            }
        } else {
            Log.d(TAG, "download - Start downloading")
            CoroutineScope(Dispatchers.Main).launch {
                isDownloadedFile = libreDownloader.secondLibreDownloading()
                if (isDownloadedFile){
                    libreDownloader.startInstallation()
                } else {
                        Log.d("info", "download - in progress")
                }
            }
        }
    }
    fun uninstallApplication(){
        CoroutineScope(Dispatchers.Main).launch {
            packageInstallerUninstall.uninstallApplication()
        }
    }
}