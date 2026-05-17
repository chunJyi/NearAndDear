package com.chun.nearanddear

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.chun.nearanddear.ui.navigation.AppNavHost
import com.chun.nearanddear.ui.theme.NearAndDearTheme
import com.chun.nearanddear.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private lateinit var permissionManager: PermissionManager
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        handlePermissionResults()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        permissionManager = PermissionManager(this)
        
        if (permissionManager.areAllPermissionsGranted()) {
            setContent {
                NearAndDearTheme {
                    AppNavHost()
                }
            }
        } else {
            requestPermissions()
        }
    }
    
    private fun requestPermissions() {
        val deniedPermissions = permissionManager.getDeniedPermissions()
        
        if (deniedPermissions.isNotEmpty()) {
            permissionLauncher.launch(deniedPermissions.toTypedArray())
        } else {
            setContent {
                NearAndDearTheme {
                    AppNavHost()
                }
            }
        }
    }
    
    private fun handlePermissionResults() {
        val allGranted = permissionManager.areAllPermissionsGranted()
        
        if (allGranted) {
            setContent {
                AppNavHost()
            }
        } else {
            val deniedPermissions = permissionManager.getDeniedPermissions().toSet()
            if (deniedPermissions.isNotEmpty()) {
                showPermissionDeniedMessage(deniedPermissions)
            }
            
            // Still show the app but with limited functionality
            setContent {
                NearAndDearTheme {
                    AppNavHost()
                }
            }
        }
    }
    
    private fun showPermissionDeniedMessage(deniedPermissions: Set<String>) {
        val message = when {
            deniedPermissions.contains("android.permission.ACCESS_FINE_LOCATION") ||
            deniedPermissions.contains("android.permission.ACCESS_COARSE_LOCATION") -> 
                "Location permissions are required for the app to function properly."
            
            deniedPermissions.contains("android.permission.POST_NOTIFICATIONS") -> 
                "Notification permission is required for location updates."
            
            else -> "Some permissions were denied. App functionality may be limited."
        }
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}
