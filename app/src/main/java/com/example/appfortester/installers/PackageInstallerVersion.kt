package com.example.appfortester.installers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Environment
import com.example.appfortester.broadcasts.PackageInstallReceiver
import com.example.appfortester.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PackageInstallerVersion(private val context: Context) {

    private val packageInstalledAction =
        "com.example.testappinstaller.data.repository.SESSION_API_PACKAGE_INSTALLED"

    companion object {
        private const val PACKAGE = "package"
    }

    suspend fun packageInstallerDownloader(apkUri: Uri, context: Context) {
        withContext(Dispatchers.IO){
            val installer = context.packageManager.packageInstaller
            val resolver = context.contentResolver
            resolver.openInputStream(apkUri)?.use { apkStream ->
                val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"+ Constants.FILE_NAME
                val file = File(path)
                var session: PackageInstaller.Session? = null

                val params: PackageInstaller.SessionParams = PackageInstaller
                    .SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                val sessionId: Int = installer.createSession(params)
                session = installer.openSession(sessionId)

                session.openWrite(PACKAGE, 0, -1).use { packageInSession ->
                    apkStream.copyTo(packageInSession)
                    session.fsync(packageInSession)
                }

                val intent = Intent(context, PackageInstallReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context,
                    Constants.REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                session.commit(pendingIntent.intentSender)
                session.close()
            }
        }
    }
}