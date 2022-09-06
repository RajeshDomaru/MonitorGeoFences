package com.span.monitorgeofences.live_location.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.span.monitorgeofences.live_location.services.LocationService
import com.span.monitorgeofences.live_location.utils.Util

class RestartBackgroundService : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        if (intent?.action.equals("RestartService")) {

            Log.i("Broadcast Listened", "Service tried to stop")

//            Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show()

            val serviceIntent = Intent(context, LocationService::class.java)

            if (Util.isMyServiceRunning(serviceIntent.javaClass, context))
                context.stopService(serviceIntent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(serviceIntent)
            else context.startService(serviceIntent)

        }

    }

}