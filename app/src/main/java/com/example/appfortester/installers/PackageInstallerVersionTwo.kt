package com.example.appfortester.installers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.appfortester.utils.Constants.FILE_NAME
import com.example.appfortester.utils.Constants.PACKAGE_INSTALLED_ACTION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*


class PackageInstallerVersionTwo(private val context: Context) {

    @SuppressLint("UnspecifiedImmutableFlag")
    suspend fun packageInstall(path: String) {
        withContext(Dispatchers.IO) {

            var session: android.content.pm.PackageInstaller.Session? = null
            try {
                val packageInstaller: android.content.pm.PackageInstaller =
                    context.packageManager.packageInstaller

                val params = android.content.pm.PackageInstaller.SessionParams(
                    android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL
                )
                val sessionId: Int = packageInstaller.createSession(params)
                session = packageInstaller.openSession(sessionId)

                val fileLocation = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + "/" + FILE_NAME
                var fileLength = 0L
                val file = File(fileLocation)
                if (file.isFile) {
                    fileLength = file.length()
                } else {
                    Log.d("info", "File is not existing... How it's working?!")
                }
                var inputStr: InputStream? = null
                var outputStr: OutputStream? = null

                inputStr = context.contentResolver.openInputStream(Uri.fromFile(file))!!

                outputStr = session.openWrite("my_app_session", 0, fileLength)

                var totalSize = 0
                var c: Int
                val buffer = ByteArray(65536)

                while ((inputStr.read(buffer).also { c = it }) >= 0) {
                    totalSize += c
                    outputStr.write(buffer, 0, c)
                }

                session.fsync(outputStr)
                inputStr.close()
                outputStr.close()

                Log.d(
                    "info",
                    "InstallApkViaPackageInstaller - Success: streamed apk + $totalSize + bytes"
                )

                //TODO Unable to start receiver

                //version 2

                // Create an install status receiver.

                val intent = Intent(context, PackageInstallerVersionTwo::class.java)
                intent.action = PACKAGE_INSTALLED_ACTION
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                val statusReceiver = pendingIntent.intentSender

                // Commit the session (this will start the installation workflow).

                // Commit the session (this will start the installation workflow).
                session.commit(statusReceiver)

                //version 1
//                val intent = Intent()
//                intent.action = PACKAGE_INSTALLED_ACTION
//                intent.putExtra(PACKAGE_INSTALLED_ACTION, "Message from PackageInstaller")
//                val pendingIntent =
//                    PendingIntent.getBroadcast(
//                        context,
//                        sessionId,
//                        intent,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                    )
//                context.sendBroadcast(intent)
//                val statusReceiver = pendingIntent.intentSender
////                 Commit the session (this will start the installation workflow).
//                session.commit(statusReceiver)
//                session.commit(PendingIntent.getBroadcast(context, sessionId, intent, PendingIntent.FLAG_UPDATE_CURRENT).intentSender)
//                session.close()
            } catch (e: IOException) {
                Log.d("info", "IOException - ${e.message}")
                Log.d("info", "Path - ${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME}")
                throw RuntimeException("Couldn't install package", e)
            } catch (e: RuntimeException) {
                session?.abandon()
                throw e
            }
        }
    }
}


//    private fun addApkToInstallSession(session: android.content.pm.PackageInstaller.Session?) {
//        // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
//        // if the disk is almost full.
//        session?.openWrite("package", 0, -1).use { packageInSession ->
//            context.assets.open(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME).use { inputStr ->
//                val buffer = ByteArray(65536)
//                var n: Int
//                while (inputStr.read(buffer).also { n = it } >= 0) {
//                    packageInSession?.write(buffer, 0, n)
//                }
//            }
//        }
//    }
//
//    // Note: this Activity must run in singleTop launchMode for it to be able to receive the intent
//    // in onNewIntent().
//    private fun onNewIntent(intent: Intent) {
//        val extras = intent.extras
//        if (PACKAGE_INSTALLED_ACTION == intent.action) {
//            val status = extras!!.getInt(android.content.pm.PackageInstaller.EXTRA_STATUS)
//            val message = extras.getString(android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE)
//            when (status) {
//                android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION -> {
//                    // This test app isn't privileged, so the user has to confirm the install.
////                    val confirmIntent = extras[Intent.EXTRA_INTENT] as Intent?
//                    val confirmIntent = intent.getStringExtra(Intent.EXTRA_INTENT) as Intent
//                    context.startActivity(confirmIntent)
//                }
//                android.content.pm.PackageInstaller.STATUS_SUCCESS -> Toast.makeText(
//                    context,
//                    "Install succeeded!",
//                    Toast.LENGTH_SHORT
//                ).show()
//                android.content.pm.PackageInstaller.STATUS_FAILURE,
//                android.content.pm.PackageInstaller.STATUS_FAILURE_ABORTED,
//                android.content.pm.PackageInstaller.STATUS_FAILURE_BLOCKED,
//                android.content.pm.PackageInstaller.STATUS_FAILURE_CONFLICT,
//                android.content.pm.PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
//                android.content.pm.PackageInstaller.STATUS_FAILURE_INVALID,
//                android.content.pm.PackageInstaller.STATUS_FAILURE_STORAGE -> Toast.makeText(
//                    context,
//                    "Install failed! $status, $message",
//                    Toast.LENGTH_SHORT
//                ).show()
//                else -> Toast.makeText(
//                    context, "Unrecognized status received from installer: $status",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }

//}