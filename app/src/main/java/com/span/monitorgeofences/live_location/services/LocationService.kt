package com.span.monitorgeofences.live_location.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.span.monitorgeofences.live_location.receivers.RestartBackgroundService
import java.util.*

class LocationService : Service() {

    private lateinit var context: Context

    private val notificationChannelId = "com.get_background_live_location"

    private val channelName = "Background Live Location"

    override fun onCreate() {

        super.onCreate()

        context = this

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startTimer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChanel()
        else startForeground(1, Notification())

        requestLocationUpdates()

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {

        val notificationChannel = NotificationChannel(
            notificationChannelId,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        ).apply {
            lightColor = Color.BLUE
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

        manager.createNotificationChannel(notificationChannel)

        val notificationBuilder =
            NotificationCompat.Builder(this, notificationChannelId)

        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running count::$counter")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(2, notification)

    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimerTask()
        val broadcastIntent = Intent()
        broadcastIntent.action = "RestartService"
        broadcastIntent.setClass(this, RestartBackgroundService::class.java)
        sendBroadcast(broadcastIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestLocationUpdates() {

        val request = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val client: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission == PackageManager.PERMISSION_GRANTED) { // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location? = locationResult.lastLocation
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        Log.d("LocationUpdates ", "${location.latitude}, ${location.longitude}")
                    }
                }
            }, Looper.myLooper())
        }
    }

    var counter = 0

    var latitude: Double = 0.0

    var longitude: Double = 0.0

    private var timer: Timer? = null

    private var timerTask: TimerTask? = null

    private fun startTimer() {

        timer = Timer()

        timerTask = object : TimerTask() {

            override fun run() {

                Thread {

                    val count = counter++

                    if (latitude != 0.0 && longitude != 0.0) {

                        val liveLocation = "$latitude, $longitude \nCount: $count"

                        Log.d("Location ", liveLocation)

                        val liveIntent = Intent("LiveLocationUpdates").apply {
                            putExtra("liveLocation", liveLocation)
                        }

                        LocalBroadcastManager.getInstance(context).sendBroadcast(liveIntent)

                    }

                }.start()

            }

        }

        timer?.schedule(timerTask, 0, 1000) // 1 * 60 * 1000 1 minute

    }

    private fun stopTimerTask() {

        timer?.apply {

            cancel()

            timer = null

        }

    }

}