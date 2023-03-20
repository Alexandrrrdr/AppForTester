package com.example.appfortester.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.widget.Toast

class PackageInstallReceiver(): BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val status: Int = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val message: String =
            intent.getIntExtra(PackageInstaller.EXTRA_STATUS_MESSAGE, -1).toString()

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmIntent = intent.getStringExtra(Intent.EXTRA_INTENT) as Intent
                context.startActivity(confirmIntent)
                return
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Toast.makeText(context, "Install succeeded", Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                Toast.makeText(context, "Install failed! " + status + ", " + message, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}