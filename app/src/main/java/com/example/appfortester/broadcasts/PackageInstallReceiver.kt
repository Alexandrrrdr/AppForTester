package com.example.appfortester.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import android.widget.Toast
import com.example.appfortester.utils.Constants.PACKAGE_INSTALLED_ACTION

class PackageInstallReceiver(): BroadcastReceiver() {

    //    override fun onReceive(context: Context, intent: Intent) {
//        val status: Int = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
//        val message: String = intent.getIntExtra(PackageInstaller.EXTRA_STATUS_MESSAGE, -1).toString()
//
//        when (status) {
//            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
//                val confirmIntent = intent.getStringExtra(Intent.EXTRA_INTENT) as Intent
//                context.startActivity(confirmIntent)
//                return
//            }
//            PackageInstaller.STATUS_SUCCESS -> {
//                Log.d("info", "PackageInstallReceiver - app is installed, message - $message!")
//                return
//            }
//            else -> {
//                Log.d("info", "PackageInstallReceiver - install failed..., message - $message")
//            }
//        }
//    }
    override fun onReceive(context: Context?, intent: Intent?) {
        val extras = intent?.extras
        if (PACKAGE_INSTALLED_ACTION == intent?.action) {
            val status = extras!!.getInt(android.content.pm.PackageInstaller.EXTRA_STATUS)
            val message = extras.getString(android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE)
            when (status) {
                android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    // This test app isn't privileged, so the user has to confirm the install.
//                    val confirmIntent = extras[Intent.EXTRA_INTENT] as Intent?
                    val confirmIntent = intent.getStringExtra(Intent.EXTRA_INTENT) as Intent
                    context?.startActivity(confirmIntent)
                }
                android.content.pm.PackageInstaller.STATUS_SUCCESS -> Toast.makeText(
                    context,
                    "Install succeeded!",
                    Toast.LENGTH_SHORT
                ).show()
                android.content.pm.PackageInstaller.STATUS_FAILURE,
                android.content.pm.PackageInstaller.STATUS_FAILURE_ABORTED,
                android.content.pm.PackageInstaller.STATUS_FAILURE_BLOCKED,
                android.content.pm.PackageInstaller.STATUS_FAILURE_CONFLICT,
                android.content.pm.PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
                android.content.pm.PackageInstaller.STATUS_FAILURE_INVALID,
                android.content.pm.PackageInstaller.STATUS_FAILURE_STORAGE -> Toast.makeText(
                    context,
                    "Install failed! $status, $message",
                    Toast.LENGTH_SHORT
                ).show()
                else -> Toast.makeText(
                    context, "Unrecognized status received from installer: $status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}