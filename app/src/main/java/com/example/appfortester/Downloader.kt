package com.example.appfortester

import android.app.DownloadManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.appfortester.broadcasts.PackageInstallReceiver
import com.example.appfortester.utils.Constants.APP_INSTALL_PATH
import com.example.appfortester.utils.Constants.FILE_BASE_PATH
import com.example.appfortester.utils.Constants.FILE_NAME
import com.example.appfortester.utils.Constants.MIME_TYPE
import com.example.appfortester.utils.Constants.PROVIDER_PATH
import java.io.File


class Downloader(
    private val context: Context
) {

    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    private var downloadedFileId = 0L

    //downloader
    suspend fun downloadFile(linkUrl: String) {
        val networkAddress = Uri.parse(linkUrl)
        val destinationUri =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/" + FILE_NAME
        val uri = Uri.parse("$FILE_BASE_PATH$destinationUri")
        val request = DownloadManager.Request(networkAddress)
            .setTitle(FILE_NAME)
            .setMimeType(MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(uri)
        downloadedFileId = downloadManager.enqueue(request)
    }

//    fun installation(){
//
//        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val file = File(filePath, FILE_NAME)
//        val uriFromFile = Uri.fromFile(file)
//        Log.d("info", "installPackage - $uriFromFile")
//        CoroutineScope(Dispatchers.Main).launch {
//            installPackage(apkUri = uriFromFile)
//        }
//    }
//
//    private suspend fun installPackage(apkUri: Uri){
//        val installer = context.packageManager.packageInstaller
//        val resolver = context.contentResolver
//        withContext(Dispatchers.IO){
//            resolver.openInputStream(apkUri)?.use { apkStream ->
//
//                val length =
//                    DocumentFile.fromSingleUri(context, apkUri)?.length() ?: -1
////                var length = 0L
////                val file = File("$apkUri")
////                //TODO if problems change to isExists
////                Log.d("info", "installPackage - $apkUri")
////                if (file.exists()) {
////                    length = file.length()
////                }
//
////                Log.d("info", "installPackage - $length")
//                val params =
//                    PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
//                val sessionId = installer.createSession(params)
//                val session = installer.openSession(sessionId)
//
//                session.openWrite(PACKAGE_NAME, 0, length).use { sessionStream ->
//                    apkStream.copyTo(sessionStream)
//                    session.fsync(sessionStream)
//                }
//                val intent = Intent(context, PackageInstallReceiver::class.java)
//                val pi = PendingIntent.getBroadcast(
//                    context,
//                    PACKAGE_INSTALL,
//                    intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT
//                )
//                session.commit(pi.intentSender)
//                session.close()
//            }
//        }
//    }

//    fun installPackageVersion() {
//        val destinationUri =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val uri = Uri.parse("$FILE_BASE_PATH$destinationUri")
//        val packageName = PACKAGE_NAME
////        val inputAltStream: InputStream = context.assets.open(FILE_NAME)
////        val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
//        val inputStream = File(destinationUri, FILE_NAME).inputStream()
//
//        installPackage(inputStream)
//    }

//    @SuppressLint("UnspecifiedImmutableFlag")
//    private fun installPackage(inputStream: InputStream){
//        val packageInstaller = context.packageManager.packageInstaller
//        val sessionId = packageInstaller.createSession(PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL))
//        val session = packageInstaller.openSession(sessionId)
//
//        val sizeBytes: Long = 0L
//        var outputStream: OutputStream? = null
//
//        outputStream = session.openWrite("my_app_session", 0, sizeBytes)
//        val buffer = ByteArray(65536)
//        var total = 0
//        var c: Int
//
//        while (inputStream.read(buffer) != -1) {
//            c = inputStream.read(buffer)
//            total +=c
//            outputStream.write(buffer, 0, c)
//        }
//
//        session.fsync(outputStream)
//        inputStream.close()
//        outputStream.close()
//
//        // fake intent
//        val statusReceiver: IntentSender? = null
//        val intent = Intent(context, PackageInstallReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        session.commit(pendingIntent.intentSender)
//        session.close()
//    }

    //2nd variant of package installer
//    fun installPackageLastVersion(){
//        val destinationUri =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                .toString() + "/" + FILE_NAME
//        val uri = Uri.parse("$FILE_BASE_PATH$destinationUri")
//        val packageInstaller = context.packageManager.packageInstaller
//        val params =
//            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
//        params.setAppPackageName(Constants.PACKAGE_NAME)
//        val sessionId = packageInstaller.createSession(params)
//        val session = packageInstaller.openSession(sessionId)
//
//        val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
//        val outputStream: OutputStream = session.openWrite("package", 0, -1)
//
//        val byte = ByteArray(65536)
//        var n = 0
//        while (inputStream.read(byte) != -1) {
//            n = inputStream.read(byte)
//            outputStream.write(byte, 0, n)
//        }
//        session.fsync(outputStream)
//        outputStream.close()
//        inputStream.close()
//
//        val intent = Intent(context, PackageInstallReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, 0)
//        val statusReceiver = pendingIntent.intentSender
//
//        session.commit(statusReceiver)
//    }

    //intent installer work version
    fun installViaIntentMethod() {
        val destination =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/" + FILE_NAME
        val uri = Uri.parse("$FILE_BASE_PATH$destination")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val contentUri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + PROVIDER_PATH,
                File(destination)
            )
            val install = Intent(Intent.ACTION_VIEW)
            install.setDataAndType(contentUri, MIME_TYPE)
            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            try {
                context.startActivity(install)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace();
                Log.d("info", "Error in opening the file!");
            }
        } else {
            val install = Intent(Intent.ACTION_VIEW)
            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            install.setDataAndType(
                uri,
                APP_INSTALL_PATH
            )
            context.startActivity(install)
        }
    }


    //1 st variant of packageInstaller
//    val packageInstalledAction =
//        "com.example.testappinstaller.data.repository.SESSION_API_PACKAGE_INSTALLED"
//
//    private fun addApkInstallSession(session: PackageInstaller.Session) {
//        val destinationUri =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                .toString() + "/" + FILE_NAME
//        val uri = Uri.parse("$FILE_BASE_PATH$destinationUri")
//        try {
//            val packageInSession: OutputStream = session.openWrite("package", 0, -1)
//            val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
//            val byte = ByteArray(65536)
//            var n = 0
//            while (inputStream.read(byte) != -1) {
//                n = inputStream.read(byte)
//                packageInSession.write(byte, 0, n)
//            }
//            inputStream.close()
//        } catch (e: IOException){
//            Log.d("info", "addApkInstallSession IOException")
//        }
//    }
}


