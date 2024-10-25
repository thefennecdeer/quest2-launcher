package com.fennecdeer.launcher

import android.util.Log
import se.vidstige.jadb.JadbConnection
import se.vidstige.jadb.JadbDevice
import se.vidstige.jadb.JadbException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets


class ADBManager {
    private val TAG = ADBManager::class.java.simpleName

    private lateinit var device: JadbDevice
    private var devices = mutableListOf<JadbDevice>()
    fun initADB(): JadbDevice {
        var jadb: JadbConnection = JadbConnection("127.0.0.1", 5555)
        jadb.connectToTcpDevice(InetSocketAddress("127.0.0.1", 5555))
        println(jadb.hostVersion)
        println("aaae!!")
        devices = jadb.devices

        println("YEAFirstt?")
        println( devices.toString())

        var device = devices[0]
        return device
    }

    fun pauseGuardian(device: JadbDevice) {
        this.device = device
        println("yeas")
        try {
            var input = BufferedReader( InputStreamReader(device.executeShell("setprop", "debug.oculus.guardian_pause", "1"), StandardCharsets.UTF_8))
            var line: String? = null
            while ((input.readLine().also { line = it }) != null) println(
                line!!
            )

        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: JadbException) {
            throw RuntimeException(e)
        }

    }
}