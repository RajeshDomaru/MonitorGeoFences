package com.span.monitorgeofences

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.span.monitorgeofences.databinding.ActivityMapsBinding


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private lateinit var binding: ActivityMapsBinding

    private var mLocationService: LocationService = LocationService()

    private lateinit var mServiceIntent: Intent

    private val geoFencesId = "GEO_FENCES_ID"

    private val radiusPoint = 100.0

    private val isQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        mLocationService = LocationService()

        mServiceIntent = Intent(this, mLocationService.javaClass)

    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun enableUserLocation() {

        if (approveForegroundAndBackgroundLocation()) {

            val latLng = LatLng(12.98887152580908, 80.25068689137697)

            setMonitorGeoFences(latLng)

            googleMap.isMyLocationEnabled = true

        } else {

            askLocationPermission()

        }

    }

    private fun addMarker(latLng: LatLng) {

        val markerOptions = MarkerOptions().apply {

            position(latLng)

            title(resources.getString(R.string.span_address))

        }

        googleMap.addMarker(markerOptions)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17F))

    }

    private fun addCircle(latLng: LatLng) {

        val circleOptions = CircleOptions().apply {

            center(latLng)

            radius(radiusPoint)

            strokeColor(Color.argb(255, 255, 0, 0))

            fillColor(Color.argb(64, 255, 0, 0))

            strokeWidth(4F)

        }

        googleMap.addCircle(circleOptions)

    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //We have the permission
                if (!approveForegroundAndBackgroundLocation()) return

                googleMap.isMyLocationEnabled = true

            } else {

                //We do not have the permission..

            }

        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show()

            } else {

                Toast.makeText(
                    this,
                    "Background location access is necessary for geofences to trigger...",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(newGoogleMap: GoogleMap) {

        googleMap = newGoogleMap

        googleMap.setOnMapLongClickListener { longPressLatLng ->

            if (approveForegroundAndBackgroundLocation()) {

                setMonitorGeoFences(longPressLatLng)

            } else {

                askLocationPermission()

            }

        }

        enableUserLocation()

    }

    private fun setMonitorGeoFences(latLng: LatLng) {

        googleMap.clear()

        addMarker(latLng)

        addCircle(latLng)

//        addGeoFence(latLng)

        if (Util.isMyServiceRunning(mLocationService.javaClass, this)) {

            Log.e("setMonitorGeoFences", getString(R.string.service_already_running))

        } else {

            mServiceIntent.putExtra("latitude", latLng.latitude)

            mServiceIntent.putExtra("longitude", latLng.longitude)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(mServiceIntent)
            else startService(mServiceIntent)

            Toast.makeText(
                applicationContext,
                getString(R.string.service_start_successfully),
                Toast.LENGTH_SHORT
            ).show()

        }

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
                        Toast.makeText(this, "Geofencing Successful", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMessage =
                            task.exception?.let { geoFencesHelper.getErrorString(it) }
                        Log.e("Geofencing Failed: ", errorMessage!!)
                    }

                }

            /*.addOnSuccessListener {

                Log.e("addOnSuccessListener ", "Success...")

            }.addOnFailureListener {

                Log.e("addOnFailureListener ", geoFencesHelper.getErrorString(it))

            }*/

        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun approveForegroundAndBackgroundLocation(): Boolean {

        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ))

        val backgroundPermissionApproved =
            if (isQ) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else true

        return foregroundLocationApproved && backgroundPermissionApproved

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun authorizedLocation(): Boolean {

        val formalizeForeground = (
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ))

        val formalizeBackground =
            if (isQ) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else true

        return formalizeForeground && formalizeBackground

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun askLocationPermission() {

        if (authorizedLocation()) return

        var grantingPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val customResult = when {

            isQ -> {
                grantingPermission += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                BACKGROUND_LOCATION_ACCESS_REQUEST_CODE
            }

            else -> FINE_LOCATION_ACCESS_REQUEST_CODE

        }

        ActivityCompat.requestPermissions(this, grantingPermission, customResult)

    }

    /*override fun onPause() {

        super.onPause()

        *//*val outMetrics = DisplayMetrics()

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(outMetrics)
        } else {
            windowManager.defaultDisplay.getMetrics(outMetrics)
        }*//*

       *//* val d: Display = windowManager.defaultDisplay
        val p = Point()
        d.getSize(p)
        val width: Int = p.x
        val height: Int = p.y*//*

//        val ratio = Rational(width, height)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pipBuilder = PictureInPictureParams.Builder()
//            pipBuilder.setAspectRatio(ratio).build()
            enterPictureInPictureMode(pipBuilder.build())
        }

    }*/

}