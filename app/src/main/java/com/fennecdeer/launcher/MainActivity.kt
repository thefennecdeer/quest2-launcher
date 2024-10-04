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



class MainActivity : AppCompatActivity() {

    private lateinit var mAdminComponentName: ComponentName
    private lateinit var mDevicePolicyManager: DevicePolicyManager
    private lateinit var binding: ActivityMainBinding

    private val KIOSK_PACKAGE = "com.wearecypha.vrroadtripupdate"
    private val APP_PACKAGES = arrayOf(KIOSK_PACKAGE)

    private val handlerThread = HandlerThread("MyHandlerThread")
    lateinit var handler: Handler
    lateinit var runnable: Runnable
    private val threadDelay: Long = 5000

    private val TAG = MainActivity::class.java.simpleName

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG,"aaa!")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAdminComponentName = LauncherDeviceAdminReceiver.getComponentName(this)
        mDevicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager

        //mDevicePolicyManager.removeActiveAdmin(mAdminComponentName)
        Log.i(TAG,"aaa!")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        runnable = Runnable {
            if (ActivityFinder.Helper.isAppRunning(applicationContext, KIOSK_PACKAGE)) {
                Log.i(TAG,"yes!")
            } else {
                Log.i(TAG, "nope!")
                //launchPackage()
            }
            handler.postDelayed(runnable, threadDelay)
        }

        val isAdmin = isAdmin()
        if (isAdmin) {
            Snackbar.make(binding.content, R.string.device_owner, Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.content, R.string.not_device_owner, Snackbar.LENGTH_SHORT).show()
        }
        binding.btStartLockTask.setOnClickListener {
            setKioskPolicies(true, isAdmin)
        }
        binding.btStopLockTask.setOnClickListener {
            setKioskPolicies(false, isAdmin)
        }
        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, APP_PACKAGES)


        // Start our kiosk app's main activity with our lock task mode option.
        launchPackage()

    }

    override fun onResume() {
        super.onResume()
        launchPackage()
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quit()
    }

     fun removeCallbacks(boolean: Boolean) {
        if (boolean) {
            handler.removeCallbacks(runnable)
        }
        else {
            handler.postDelayed(runnable, threadDelay)
        }
    }

    private fun isAdmin() = mDevicePolicyManager.isDeviceOwnerApp(packageName)

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
    private fun setKioskPolicies(enable: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            setRestrictions(enable)
            enableStayOnWhilePluggedIn(enable)
            setUpdatePolicy(enable)
            setAsHomeApp(enable)
        }
        setLockTask(enable, isAdmin)
        setImmersiveMode(enable)
    }

    // region restrictions
    private fun setRestrictions(disallow: Boolean) {
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, disallow)
        mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, disallow)
    }

    private fun setUserRestriction(restriction: String, disallow: Boolean) = if (disallow) {
        mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction)
    } else {
        mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction)
    }
    // endregion

    private fun enableStayOnWhilePluggedIn(active: Boolean) = if (active) {
        mDevicePolicyManager.setGlobalSetting(
            mAdminComponentName,
            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            (BatteryManager.BATTERY_PLUGGED_AC
                    or BatteryManager.BATTERY_PLUGGED_USB
                    or BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
        )
    } else {
        mDevicePolicyManager.setGlobalSetting(
            mAdminComponentName,
            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            "0"
        )
    }

    private fun setLockTask(start: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            mDevicePolicyManager.setLockTaskPackages(
                mAdminComponentName, if (start) arrayOf(packageName) else arrayOf()
            )
        }
        if (start) {
            startLockTask()
        } else {
            stopLockTask()
        }
    }

    private fun setUpdatePolicy(enable: Boolean) {
        if (enable) {
            mDevicePolicyManager.setSystemUpdatePolicy(
                mAdminComponentName,
                SystemUpdatePolicy.createWindowedInstallPolicy(60, 120)
            )
        } else {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null)
        }
    }

    private fun setAsHomeApp(enable: Boolean) {
        if (enable) {
            val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            mDevicePolicyManager.addPersistentPreferredActivity(
                mAdminComponentName,
                intentFilter,
                ComponentName(packageName, MainActivity::class.java.name)
            )
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(
                mAdminComponentName, packageName
            )
        }
    }


    @Suppress("DEPRECATION")
    private fun setImmersiveMode(enable: Boolean) {
        if (enable) {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.decorView.systemUiVisibility = flags
        } else {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.decorView.systemUiVisibility = flags
        }
    }

    private fun createIntentSender(
        context: Context?,
        sessionId: Int,
        packageName: String?
    ): IntentSender {
        val intent = Intent("INSTALL_COMPLETE")
        if (packageName != null) {
            intent.putExtra("PACKAGE_NAME", packageName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            intent,
            FLAG_IMMUTABLE
        )
        return pendingIntent.intentSender
    }
}