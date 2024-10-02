package com.fennecdeer.launcher

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class LauncherDeviceAdminReceiver : DeviceAdminReceiver() {
    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, LauncherDeviceAdminReceiver::class.java)
        }

        private val TAG = LauncherDeviceAdminReceiver::class.java.simpleName
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        Log.d(TAG, "onLockTaskModeEntering")
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        Log.d(TAG, "onLockTaskModeExiting")
    }
}