package com.example

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class NotificationReaderService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val packageName = it.packageName
            val extras = it.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            // We need a way to get the userName. For now, we can use a SharedPreferences approach or hardcode/save globally.
            // Let's save userName in SharedPreferences in MainActivity.
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val userName = prefs.getString("USER_NAME", "unknown") ?: "unknown"

            if (title.isNotEmpty() || text.isNotEmpty()) {
                val db = FirebaseFirestore.getInstance()
                val notificationData = hashMapOf(
                    "package" to packageName,
                    "title" to title,
                    "text" to text,
                    "timestamp" to System.currentTimeMillis()
                )
                
                // Add to a subcollection for the user
                db.collection("notifications").document(userName)
                    .collection("logs").add(notificationData)
                    .addOnSuccessListener {
                        Log.d("NotificationReader", "Notification saved to Firebase")
                    }
                    .addOnFailureListener { e ->
                        Log.e("NotificationReader", "Error saving notification", e)
                    }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Can be handled if needed
    }
}
