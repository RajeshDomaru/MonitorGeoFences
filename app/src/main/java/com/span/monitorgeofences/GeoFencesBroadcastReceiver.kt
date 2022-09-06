package com.span.monitorgeofences

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeoFencesBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.e("GeoFencesBroadcastReceiver", "Done...")

        //        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)

        if (context != null && intent != null) {

            val notificationHelper = NotificationHelper(context)

            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent != null) {

                if (geofencingEvent.hasError()) {

                    Log.e(
                        "GeoFencesBroadcastReceiver",
                        "onReceive : Error receiving geoFence event..."
                    )

                    return

                }

                when (geofencingEvent.geofenceTransition) {

                    Geofence.GEOFENCE_TRANSITION_ENTER -> {

                        Log.e("GeofencingEvent", "GEOFENCE_TRANSITION_ENTER")

                        Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT)
                            .show()

                        notificationHelper.sendHighPriorityNotification(
                            "GEOFENCE_TRANSITION_ENTER", "",
                            MapsActivity::class.java
                        )

                    }

                    Geofence.GEOFENCE_TRANSITION_DWELL -> {

                        Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT)
                            .show()

                        Log.e("GeofencingEvent", "GEOFENCE_TRANSITION_DWELL")

                        notificationHelper.sendHighPriorityNotification(
                            "GEOFENCE_TRANSITION_DWELL", "",
                            MapsActivity::class.java
                        )

                    }

                    Geofence.GEOFENCE_TRANSITION_EXIT -> {

                        Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT)
                            .show()

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

}