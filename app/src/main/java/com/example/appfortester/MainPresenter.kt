package com.example.appfortester

import android.content.Context
import android.os.Environment
import android.util.Log
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

    fun downloadFile(typeOfInstall: Int) {
        val filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME
        val file = File(filePath)
        if (file.exists()) {
            Log.d("info", " download - file exists, start installation")

            CoroutineScope(Dispatchers.Main).launch {
                downloader.startInstallation(typeOfInstall)
            }
        } else {
            Log.d("info", "download - Start downloading")
            CoroutineScope(Dispatchers.IO).launch {
                downloader.downloadFile(MAIN_URL, typeOfInstall)
            }
        }
    }

    private fun fileIsDownloaded(downloaded: Boolean){
        viewState.downloaded(isDownloaded = downloaded)
    }
}