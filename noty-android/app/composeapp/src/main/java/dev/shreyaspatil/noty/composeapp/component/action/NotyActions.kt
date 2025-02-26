/*
 * Copyright 2020 Shreyas Patil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.shreyaspatil.noty.composeapp.component.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.shreyaspatil.noty.R

@Composable
fun DeleteAction(onClick: () -> Unit) {
    val icon = painterResource(R.drawable.ic_delete)
    Icon(
        painter = icon,
        contentDescription = "Delete",
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    )
}

@Composable
fun ShareAction(onClick: () -> Unit) {
    val icon = painterResource(R.drawable.ic_share)
    Icon(
        icon,
        "share",
        Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    )
}

data class ShareActionItem(
    val label: String,
    val onActionClick: () -> Unit,
)

@Composable
fun ShareDropdown(
    expanded: Boolean,
    shareActions: List<ShareActionItem>,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .wrapContentHeight()
            .width(100.dp)
    ) {
        shareActions.forEachIndexed { index, shareAction ->
            DropdownMenuItem(
                onClick = {
                    shareAction.onActionClick.invoke()
                    onDismissRequest.invoke()
                }
            ) {
                Row {
                    Text(text = shareAction.label)
                }
            }
        }
    }
}

@Composable
fun ThemeSwitchAction(onToggle: () -> Unit) {
    val icon = painterResource(R.drawable.ic_day)
    Icon(
        icon,
        "Theme switch",
        Modifier
            .padding(8.dp)
            .clickable(onClick = onToggle)
    )
}

@Composable
fun LogoutAction(onLogout: () -> Unit) {
    val icon = painterResource(R.drawable.ic_logout)
    Icon(
        icon,
        "Logout",
        Modifier
            .padding(8.dp)
            .clickable(onClick = onLogout)
    )
}

@Composable
fun AboutAction(onClick: () -> Unit) {
    val icon = painterResource(R.drawable.ic_baseline_info)
    Icon(
        icon,
        "About",
        Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    )
}
