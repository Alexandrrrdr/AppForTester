package com.example.appfortester.installers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.example.appfortester.broadcasts.PackageInstallReceiver
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Constants.FILE_BASE_PATH
import com.example.appfortester.utils.Constants.FILE_NAME
import com.example.appfortester.utils.Constants.PACKAGE
import com.example.appfortester.utils.Constants.REQUEST_CODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream

class PackageInstallerVersion() {

    private val TAG = "APKInstall"


    fun packageInstallerDownloader(context: Context){
        val destinationUri = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME
        val uri = Uri.parse("$FILE_BASE_PATH$destinationUri")
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

//    suspend fun packageInstallerDownloader(context: Context) {
//        val destinationUri = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME
//        val file = Uri.fromFile(File(destinationUri))
//        val uri = Uri.parse("$FILE_BASE_PATH$destinationUri")
//        val installer = context.packageManager.packageInstaller
//        val resolver = context.contentResolver
//        withContext(Dispatchers.IO){
//            resolver.openInputStream(file)?.use { apkStream ->
////                val length = DocumentFile.fromSingleUri(context, file)?.length() ?: -1
//                val length = File(destinationUri).length()
//                val params = SessionParams(SessionParams.MODE_FULL_INSTALL)
//                val sessionId: Int = installer.createSession(params)
//                val session = installer.openSession(sessionId)
//
//                session.openWrite(FILE_NAME, 0, length).use { sessionStream ->
//                    apkStream.copyTo(sessionStream)
//                    session.fsync(sessionStream)
//                }
//
//                val intent = Intent(context, PackageInstallReceiver::class.java)
//                val pendingIntent = PendingIntent.getBroadcast(
//                    context,
//                    REQUEST_CODE,
//                    intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT
//                )
//
//
//                session.commit(pendingIntent.intentSender)
//                session.close()
//            }
//        }
//    }
}