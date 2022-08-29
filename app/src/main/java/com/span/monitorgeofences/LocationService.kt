package com.span.monitorgeofences

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.*

class LocationService : Service() {

    private val geoFencesId = "GEO_FENCES_ID"

    private val radiusPoint = 100.0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        super.onStartCommand(intent, flags, startId)

        startTimer()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChanel()
        else startForeground(1, Notification())

        geoLocationUpdates(intent)

        return START_STICKY

    }

    private fun geoLocationUpdates(intent: Intent?) {

        val latitude = intent?.extras?.getDouble("latitude") ?: 12.98887152580908
        val longitude = intent?.extras?.getDouble("longitude") ?: 80.25068689137697
        val latLng = LatLng(latitude, longitude)

        addGeoFence(latLng)

    }

    private fun addGeoFence(latLng: LatLng) {

        val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(this)

        val geoFencesHelper = GeoFencesHelper(this)

        val geofence = geoFencesHelper.getGeoFence(
            geoFencesId,
            latLng,
            radiusPoint.toFloat(),
            Geofence.GEOFENCE_TRANSITION_ENTER or
                    Geofence.GEOFENCE_TRANSITION_DWELL or
                    Geofence.GEOFENCE_TRANSITION_EXIT
        )

        val geofencingRequest = geoFencesHelper.getGeoFencingRequest(geofence)

        val pendingIntent = geoFencesHelper.getGeoFencesPendingIntent()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        if (pendingIntent != null) {

            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        Log.e("addOnCompleteListener", "Geofencing Successful")
                    } else {
                        val errorMessage =
                            task.exception?.let { geoFencesHelper.getErrorString(it) }
                        Log.e("Geofencing Failed: ", errorMessage!!)
                    }

                }

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {

        val NOTIFICATION_CHANNEL_ID = "com.getlocationbackground"

        val channelName = "Background Service"

        val chain = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )

        chain.lightColor = Color.BLUE

        chain.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)

        manager.createNotificationChannel(chain)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running count: $counter")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(2, notification)

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

                val count = counter++

                if (latitude != 0.0 && longitude != 0.0) {

                    Log.d("Location ", "$latitude,$longitude Count: $count")

                }

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

    override fun onDestroy() {

        super.onDestroy()

        stopTimerTask()

        val intent = Intent()

        intent.setClass(this, ServiceBroadcastReceiver::class.java)

        intent.action = "RestartService"

        sendBroadcast(intent)

    }

    override fun onBind(intent: Intent?): IBinder? {

        return null

    }

}