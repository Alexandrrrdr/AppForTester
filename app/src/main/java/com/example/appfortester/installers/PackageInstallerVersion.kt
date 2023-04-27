package com.example.appfortester.installers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.appfortester.broadcasts.PackageInstallReceiver
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Constants.FILE_NAME
import com.example.appfortester.utils.Constants.REQUEST_CODE
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

                    val myBroadcastReceiver = object : BroadcastReceiver(){
                        override fun onReceive(context: Context, intent: Intent) {
                            when(val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)){
                                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                                    val activityIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                                    if (activityIntent != null) {
                                        context.startActivity(activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                    }
                                }
                                PackageInstaller.STATUS_SUCCESS -> {
                                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                                    Log.d("info", "$status,$msg")
                                }
                            }
                        }
                    }

                    val intent = Intent(context.applicationContext, PackageInstallReceiver::class.java)
                    //From Android S FLAG_MUTABLE or FLAG_IMMUTABLE must be used
                    //but immutable doesn't start the installation.
                    val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.getBroadcast(
                            context.applicationContext,
                            REQUEST_CODE,
                            intent,
                            PendingIntent.FLAG_MUTABLE
                        )
                    } else {
                        PendingIntent.getBroadcast(
                            context.applicationContext,
                            REQUEST_CODE,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }

                    session!!.commit(pendingIntent.intentSender)
                    session!!.close()
                }
            }
        }
    }