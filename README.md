# Quest 2 Application Launcher
Launches an app on startup and ensures it stays open
- Requires setting the app as a Device Owner through ADB:

  `adb shell dpm set-device-owner com.fennecdeer.launcher/.LauncherDeviceAdminReceiver`

  May need to remove existing accounts: https://help.arborxr.com/en/articles/6333244-how-to-resolve-the-one-or-more-accounts-error-in-the-device-setup-app
