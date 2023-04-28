package com.example.appfortester.installers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Constants.TEST_PACKAGE_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class PackageInstallerUninstall(private val context: Context){

    suspend fun uninstallApplication(){
        if (isPackageInstalled(TEST_PACKAGE_NAME)){
        withContext(Dispatchers.IO) {
                val installer: PackageInstaller = context.applicationContext.packageManager.packageInstaller

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

                IntentFilter(PackageInstaller.EXTRA_STATUS).also {
                    context.registerReceiver(myBroadcastReceiver, it)
                }

                val intent = Intent(PackageInstaller.EXTRA_STATUS)
                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getBroadcast(
                        context.applicationContext,
                        Constants.REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_MUTABLE
                    )
                } else {
                    PendingIntent.getBroadcast(
                        context.applicationContext,
                        Constants.REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
                installer.uninstall(TEST_PACKAGE_NAME, pendingIntent.intentSender)
            }
        } else {
            Toast.makeText(context, "Application not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        val packageManager = context.packageManager
        return try {
            packageManager.getPackageInfo(packageName, 0)
            Log.d("info", "$packageName is exists")
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("info", "$packageName doesn't exist")
            false
        }
    }
}
