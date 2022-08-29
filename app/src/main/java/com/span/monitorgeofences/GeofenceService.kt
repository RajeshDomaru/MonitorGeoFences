package com.span.monitorgeofences

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

//class GeofenceService : IntentService(GeofenceService::class.java.simpleName) {
class GeofenceService : Service() {

    private val tag = "Geo Fences Service"
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    /*override fun onHandleIntent(intent: Intent?) {

        Log.e("Vanthucha?2", "Vanthuduchi! Vancthuduchi! Vancthuduchi!")

        try {

            if (intent != null) {

                val MY_VALUE = intent.getBundleExtra("MY_VALUE")

                val notificationHelper = NotificationHelper(applicationContext)

                val geofencingEvent = GeofencingEvent.fromIntent(intent)

                if (geofencingEvent != null) {

                    if (geofencingEvent.hasError()) {

                        Log.e(tag, "onReceive: Error receiving geoFence event...")

                    } else {

                        when (geofencingEvent.geofenceTransition) {

                            Geofence.GEOFENCE_TRANSITION_ENTER -> {

                                Log.e("GeofencingEvent", "GEOFENCE_TRANSITION_ENTER")

                                Toast.makeText(
                                    applicationContext,
                                    "GEOFENCE_TRANSITION_ENTER",
                                    Toast.LENGTH_SHORT
                                ).show()

                                notificationHelper.sendHighPriorityNotification(
                                    "GEOFENCE_TRANSITION_ENTER", "",
                                    MapsActivity::class.java
                                )

                            }

                            Geofence.GEOFENCE_TRANSITION_DWELL -> {

                                Toast.makeText(
                                    applicationContext,
                                    "GEOFENCE_TRANSITION_DWELL",
                                    Toast.LENGTH_SHORT
                                ).show()

                                Log.e("GeofencingEvent", "GEOFENCE_TRANSITION_DWELL")

                                notificationHelper.sendHighPriorityNotification(
                                    "GEOFENCE_TRANSITION_DWELL", "",
                                    MapsActivity::class.java
                                )

                            }

                            Geofence.GEOFENCE_TRANSITION_EXIT -> {

                                Toast.makeText(
                                    applicationContext,
                                    "GEOFENCE_TRANSITION_EXIT",
                                    Toast.LENGTH_SHORT
                                ).show()

                                Log.e("GeofencingEvent", "GEOFENCE_TRANSITION_EXIT")

                                notificationHelper.sendHighPriorityNotification(
                                    "GEOFENCE_TRANSITION_EXIT",
                                    "",
                                    MapsActivity::class.java
                                )

                            }

                        }

                    }

                }

            }

        } catch (e: Exception) {

            e.printStackTrace()

        }

    }*/

}