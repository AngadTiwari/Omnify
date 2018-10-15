package com.angad.omnify.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * @author Angad Tiwari
 * @msg firebase cloud messaging push, currently not in use
 */
class FirebaseMessagingHandler: FirebaseMessagingService() {
    val tag: String? = FirebaseMessagingHandler::class.java.simpleName;

    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)
        Log.d(tag, "new push received")
    }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        Log.d(tag, "new push received")
    }
}