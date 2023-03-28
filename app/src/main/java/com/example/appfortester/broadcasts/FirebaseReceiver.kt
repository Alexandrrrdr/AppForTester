package com.example.appfortester.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.appfortester.notification.MyFirebaseMessagingService

class FirebaseReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras
        extras?.keySet()?.firstOrNull() {it == MyFirebaseMessagingService.KEY_ACTION }?.let {key ->
            Log.d("info", "firebaseReceiver - $key")

            when(extras.getString(key)){
                MyFirebaseMessagingService.ACTION_SHOW_MESSAGE -> {
                    Log.d("info", "firebaseReceiver - ${extras.getString(key)}")
                    extras.getString(MyFirebaseMessagingService.KEY_MESSAGE)?.let {message ->

                        Log.d("info", "firebaseReceiver - $message")
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    Log.d("info", "Something went wrong")}
            }
        }
    }

}