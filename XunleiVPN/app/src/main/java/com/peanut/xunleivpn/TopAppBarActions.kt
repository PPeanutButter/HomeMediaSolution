package com.peanut.xunleivpn

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@Composable
fun SettingAction(onClicked: () -> Unit) {
    IconButton(onClick = { onClicked() }) {
        Icon(imageVector = Icons.Rounded.Settings, contentDescription = null)
    }
}

@Composable
fun SendAction(enabled: Boolean, onClicked: () -> Unit) {
    IconButton(onClick = { onClicked() }, enabled = enabled) {
        Icon(painter = painterResource(id = R.drawable.ic_round_import_export_24), contentDescription = null)
    }
}

@Composable
fun LoadAction(enabled: Boolean, onClicked: () -> Unit) {
    IconButton(onClick = { onClicked() }, enabled = enabled) {
        Icon(painter = painterResource(id = R.drawable.ic_baseline_insert_drive_file_24), contentDescription = null)
    }
}
