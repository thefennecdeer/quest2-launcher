package com.fennecdeer.launcher

import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.fennecdeer.launcher.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.JadbException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Timer
import kotlin.concurrent.timerTask


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var ADBManager: ADBManager

    private val KIOSK_PACKAGE = "com.wearecypha.vrroadtrip"


    private val TAG = MainActivity::class.java.simpleName




    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {

        Log.i(TAG,"aaa!")

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //mDevicePolicyManager.removeActiveAdmin(mAdminComponentName)
        Log.i(TAG,"aaa!")


        CoroutineScope(Dispatchers.IO).launch {
            ADBManager = ADBManager()
            val adbDevice = ADBManager.initADB()
            ADBManager.pauseGuardian(adbDevice)
        }


//        Timer().schedule(timerTask {
//            runShell("/system/bin/setprop debug.oculus.guardian_pause 1")
//            System.setProperty("debug.oculus.guardian_pause", "1")
//        }, 2000)
//
//        Timer().schedule(timerTask {
//            runShell("/system/bin/getprop debug.oculus_guardian.pause")
//        }, 2500)
//        Timer().schedule(timerTask {
//            runShell("/system/bin/getprop")
//        }, 2500)
        // Start our kiosk app's main activity with our lock task mode option.
        launchPackage()

    }

    override fun onResume() {
        super.onResume()
        launchPackage()
    }

    private fun launchPackage() {
        val packageManager = packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(KIOSK_PACKAGE)
        if (launchIntent != null) {
            launchIntent.putExtra("intent_cmd", "")
            launchIntent.putExtra("intent_package", "com.oculus.vrshell")
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchIntent.setAction("com.oculus.vrshell.intent.action.LAUNCH")

            startForResult.launch(launchIntent)
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK or Activity.RESULT_CANCELED) {
            Log.d(TAG, result.resultCode.toString())
            launchPackage()
        }
    }
}