package com.chun.nearanddear.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DialogUtils {

    @Composable
    fun SimpleDialog(
        showDialog: MutableState<Boolean>,
        title: String,
        message: String,
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
        confirmText: String = "OK",
        dismissText: String = "Cancel"
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                    onDismiss()
                },
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog.value = false
                            onConfirm()
                        }
                    ) {
                        Text(confirmText)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog.value = false
                            onDismiss()
                        }
                    ) {
                        Text(dismissText)
                    }
                }
            )
        }
    }

    @Composable
    fun InfoDialog(
        showDialog: MutableState<Boolean>,
        title: String,
        message: String,
        buttonText: String = "OK",
        onDismiss: () -> Unit = {}
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                    onDismiss()
                },
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog.value = false
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(buttonText)
                    }
                }
            )
        }
    }

    @Composable
    fun ConfirmationDialog(
        showDialog: MutableState<Boolean>,
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit = {},
        confirmText: String = "Confirm",
        dismissText: String = "Cancel",
        confirmButtonColor: Color = MaterialTheme.colorScheme.primary
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                    onDismiss()
                },
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog.value = false
                            onConfirm()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmButtonColor
                        )
                    ) {
                        Text(confirmText)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog.value = false
                            onDismiss()
                        }
                    ) {
                        Text(dismissText)
                    }
                }
            )
        }
    }

    @Composable
    fun WarningDialog(
        showDialog: MutableState<Boolean>,
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit = {},
        confirmText: String = "Proceed",
        dismissText: String = "Cancel"
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                    onDismiss()
                },
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFFF9800)
                    )
                },
                text = {
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog.value = false
                            onConfirm()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        )
                    ) {
                        Text(confirmText)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog.value = false
                            onDismiss()
                        }
                    ) {
                        Text(dismissText)
                    }
                }
            )
        }
    }

    @Composable
    fun ErrorDialog(
        showDialog: MutableState<Boolean>,
        title: String = "Error",
        message: String,
        onDismiss: () -> Unit = {},
        buttonText: String = "OK"
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                    onDismiss()
                },
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFFF5252)
                    )
                },
                text = {
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog.value = false
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252)
                        )
                    ) {
                        Text(buttonText)
                    }
                }
            )
        }
    }

    @Composable
    fun CustomDialog(
        showDialog: MutableState<Boolean>,
        title: String,
        content: @Composable () -> Unit,
        onDismiss: () -> Unit = {},
        actions: @Composable () -> Unit = {}
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                    onDismiss()
                },
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = content,
                confirmButton = actions
            )
        }
    }

    @Composable
    fun LoadingDialog(
        showDialog: MutableState<Boolean>,
        title: String = "Loading...",
        message: String = "Please wait..."
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    // Prevent dismissal during loading
                },
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = message,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {}
            )
        }
    }
}
