package com.span.monitorgeofences.geo_fences.util

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.span.monitorgeofences.geo_fences.receivers.GeoFencesBroadcastReceiver

class GeoFencesHelper(mContext: Context) : ContextWrapper(mContext) {

    private var pendingIntent: PendingIntent? = null

    fun getGeoFencingRequest(geofence: Geofence): GeofencingRequest =
        GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_ENTER /*or
                        GeofencingRequest.INITIAL_TRIGGER_DWELL or
                        GeofencingRequest.INITIAL_TRIGGER_EXIT*/
            )
            .build()

    fun getGeoFence(id: String, latLng: LatLng, radius: Float, transitionTypes: Int): Geofence =
        Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setTransitionTypes(transitionTypes)
            .setLoiteringDelay(1000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

    val getGeoFencesPendingIntent: PendingIntent by lazy {

        val intent = Intent(this, GeoFencesBroadcastReceiver::class.java)

        var flag = PendingIntent.FLAG_UPDATE_CURRENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            flag = PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        PendingIntent.getBroadcast(this, 998, intent, flag)
        
    }

    fun getErrorString(e: Exception): String {

        val apiException = e as ApiException

        return when (apiException.statusCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "GEOFENCE_NOT_AVAILABLE"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "GEOFENCE_TOO_MANY_GEOFENCES"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "GEOFENCE_TOO_MANY_PENDING_INTENTS"
            GeofenceStatusCodes.GEOFENCE_INSUFFICIENT_LOCATION_PERMISSION -> "GEOFENCE_INSUFFICIENT_LOCATION_PERMISSION"
            GeofenceStatusCodes.GEOFENCE_REQUEST_TOO_FREQUENT -> "GEOFENCE_REQUEST_TOO_FREQUENT"
            else -> e.getLocalizedMessage() ?: "Something wrong!"
        }

    }

}