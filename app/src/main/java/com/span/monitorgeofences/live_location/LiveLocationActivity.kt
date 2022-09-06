package com.span.monitorgeofences.live_location

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.span.monitorgeofences.R
import com.span.monitorgeofences.databinding.ActivityLiveLocationBinding
import com.span.monitorgeofences.live_location.services.LocationService
import com.span.monitorgeofences.live_location.utils.Util

class LiveLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveLocationBinding

    private var locationService: LocationService = LocationService()

    private lateinit var serviceIntent: Intent

    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityLiveLocationBinding.inflate(layoutInflater)

        setContentView(binding.root)

        mActivity = this@LiveLocationActivity

        initialization()

        setOnClickListeners()

    }

    private val liveLocationBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent != null) {

                val liveLocation = intent.extras?.getString("liveLocation") ?: ""

                binding.tvLiveLatLng.text = liveLocation

            }

        }

    }

    private fun initialization() {

        if (!Util.isLocationEnabledOrNot(mActivity)) {
            Util.showAlertLocation(
                mActivity,
                getString(R.string.gps_enable),
                getString(R.string.please_turn_on_gps),
                getString(R.string.ok)
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissionsSafely(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE
                ), 200
            )
        } else {
            requestPermissionsSafely(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200)
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(liveLocationBroadcastReceiver, IntentFilter("LiveLocationUpdates"))

    }

    private fun setOnClickListeners() {

        binding.btnStartService.setOnClickListener {
            locationService = LocationService()
            serviceIntent = Intent(this, locationService.javaClass)
            if (Util.isMyServiceRunning(locationService.javaClass, mActivity)) {
                Toast.makeText(
                    mActivity,
                    getString(R.string.service_already_running),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startForegroundService(serviceIntent)
                else startService(serviceIntent)
                Toast.makeText(
                    mActivity,
                    getString(R.string.service_start_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnStopService.setOnClickListener {
            stopLiveLocationService()
        }

    }

    private fun stopLiveLocationService() {
        if (::serviceIntent.isInitialized) {
            stopService(serviceIntent)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissionsSafely(
        permissions: Array<String>,
        requestCode: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
    }

    override fun onDestroy() {
        stopLiveLocationService()
        super.onDestroy()
    }

}