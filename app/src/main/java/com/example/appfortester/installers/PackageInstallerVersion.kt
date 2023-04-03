package com.example.appfortester.installers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Constants.FILE_NAME
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream

class PackageInstallerVersion() {

    private val TAG = "APKInstall"

    companion object {
        private const val PACKAGE = "package"
    }

    fun packageInstallerDownloader(context: Context){
        val destinationUri = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME
        val uri = Uri.parse("${Constants.FILE_BASE_PATH}$destinationUri")
        val file = File(destinationUri)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val intent = Intent(TAG).setPackage(context.packageName)
        val pending = PendingIntent.getBroadcast(context, 0, intent, flag)
        val installer = context.packageManager.packageInstaller
        val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            params.setRequireUserAction(SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        try  {
            val session = installer.openSession(installer.createSession(params))
            val outputStream: OutputStream = session.openWrite(file.name, 0, file.length())
            try {
                val inputStream = FileInputStream(file)
                transfer(inputStream, outputStream)
            } catch (e: Exception){
                Log.d("info", "[ackageInstaller - ${e.cause}")
            }
            session.commit(pending.intentSender)
        } catch (e: IOException){
            Log.d("info", "[ackageInstaller - ${e.cause}")
        }
    }

    private fun transfer(inputStream: FileInputStream, outputStream: OutputStream) {
        val size: Int = 8192
        val buffer: ByteArray = ByteArray(size)
        val read: Int = inputStream.read(buffer, 0, size)
        while (read>= 0){
            outputStream.write(buffer, 0, read)
        }
    }

//    suspend fun packageInstallerDownloader(apkUri: Uri, context: Context) {
//        val installer = context.packageManager.packageInstaller
//        val resolver = context.contentResolver
//        withContext(Dispatchers.IO){
//            resolver.openInputStream(apkUri)?.use { apkStream ->
//                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                val file = File(path, FILE_NAME)
//                var session: PackageInstaller.Session? = null
//
//                val params: PackageInstaller.SessionParams = PackageInstaller
//                    .SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
//                val sessionId: Int = installer.createSession(params)
//                session = installer.openSession(sessionId)
//
//                session.openWrite(PACKAGE, 0, -1).use { packageInSession ->
//                    apkStream.copyTo(packageInSession)
//                    session.fsync(packageInSession)
//                }
//
//                val intent = Intent(context, PackageInstallReceiver::class.java)
//                val pendingIntent = PendingIntent.getBroadcast(context,
//                    Constants.REQUEST_CODE,
//                    intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT
//                )
//
//                session.commit(pendingIntent.intentSender)
//                session.close()
//            }
//        }
//    }
}