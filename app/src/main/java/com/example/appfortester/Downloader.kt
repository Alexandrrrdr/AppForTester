package com.example.appfortester

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.appfortester.installers.IntentInstallerVersion
import com.example.appfortester.installers.PackageInstallerVersion
import com.example.appfortester.utils.Constants.FILE_BASE_PATH
import com.example.appfortester.utils.Constants.FILE_NAME
import com.example.appfortester.utils.Constants.MIME_TYPE
import kotlinx.coroutines.*
import java.io.File

class Downloader(
    private val context: Context, private val url: String,
    private val intentInstallerVersion: IntentInstallerVersion,
    private val packageInstallerVersion: PackageInstallerVersion
                 ) {

    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    private var downloadId = 0L

    @SuppressLint("Range")
    fun downloadFile(linkAddress: String, installationType: Int) {
        val networkAddress = Uri.parse(linkAddress)
        var destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += FILE_NAME
//        val destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                .toString() + "/" + FILE_NAME
        Log.d("info", "$destination")
        val uri = Uri.parse("$FILE_BASE_PATH$destination")
        val file = File(destination)
        if (file.exists()) {
//                startInstall(type = installationType, destination = destination, uri = uri)
            Toast.makeText(context, "File existed and was deleted", Toast.LENGTH_SHORT).show()

        } else {
            CoroutineScope(Dispatchers.Main).launch{
//                startInstall(type = installationType, destination = destination, uri = uri)
                downloadFile(networkAddress)
            }
        }
    }

    private fun startInstall(type: Int, destination: String, uri: Uri){
        when(type){
            1 -> {
                intentInstallerVersion.intentInstallation()
            }
            2 -> {
                intentInstallerVersion.intentInstallation()
            }
        }
    }

    private fun downloadFile(downloadUri: Uri) {
            val request = DownloadManager.Request(downloadUri)
                .setMimeType(MIME_TYPE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle(FILE_NAME)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, FILE_NAME)
//                .setDestinationUri(destinationUri)
        downloadManager.enqueue(request)
    }

//    fun enqueueDownload() {
////        var destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
//        var destination = Environment.getExternalStorageDirectory().toString() + "/"
//        destination += FILE_NAME
//        val uri = Uri.parse("$FILE_BASE_PATH$destination")
//        val file = File(destination)
//
//        Log.d("info", "$destination")
//        if (file.exists()){
//            Log.d("info", "file is exists")
//        } else {
//            Log.d("info", "file doesn't exist")
//        }
////        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//
//        val downloadUri = Uri.parse(url)
//        val request = DownloadManager.Request(downloadUri)
//        request.setMimeType(MIME_TYPE)
//        request.setTitle(context.getString(R.string.title_file_download))
//        request.setDescription(context.getString(R.string.downloading))
//        request.setDestinationUri(uri)
//        showInstallOption(destination, uri)
//
//        downloadManager.enqueue(request)
//        Toast.makeText(context, context.getString(R.string.downloading), Toast.LENGTH_LONG).show()
//    }
//    private fun showInstallOption(
//        destination: String,
//        uri: Uri
//    ) {
//        // set BroadcastReceiver to install app when .apk is downloaded
//        val onComplete = object : BroadcastReceiver() {
//            override fun onReceive(
//                context: Context,
//                intent: Intent
//            ) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    val contentUri = FileProvider.getUriForFile(
//                        context,
//                        com.example.appfortester.BuildConfig.APPLICATION_ID + PROVIDER_PATH,
//                        File(destination)
//                    )
//                    val install = Intent(Intent.ACTION_VIEW)
//                    install.setDataAndType(uriFromFile(context, File(destination)), MIME_TYPE)
//                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    install.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                    install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
//                    install.data = contentUri
//                    try {
//                        context.startActivity(install)
//                    } catch (e: ActivityNotFoundException){
//                        e.printStackTrace();
//                        Log.d("info", "Error in opening the file!");
//                    }
//                    context.unregisterReceiver(this)
//                    // finish()
//                } else {
//                    val install = Intent(Intent.ACTION_VIEW)
//                    install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                    install.setDataAndType(
//                        uri,
//                        APP_INSTALL_PATH
//                    )
//                    context.startActivity(install)
//                    context.unregisterReceiver(this)
//                    // finish()
//                }
//            }
//        }
//        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
//    }
//
//    fun uriFromFile(context: Context?, file: File?): Uri? {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            FileProvider.getUriForFile(
//                context!!, com.example.appfortester.BuildConfig.APPLICATION_ID + ".provider",
//                file!!
//            )
//        } else {
//            Uri.fromFile(file)
//        }
//    }
}