package com.example.appfortester

import android.content.Context
import android.util.Log
import com.example.appfortester.installers.PackageInstallerUninstall
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
    private val context: Context,
    private val libreDownloader: LibreDownloader,
    private val packageInstallerUninstall: PackageInstallerUninstall
) : MvpPresenter<MainView>() {

    fun downloadFile() {
        val file = File(context.cacheDir, FILE_NAME)
        if (file.exists()) {
            Log.d("info", " download - file exists, start installation")
            CoroutineScope(Dispatchers.Main).launch {
                libreDownloader.startInstallation()

            }
        } else {
            Log.d("info", "download - Start downloading")
            CoroutineScope(Dispatchers.Main).launch {
//                downloader.downloadFile(MAIN_URL)
                libreDownloader.secondLibreDownloading()
            }
        }
    }
    fun uninstallApplication(){
        CoroutineScope(Dispatchers.Main).launch {
            packageInstallerUninstall.uninstallApplication()
        }
    }
}