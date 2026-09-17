package com.opendialer.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opendialer.app.data.model.BarringType
import com.opendialer.app.data.model.ForwardingReason

@Composable
fun CallForwardingScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var alwaysForwardNumber by remember { mutableStateOf("") }
    var busyForwardNumber by remember { mutableStateOf("") }
    var callWaitingActive by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Call Forwarding (Carrier MMI)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Always Forward Card
        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Always Forward (Unconditional)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("Direct all incoming calls to another number", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = alwaysForwardNumber,
                        onValueChange = { alwaysForwardNumber = it },
                        placeholder = { Text("Forward to phone number...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = {
                            viewModel.setCallForwarding(ForwardingReason.ALWAYS, alwaysForwardNumber, false)
                        }) {
                            Text("Disable")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (alwaysForwardNumber.isNotBlank()) {
                                    viewModel.setCallForwarding(ForwardingReason.ALWAYS, alwaysForwardNumber, true)
                                }
                            }
                        ) {
                            Text("Enable")
                        }
                    }
                }
            }
        }

        // When Busy Forward Card
        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Forward When Busy", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("Forward calls if you are already on an active call", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = busyForwardNumber,
                        onValueChange = { busyForwardNumber = it },
                        placeholder = { Text("Forward to phone number...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = {
                            viewModel.setCallForwarding(ForwardingReason.BUSY, busyForwardNumber, false)
                        }) {
                            Text("Disable")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (busyForwardNumber.isNotBlank()) {
                                    viewModel.setCallForwarding(ForwardingReason.BUSY, busyForwardNumber, true)
                                }
                            }
                        ) {
                            Text("Enable")
                        }
                    }
                }
            }
        }

        // Call Waiting
        item {
            Text(
                text = "Call Waiting & Barring",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
        }

        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Call Waiting (*43#)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text("Receive incoming call notifications during an active call", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = callWaitingActive,
                            onCheckedChange = {
                                callWaitingActive = it
                                viewModel.setCallWaiting(it)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Call Barring (International Outgoing)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text("Prevent outgoing international roaming calls", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(onClick = {
                            viewModel.setCallBarring(BarringType.INTERNATIONAL_OUTGOING, false, "0000")
                        }) {
                            Text("Query")
                        }
                    }
                }
            }
        }
    }
}
