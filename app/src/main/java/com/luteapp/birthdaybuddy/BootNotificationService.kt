package com.luteapp.birthdaybuddy

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import android.util.Log
import com.luteapp.birthdaybuddy.handler.EventHandler
import com.luteapp.birthdaybuddy.handler.IOHandler
import com.luteapp.birthdaybuddy.handler.NotificationHandler

class BootNotificationService : JobIntentService() {
    private val JOB_ID = 602

    fun addWork(context: Context, work: Intent) {
        enqueueWork(context, BootNotificationService::class.java, JOB_ID, work)
        Log.i("BootNotificationService", "add Work called")
    }

    override fun onHandleWork(intent: Intent) {
        IOHandler.registerIO(this)
        IOHandler.readAll(this)
        NotificationHandler.scheduleListEventNotifications(this, EventHandler.getList())
        Log.i("BootNotificationService", "notifications added")
    }
}