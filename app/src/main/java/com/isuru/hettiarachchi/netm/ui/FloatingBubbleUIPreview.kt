package com.isuru.hettiarachchi.netm.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun FloatingBubbleUIPreview() {
    // Provide sample values for preview
    FloatingBubbleUI(
        onClose = {}, // empty lambda for preview
        download = "12 Mbps",
        upload = "4 Mbps"
    )
}
