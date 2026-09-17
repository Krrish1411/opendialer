package com.opendialer.app.ui.screens.recents

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opendialer.app.core.common.DateTimeUtils
import com.opendialer.app.core.designsystem.components.ContactAvatar
import com.opendialer.app.core.designsystem.components.SimBadge
import com.opendialer.app.data.model.CallLogUiModel
import com.opendialer.app.data.model.CallType
import com.opendialer.app.data.repository.CallLogFilter

@Composable
fun RecentsScreen(
    viewModel: RecentsViewModel,
    modifier: Modifier = Modifier
) {
    val callLogs by viewModel.callLogs.collectAsState()
    val activeFilter by viewModel.selectedFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CallLogFilter.values().forEach { filter ->
                FilterChip(
                    selected = activeFilter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (callLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent calls found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(callLogs, key = { it.id }) { log ->
                    CallLogItem(
                        log = log,
                        onCallClick = { viewModel.placeCall(context, log.number) },
                        onDeleteClick = { viewModel.deleteCall(log.id) },
                        onBlockClick = { viewModel.blockNumber(log.number, log.contactName) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallLogItem(
    log: CallLogUiModel,
    onCallClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBlockClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onCallClick,
                onLongClick = { menuExpanded = true }
            ),
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                name = log.contactName ?: log.number,
                photoUri = log.photoUri,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.contactName ?: log.number,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = if (log.callType == CallType.MISSED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    if (log.callCount > 1) {
                        Text(
                            text = " (${log.callCount})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    val (icon, tint) = when (log.callType) {
                        CallType.INCOMING -> Pair(Icons.Default.CallReceived, Color(0xFF10B981))
                        CallType.OUTGOING -> Pair(Icons.Default.CallMade, Color(0xFF3B82F6))
                        CallType.MISSED -> Pair(Icons.Default.CallMissed, MaterialTheme.colorScheme.error)
                        CallType.BLOCKED -> Pair(Icons.Default.Block, MaterialTheme.colorScheme.error)
                        CallType.REJECTED -> Pair(Icons.Default.CallMissed, Color(0xFFF59E0B))
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = log.callType.name,
                        tint = tint,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    SimBadge(
                        simLabel = log.simDisplayName,
                        slotIndex = log.simSlotIndex
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = DateTimeUtils.formatCallTime(log.timestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onCallClick) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Call ${log.number}") },
                        onClick = {
                            menuExpanded = false
                            onCallClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("WhatsApp Chat") },
                        onClick = {
                            menuExpanded = false
                            com.opendialer.app.core.telephony.SocialActionHelper.openWhatsAppChat(context, log.number)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Send SMS") },
                        onClick = {
                            menuExpanded = false
                            com.opendialer.app.core.telephony.SocialActionHelper.sendSms(context, log.number)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Block number") },
                        onClick = {
                            menuExpanded = false
                            onBlockClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete from log") },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}
