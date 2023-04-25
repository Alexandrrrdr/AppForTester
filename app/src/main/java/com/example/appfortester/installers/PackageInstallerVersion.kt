package com.example.appfortester.installers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.appfortester.broadcasts.PackageInstallReceiver
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Constants.FILE_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PackageInstallerVersion(private val context: Context) {

    suspend fun install() {
        coroutineInstaller()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private suspend fun coroutineInstaller() {
            withContext(Dispatchers.IO) {
                val resolver: ContentResolver = context.applicationContext.contentResolver
                val installer: PackageInstaller =
                    context.applicationContext.packageManager.packageInstaller
                val fileLocation = File(context.cacheDir, FILE_NAME)
                var fileLength = 0L
//                val file = File(fileLocation)
                if (fileLocation.isFile) {
                    fileLength = fileLocation.length()
                } else {
                    Log.d("info", "File is not existing... How it's working?!")
                }
                var sessionId = 0
                var session: PackageInstaller.Session? = null

                resolver.openInputStream(Uri.fromFile(fileLocation)).use { apkStream ->
                    val params =
                        PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                    try {
                        sessionId = installer.createSession(params)
                    } catch (e: Exception) {
                        Log.d("info", "Couldn't create session")
                    }

                    try {
                        session = installer.openSession(sessionId)
                    } catch (e: Exception) {
                        Log.d("info", "Couldn't open session")
                        return@withContext
                    }

                    session!!.openWrite(Constants.PACKAGE, 0, fileLength).use { outputStream ->
                        apkStream?.copyTo(outputStream)
                        session!!.fsync(outputStream)
                    }

                    val intent =
                        Intent(context.applicationContext, PackageInstallReceiver::class.java)
                    //From Android S FLAG_MUTABLE or FLAG_IMMUTABLE must be used
                    //but immutable doesn't start the installation.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val pendingIntent = PendingIntent.getBroadcast(
                            context.applicationContext,
                            Constants.PI_INSTALLER,
                            intent,
                            PendingIntent.FLAG_MUTABLE
                        )
                        session!!.commit(pendingIntent.intentSender)
                        session!!.close()
                    } else {
                        val pendingIntent = PendingIntent.getBroadcast(
                            context.applicationContext,
                            Constants.PI_INSTALLER,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        session!!.commit(pendingIntent.intentSender)
                        session!!.close()
                    }
                }
            }
        }
    }