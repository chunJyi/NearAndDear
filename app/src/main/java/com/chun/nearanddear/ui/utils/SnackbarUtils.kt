package com.chun.nearanddear.ui.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object SnackbarUtils {

    @Composable
    fun DefaultSnackbar(
        snackbarHostState: SnackbarHostState,
        modifier: Modifier = Modifier
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = modifier.padding(16.dp),
            snackbar = { snackbarData ->
                CustomSnackbar(snackbarData = snackbarData)
            }
        )
    }

    @Composable
    private fun CustomSnackbar(
        snackbarData: SnackbarData,
        backgroundColor: Color = MaterialTheme.colorScheme.inverseSurface,
        contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface
    ) {
        Snackbar(
            snackbarData = snackbarData,
            containerColor = backgroundColor,
            contentColor = contentColor,
            actionColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(4.dp)
        )
    }

    fun showSuccessSnackbar(
        snackbarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = null,
        scope: CoroutineScope,
        onAction: (() -> Unit)? = null
    ) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = true
            )
        }
    }

    fun showErrorSnackbar(
        snackbarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = "Retry",
        scope: CoroutineScope,
        onAction: (() -> Unit)? = null
    ) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = true
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onAction?.invoke()
            }
        }
    }

    fun showInfoSnackbar(
        snackbarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = null,
        scope: CoroutineScope,
        onAction: (() -> Unit)? = null
    ) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = true
            )
        }
    }

    fun showWarningSnackbar(
        snackbarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = "Dismiss",
        scope: CoroutineScope,
        onAction: (() -> Unit)? = null
    ) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = true
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onAction?.invoke()
            }
        }
    }

    @Composable
    fun SuccessSnackbar(
        message: String,
        isVisible: Boolean,
        onDismiss: () -> Unit
    ) {
        if (isVisible) {
            LaunchedEffect(isVisible) {
                kotlinx.coroutines.delay(3000)
                onDismiss()
            }
            
            androidx.compose.material3.Card(
                modifier = Modifier.padding(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }

    @Composable
    fun ErrorSnackbar(
        message: String,
        isVisible: Boolean,
        onDismiss: () -> Unit,
        actionLabel: String? = "Retry",
        onAction: (() -> Unit)? = null
    ) {
        if (isVisible) {
            androidx.compose.material3.Card(
                modifier = Modifier.padding(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color(0xFFFF5252)
                )
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    
                    actionLabel?.let { action ->
                        androidx.compose.material3.TextButton(
                            onClick = {
                                onAction?.invoke()
                                onDismiss()
                            },
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = action,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun InfoSnackbar(
        message: String,
        isVisible: Boolean,
        onDismiss: () -> Unit
    ) {
        if (isVisible) {
            LaunchedEffect(isVisible) {
                kotlinx.coroutines.delay(4000)
                onDismiss()
            }
            
            androidx.compose.material3.Card(
                modifier = Modifier.padding(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }

    @Composable
    fun WarningSnackbar(
        message: String,
        isVisible: Boolean,
        onDismiss: () -> Unit,
        actionLabel: String? = "Dismiss",
        onAction: (() -> Unit)? = null
    ) {
        if (isVisible) {
            androidx.compose.material3.Card(
                modifier = Modifier.padding(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    
                    actionLabel?.let { action ->
                        androidx.compose.material3.TextButton(
                            onClick = {
                                onAction?.invoke()
                                onDismiss()
                            },
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = action,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    fun showSnackbarWithAction(
        snackbarHostState: SnackbarHostState,
        message: String,
        actionLabel: String,
        scope: CoroutineScope,
        onAction: () -> Unit
    ) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = true
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onAction()
            }
        }
    }

    fun showSimpleSnackbar(
        snackbarHostState: SnackbarHostState,
        message: String,
        scope: CoroutineScope,
        duration: androidx.compose.material3.SnackbarDuration = androidx.compose.material3.SnackbarDuration.Short
    ) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = duration
            )
        }
    }
}
