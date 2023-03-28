package com.example.appfortester.notification

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {


    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val intent = Intent(INTENT_FILTER)
        remoteMessage.data.forEach{ entity ->
            intent.putExtra(entity.key, entity.value)
        }
        sendBroadcast(intent)
    }

    companion object{
        const val INTENT_FILTER = "com.example.appfortester.notification"
        const val KEY_ACTION = "key_action"
        const val KEY_MESSAGE = "key_message"
        const val ACTION_SHOW_MESSAGE = "show_message"
    }
}