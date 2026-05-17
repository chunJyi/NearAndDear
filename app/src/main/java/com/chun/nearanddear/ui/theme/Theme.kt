package com.chun.nearanddear.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun NearAndDearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        typography = NearAndDearTypography,
        content = content
    )
}
