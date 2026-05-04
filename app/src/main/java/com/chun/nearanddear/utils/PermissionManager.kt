package com.chun.nearanddear.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: Activity) {
    
    companion object {
        const val PERMISSION_REQUEST_CODE = 1001

        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val SETUP_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            LOCATION_PERMISSIONS + Manifest.permission.POST_NOTIFICATIONS
        } else {
            LOCATION_PERMISSIONS
        }
        
        fun Context.hasPermission(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        fun Context.hasAllPermissions(permissions: Array<String>): Boolean {
            return permissions.all { hasPermission(it) }
        }
    }
    
    fun areAllPermissionsGranted(): Boolean {
        return areLocationPermissionsGranted() && isNotificationPermissionGranted()
    }
    
    fun areLocationPermissionsGranted(): Boolean {
        return LOCATION_PERMISSIONS.any { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isNotificationPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            activity,
            SETUP_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }
    
    fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            activity,
            LOCATION_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }
    
    fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    fun getDeniedPermissions(): List<String> {
        val deniedPermissions = mutableListOf<String>()

        if (!areLocationPermissionsGranted()) {
            deniedPermissions.addAll(LOCATION_PERMISSIONS)
        }

        if (!isNotificationPermissionGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            deniedPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return deniedPermissions
    }
    
    fun getDeniedLocationPermissions(): List<String> {
        return LOCATION_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
}
