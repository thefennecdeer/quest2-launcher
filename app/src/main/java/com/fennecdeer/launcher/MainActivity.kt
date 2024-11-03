package com.fennecdeer.launcher

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.fennecdeer.launcher.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

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