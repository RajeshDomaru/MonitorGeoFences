package com.span.monitorgeofences.geo_fences.services

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.span.monitorgeofences.R
import com.span.monitorgeofences.geo_fences.MapsActivity
import com.span.monitorgeofences.geo_fences.util.GeofenceErrorMessages
import com.span.monitorgeofences.geo_fences.util.NotificationHelper

/**
 * Listener for geofence transition changes.
 *
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
class GeofenceTransitionsJobIntentService : JobIntentService() {
    /**
     * Handles incoming intents.
     *
     * @param intent sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */
    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage =
                    GeofenceErrorMessages.getErrorString(this, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            // Get the transition type.
            val geofenceTransition = geofencingEvent.geofenceTransition

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

                // Get the geofences that were triggered. A single event can trigger multiple geofences.
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                if (triggeringGeofences != null) {

                    // Get the transition details as a String.
                    val geofenceTransitionDetails =
                        getGeofenceTransitionDetails(geofenceTransition, triggeringGeofences)

                    // Send notification and log the transition details.
//                sendNotification(geofenceTransitionDetails);
                    NotificationHelper(applicationContext).sendHighPriorityNotification(
                        getTransitionString(geofenceTransition),
                        geofenceTransitionDetails,
                        MapsActivity::class.java
                    )
                    Log.i(TAG, geofenceTransitionDetails)
                }
            } else {
                // Log the error.
                Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition))
            }
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition  The ID of the geofence transition.
     * @param triggeringGeofences The geofence(s) triggered.
     * @return The transition details formatted as String.
     */
    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        val geofenceTransitionString = getTransitionString(geofenceTransition)

        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = ArrayList<String?>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)
        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> getString(R.string.geofence_transition_entered)
            Geofence.GEOFENCE_TRANSITION_EXIT -> getString(R.string.geofence_transition_exited)
            else -> getString(R.string.unknown_geofence_transition)
        }
    }

    companion object {
        private const val JOB_ID = 573
        private const val TAG = "GeofenceTransitionsIS"

        /**
         * Convenience method for enqueuing work in to this service.
         */
        fun enqueueWork(context: Context?, intent: Intent?) {
            enqueueWork(
                context!!,
                GeofenceTransitionsJobIntentService::class.java,
                JOB_ID,
                intent!!
            )
        }
    }
}