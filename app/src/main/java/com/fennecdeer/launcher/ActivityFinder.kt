package com.fennecdeer.launcher

import android.app.ActivityManager
import android.content.Context

class ActivityFinder {
    object Helper {
        fun isAppRunning(context: Context, packageName: String): Boolean {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val procInfos = activityManager.runningAppProcesses
            if (procInfos != null) {
                for (processInfo in procInfos) {
                    if (processInfo.processName == packageName) {
                        return true
                    }
                }
            }
            return false
        }
    }
}