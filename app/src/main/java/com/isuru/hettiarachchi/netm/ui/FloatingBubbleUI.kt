package com.isuru.hettiarachchi.netm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingBubbleUI(
    onClose: () -> Unit,
    download: String,
    upload: String
) {

    Box(
        modifier = Modifier
            .background(Color(0x5B000000), shape = CircleShape)
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "↓ $download", color = Color.Green, fontSize = 14.sp)
            Text(text = "↑ $upload", color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "✕", color = Color.White, fontSize = 14.sp, modifier = Modifier
                    .padding(top = 2.dp)
                    .clickable { onClose() }
                    .align(Alignment.CenterHorizontally)
            )

        }
    }

}