package com.span.monitorgeofences.geo_fences.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.span.monitorgeofences.geo_fences.services.GeoFencesService

class ServiceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action.equals("RestartService")) {

            Log.i("Broadcast Listened", "Service tried to stop")

            Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show()

            val intentServices = Intent(context, GeoFencesService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intentServices)
            else context.startService(intentServices)

        }

    }

}