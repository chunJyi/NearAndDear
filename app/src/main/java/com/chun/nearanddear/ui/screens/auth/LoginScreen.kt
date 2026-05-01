package com.chun.nearanddear.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chun.nearanddear.R
import com.chun.nearanddear.ui.utils.DialogUtils

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()

    // Dialog states
    val showErrorDialog = remember { mutableStateOf(false) }
    val errorTitle = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }

    val showLoadingDialog = remember { mutableStateOf(false) }
    val showPrivacyDialog = remember { mutableStateOf(false) }

    /* ---------------- State → UI mapping ---------------- */

    // Loading
    LaunchedEffect(uiState.isLoading) {
        showLoadingDialog.value = uiState.isLoading
    }

    // Success
    LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            showLoadingDialog.value = false
            onLoginSuccess()
        }
    }

    // Error (omit title/message when cleared after dismiss)
    LaunchedEffect(uiState.errorTitle, uiState.errorMessage) {
        val title = uiState.errorTitle
        val message = uiState.errorMessage
        if (title != null && message != null) {
            errorTitle.value = title
            errorMessage.value = message
            showErrorDialog.value = true
        }
    }

    /* ---------------- Dialogs ---------------- */

//    DialogUtils.LoadingDialog(
//        showDialog = showLoadingDialog,
//        title = "Signing in",
//        message = "Please wait..."
//    )

    DialogUtils.ErrorDialog(
        showDialog = showErrorDialog,
        title = errorTitle.value.ifBlank { "Login failed" },
        message = errorMessage.value,
        onDismiss = {
            viewModel.clearLoginError()
        }
    )

    /* ---------------- UI ---------------- */

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    GoogleSignInButton(
                        isEnabled = !uiState.isLoading
                    ) {
                        viewModel.loginWithGoogle(context)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FacebookSignInButton {
                        errorTitle.value = "Not available"
                        errorMessage.value = "Facebook Sign-In is not available yet."
                        showErrorDialog.value = true
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TermsText(showPrivacyDialog)
                }
            }
        }
    }
}

/**
 * Composable function to display the Google Sign-In button.
 *
 * @param isEnabled boolean indicating whether the button is enabled
 * @param onClick lambda function to be executed when the button is clicked
 */
@Composable
private fun GoogleSignInButton(isEnabled: Boolean, onClick: () -> Unit) {
    SignInButton(
        iconResId = R.drawable.google_logo,
        text = "Continue with Google",
        backgroundColor = Color(0xFFFFFFFF),
        onClick = onClick,
        textColor = Color.Black,
        isEnabled = isEnabled
    )
}

/**
 * Composable function to display the Facebook Sign-In button.
 *
 * @param onClick lambda function to be executed when the button is clicked
 */
@Composable
private fun FacebookSignInButton(onClick: () -> Unit) {
    SignInButton(
        iconResId = R.drawable.icons8_facebook_150,
        text = "Continue with Facebook",
        backgroundColor = Color(0xFF5795E6),
        textColor = Color.White,
        onClick = onClick
    )
}

/**
 * Composable function to display a Sign-In button.
 *
 * @param iconResId resource ID of the icon to be displayed
 * @param text text to be displayed
 * @param backgroundColor background color of the button
 * @param onClick lambda function to be executed when the button is clicked
 * @param isEnabled boolean indicating whether the button is enabled
 */
@Composable
private fun SignInButton(
    iconResId: Int,
    text: String,
    textColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 14.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false
            )
            .border(
                width = 1.5.dp,
                color = Color.Gray.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$text Icon",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                fontSize = 16.sp,
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Composable function to display the terms of service and privacy policy text.
 *
 * @param showPrivacyDialog mutable state indicating whether the privacy policy dialog is visible
 */
@Composable
private fun TermsText(showPrivacyDialog: MutableState<Boolean>) {
    Text(
        text = "By clicking continue, you agree to our Terms of Service and Privacy Policy",
        fontSize = 12.sp,
        color = Color.Blue,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { showPrivacyDialog.value = true }
    )
}
