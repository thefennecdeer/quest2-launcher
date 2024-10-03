package com.fennecdeer.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fennecdeer.launcher.MainActivity

class LauncherReceiver : BroadcastReceiver() {
    private val TAG = LauncherDeviceAdminReceiver::class.java.simpleName
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "YEA")
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val activityIntent = Intent(context, MainActivity::class.java)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(activityIntent)
        }


    }
}
