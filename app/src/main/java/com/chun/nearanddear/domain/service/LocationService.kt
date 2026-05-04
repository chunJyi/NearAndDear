package com.chun.nearanddear.domain.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.TrafficStats
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.chun.nearanddear.data.remote.supabase.SupabaseAuthDataSource
import com.chun.nearanddear.data.session.SessionDataStore
import com.chun.nearanddear.domain.model.Location
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.chun.nearanddear.R
import jakarta.inject.Inject

class LocationService : Service() {

    @Inject
    lateinit var sessionDataStore: SessionDataStore
    
    @Inject
    lateinit var supabaseAuthDataSource: SupabaseAuthDataSource

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val channelId = "location_channel"
    private val notificationId = 1

    companion object {
        const val ACTION_STOP_SERVICE = "com.chun.nearanddear.ACTION_STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
//        UserSession.serverRunning = true
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (location in result.locations) {
                    val lat = location.latitude.toString()
                    val lng = location.longitude.toString()
                    Log.d("LocationService", "Lat: $lat, Lng: $lng")
                    // Update location in SessionDataStore
                    val domainLocation = Location(
                        userID = sessionDataStore.userOrNull?.userID ?: "",
                        latitude = lat,
                        longitude = lng
                    )
                    sessionDataStore.setLocation(domainLocation)
                    CoroutineScope(Dispatchers.IO).launch {
                        checkBandwidth {
                            pushUserLocation(lat, lng)
                        }
                    }
                    updateNotification(lat, lng)
                }
            }
        }
    }

    // Bandwidth checker: accepts a suspend lambda
    suspend fun checkBandwidth(block: suspend () -> Unit) {
        val txBefore = TrafficStats.getUidTxBytes(android.os.Process.myUid())
        val rxBefore = TrafficStats.getUidRxBytes(android.os.Process.myUid())

        block()

        val txAfter = TrafficStats.getUidTxBytes(android.os.Process.myUid())
        val rxAfter = TrafficStats.getUidRxBytes(android.os.Process.myUid())

        val sent = txAfter - txBefore
        val received = rxAfter - rxBefore

        Log.d("Bandwidth", "Sent: $sent bytes, Received: $received bytes")
    }

    suspend fun pushUserLocation(
        latitude: String,
        longitude: String
    ): Result<Unit> {
        val userId = sessionDataStore.userOrNull?.userID
            ?: return Result.failure(IllegalStateException("User not logged in"))

        val location = Location(
            userID = userId,
            latitude = latitude,
            longitude = longitude
        )

        return runCatching {
            supabaseAuthDataSource.updateUserLocation(location)
        }
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(
            notificationId,
            createNotification("Getting location..."),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION // Android 10+
        )
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateDistanceMeters(1f)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                request, locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun createNotification(contentText: String): Notification {
        val stopIntent = Intent(this, LocationService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val pendingStopIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking Active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.facebook_logo)
            .addAction(R.drawable.facebook_logo, "Stop", pendingStopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(lat: String, lng: String) {
        val updatedNotification = createNotification("Lat: $lat, Lng: $lng")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, updatedNotification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId, "Location Service", NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}