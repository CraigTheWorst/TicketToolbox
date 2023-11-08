package com.example.tickettoolbox.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class dashboardBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "update_count") {
            // Your code here to handle the broadcast
            // For example, you can update the UI of your activity with new data

        }
    }
}