package com.example.appfortester.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import android.widget.Toast

class PackageInstallReceiver(): BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)){
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val activityIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (activityIntent != null) {
                    context.startActivity(activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                Toast.makeText(context, "Installed successfully", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Toast.makeText(context, "received $status, $msg", Toast.LENGTH_SHORT).show()
                Log.d("info", "receiver status - $status, message - $msg")
            }
        }
    }
}