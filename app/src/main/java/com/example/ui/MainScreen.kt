package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuditLog
import com.example.data.DeviceStatus
import com.example.data.DeviceType
import com.example.data.NetworkDevice
import com.example.data.ThreatAlert
import com.example.data.VlanInfo
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.webkit.JavascriptInterface
import android.os.Handler
import android.os.Looper
import com.example.ui.theme.BlueOnPrimary
import com.example.ui.theme.BlueOnPrimaryContainer
import com.example.ui.theme.BlueOnSecondaryContainer
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BluePrimaryContainer
import com.example.ui.theme.BlueSecondaryContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NetBackground
import com.example.ui.theme.NetOnBackground
import com.example.ui.theme.NetOutline
import com.example.ui.theme.NetSubtext
import com.example.ui.theme.SuccessGreen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import com.example.ui.theme.NetSurface
import com.example.ui.theme.WarningOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: NetworkViewModel) {
    // Current Active Tab
    var currentTab by remember { mutableStateOf("Console") }

    // Observers
    val devices by viewModel.devices.collectAsState()
    val vlans by viewModel.vlans.collectAsState()
    val activeThreats by viewModel.activeThreats.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val isDeployingAi by viewModel.isDeployingAi.collectAsState()
    val networkOverview by viewModel.networkOverview.collectAsState()
    val activeCiscoConfig by viewModel.activeCiscoConfig.collectAsState()
    val healthScore by viewModel.overallHealthScore.collectAsState()
    val latencyMs by viewModel.avgLatencyMs.collectAsState()
    val bandwidthKbps by viewModel.totalBandwidthKbps.collectAsState()

    // Dialog state for device inspector
    var selectedDeviceForInspector by remember { mutableStateOf<NetworkDevice?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NetBackground)
                .padding(innerPadding)
        ) {
            // --- TOP HEADER ---
            HeaderSection()

            // --- RENDER CURRENT TAB MODULE ---
            when (currentTab) {
                "Console" -> ConsoleTab(
                    viewModel = viewModel,
                    devices = devices,
                    vlans = vlans,
                    activeThreats = activeThreats,
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    healthScore = healthScore,
                    latencyMs = latencyMs,
                    bandwidthKbps = bandwidthKbps,
                    onDeviceClick = { selectedDeviceForInspector = it }
                )
                "Topology" -> TopologyTab(
                    viewModel = viewModel,
                    devices = devices,
                    vlans = vlans,
                    onDeviceClick = { selectedDeviceForInspector = it }
                )
                "Bank Config" -> BankConfigTab(viewModel = viewModel)
                "AI Wizard" -> AIWizardTab(
                    viewModel = viewModel,
                    isDeployingAi = isDeployingAi,
                    networkOverview = networkOverview,
                    activeCiscoConfig = activeCiscoConfig,
                    vlans = vlans
                )
                "CLI Console" -> CLIConsoleTab(
                    viewModel = viewModel
                )
            }
        }

        // --- DEVICE INSPECTOR BOTTOM SHEET ---
        if (selectedDeviceForInspector != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedDeviceForInspector = null },
                sheetState = sheetState,
                containerColor = NetBackground,
                contentColor = NetOnBackground
            ) {
                DeviceInspectorSheet(
                    device = selectedDeviceForInspector!!,
                    onPingRequest = { targetIp ->
                        selectedDeviceForInspector = null
                        currentTab = "CLI Console"
                        viewModel.executeConsoleCommand("ping $targetIp")
                    },
                    onClose = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                selectedDeviceForInspector = null
                            }
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// COMPONENT: HEADER SECTION
// ==========================================
@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "NetArchitect AI",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = BlueOnSecondaryContainer,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Senior Network Engineer Interface",
                fontSize = 13.sp,
                color = NetSubtext,
                fontWeight = FontWeight.Medium
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(BluePrimaryContainer, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountTree,
                contentDescription = "System Status",
                tint = BlueOnPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ==========================================
// TAB: CONSOLE DASHBOARD
// ==========================================
@Composable
fun ConsoleTab(
    viewModel: NetworkViewModel,
    devices: List<NetworkDevice>,
    vlans: List<VlanInfo>,
    activeThreats: List<ThreatAlert>,
    isScanning: Boolean,
    scanProgress: Float,
    healthScore: Int,
    latencyMs: Double,
    bandwidthKbps: Int,
    onDeviceClick: (NetworkDevice) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. ACTIVE DEPLOYMENT STATE (POLISHED BANNER) ---
        ActiveDeploymentBanner(healthScore = healthScore, latencyMs = latencyMs, vlans = vlans)

        // --- 2. SECURITY INTRUSION FEED (AUTOMATED THREAT DETECTION) ---
        AnimatedVisibility(visible = activeThreats.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF2F2), shape = RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFFFD1D1), shape = RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Threat Detected",
                        tint = ErrorRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AUTOMATED INTRUSION ALERT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                activeThreats.forEach { alert ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alert.description,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NetOnBackground
                            )
                            Text(
                                text = "Source IP: ${alert.sourceIp} | Port: ${alert.interfacePort} | MAC: ${alert.sourceMac}",
                                fontSize = 11.sp,
                                color = NetSubtext
                            )
                        }
                        Button(
                            onClick = { viewModel.handleMitigateThreat(alert.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("auto_fix_button")
                                .height(28.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text("AUTO-FIX", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 3. QUICK GRID METRICS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // VLAN Manager status card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { },
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, NetOutline),
                colors = CardDefaults.cardColors(containerColor = NetSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lan,
                        contentDescription = "VLAN",
                        tint = BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "VLAN segments",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NetOnBackground
                    )
                    Text(
                        text = "${vlans.size} Active Segments",
                        fontSize = 11.sp,
                        color = NetSubtext
                    )
                }
            }

            // Firewall / ACL Policies status card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, NetOutline),
                colors = CardDefaults.cardColors(containerColor = NetSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "ACL Policies",
                        tint = BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "ACL policies",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NetOnBackground
                    )
                    Text(
                        text = "${vlans.size * 2 + 1} Policies Enforced",
                        fontSize = 11.sp,
                        color = NetSubtext
                    )
                }
            }
        }

        // --- 4. SCANNER SWEEP TOOL ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BluePrimaryContainer),
            colors = CardDefaults.cardColors(containerColor = BlueSecondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Admin PC Discovery Sweep",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlueOnSecondaryContainer
                        )
                        Text(
                            text = "Discover connected devices on local subnet automatically",
                            fontSize = 11.sp,
                            color = NetSubtext
                        )
                    }
                    IconButton(
                        onClick = { viewModel.startScannerSweep() },
                        modifier = Modifier
                            .testTag("rescan_network_button")
                            .background(BluePrimary, shape = CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh scan",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (isScanning) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Scanning FastEthernet administrative ports... ${String.format("%.0f", scanProgress * 100)}%",
                        fontSize = 12.sp,
                        color = BluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { scanProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        color = BluePrimary,
                        trackColor = Color.White
                    )
                }
            }
        }

        // --- 5. DETECTED DEVICES LIST ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NetSurface, shape = RoundedCornerShape(24.dp))
                .border(1.dp, NetOutline, shape = RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detected Devices (${devices.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NetOnBackground
                )
                Text(
                    text = "ADMINISTRATOR PC PORT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary,
                    modifier = Modifier
                        .background(BluePrimaryContainer, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (devices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No devices detected yet. Run discovery sweep.", color = NetSubtext, fontSize = 13.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    devices.forEach { device ->
                        DeviceRowItem(device = device, onClick = { onDeviceClick(device) })
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveDeploymentBanner(healthScore: Int, latencyMs: Double, vlans: List<VlanInfo>) {
    val activeScenarioName = if (vlans.size > 2) "Segmented Enterprise Setup" else "Bootstrap Infrastructure"
    val scenarioDetails = if (vlans.size > 2) "Configured Segmented VLANs (${vlans.joinToString { it.name }})" else "Primary LAN configuration active."
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BlueSecondaryContainer),
        border = BorderStroke(1.dp, BluePrimaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE DEPLOYMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .background(if (healthScore >= 90) SuccessGreen else WarningOrange, shape = CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (healthScore >= 90) "SYNCHRONIZED" else "INTRUSION ENFORCED",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = activeScenarioName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueOnSecondaryContainer
                )
                Text(
                    text = scenarioDetails,
                    fontSize = 12.sp,
                    color = NetSubtext
                )
            }

            // Progress tracking bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { if (healthScore >= 90) 1.0f else 0.75f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BluePrimary,
                    trackColor = Color.White
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (healthScore >= 90) "100% Fully Compliant" else "75% Link Safeguarded",
                        fontSize = 11.sp,
                        color = NetSubtext,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format("%.1f", latencyMs)}ms Average Latency",
                        fontSize = 11.sp,
                        color = NetSubtext,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceRowItem(device: NetworkDevice, onClick: () -> Unit) {
    val statusColor = when (device.status) {
        DeviceStatus.ONLINE -> SuccessGreen
        DeviceStatus.OFFLINE -> NetSubtext
        DeviceStatus.BLOCKED -> ErrorRed
        DeviceStatus.THREAT_DETECTED -> WarningOrange
    }

    val icon = when (device.type) {
        DeviceType.ROUTER -> Icons.Default.Router
        DeviceType.SWITCH -> Icons.Default.Lan
        DeviceType.PC -> Icons.Default.Computer
        DeviceType.PRINTER -> Icons.Default.Print
        DeviceType.CAMERA -> Icons.Default.CameraAlt
        DeviceType.SERVER -> Icons.Default.Storage
        DeviceType.TERMINAL -> Icons.Default.Computer
        DeviceType.SMART_DEVICE -> Icons.Default.CameraAlt
        DeviceType.GATEWAY -> Icons.Default.Router
        DeviceType.FIREWALL -> Icons.Default.Security
        DeviceType.LOAD_BALANCER -> Icons.Default.Settings
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF0F4F8), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = device.type.label,
                tint = if (device.status == DeviceStatus.BLOCKED) ErrorRed else BluePrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NetOnBackground
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (device.vlanId > 1) "VLAN ${device.vlanId}" else "Management (VLAN 1)",
                    fontSize = 11.sp,
                    color = NetSubtext
                )
            }
        }
        Text(
            text = device.ipAddress,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = NetSubtext
        )
    }
}

// ==========================================
// TAB: TOPOLOGY GRAPH VIEW
// ==========================================
class WebAppInterface(
    private val devices: List<NetworkDevice>,
    private val onDeviceClick: (NetworkDevice) -> Unit
) {
    @JavascriptInterface
    fun onDeviceClick(deviceId: String) {
        val dev = devices.firstOrNull { it.id == deviceId }
        if (dev != null) {
            Handler(Looper.getMainLooper()).post {
                onDeviceClick(dev)
            }
        }
    }
}

@Composable
fun TopologyTab(
    viewModel: NetworkViewModel,
    devices: List<NetworkDevice>,
    vlans: List<VlanInfo>,
    onDeviceClick: (NetworkDevice) -> Unit
) {
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val listState = rememberLazyListState()

    // Workspace Active Sub-tab State
    var activeWorkspaceTab by remember { mutableStateOf("CLI Monitor") }

    // Path Optimizer States
    val optimalPath by viewModel.optimalPath.collectAsState()
    val pathBottlenecks by viewModel.pathBottlenecks.collectAsState()
    val selectedSourceId by viewModel.selectedSourceNodeId.collectAsState()
    val selectedDestId by viewModel.selectedDestNodeId.collectAsState()

    // SNMPv3 States
    val snmpGroupName by viewModel.snmpGroupName.collectAsState()
    val snmpUsername by viewModel.snmpUsername.collectAsState()
    val snmpAuthPass by viewModel.snmpAuthPass.collectAsState()
    val snmpPrivPass by viewModel.snmpPrivPass.collectAsState()
    val snmpTrapReceiver by viewModel.snmpTrapReceiver.collectAsState()
    val isSnmpConfigured by viewModel.isSnmpConfigured.collectAsState()

    // CIS Audit States
    val cisComplianceScore by viewModel.cisComplianceScore.collectAsState()
    val cisAuditResults by viewModel.cisAuditResults.collectAsState()
    val isCisScanning by viewModel.isCisScanning.collectAsState()

    // Auto-scroll terminal log to bottom when updates occur
    LaunchedEffect(terminalLogs.size) {
        if (terminalLogs.isNotEmpty()) {
            listState.animateScrollToItem(terminalLogs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NetSurface),
            border = BorderStroke(1.dp, NetOutline)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Interactive D3 CCNA Topology",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueOnSecondaryContainer
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF22C55E).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "D3 ENGINE ACTIVE",
                            color = Color(0xFF22C55E),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Live force-directed topology map powered by D3.js. Nodes represent configured physical/logical hosts, grouped together inside colored boundaries according to their VLAN segments. Tap any host node to run diagnostics, drag nodes to reshape layout, or zoom/pan.",
                    fontSize = 11.sp,
                    color = NetSubtext,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // --- REAL-TIME MONITOR COMPONENT ---
        val alertedDevices = devices.filter { it.latencyMs > 100.0 || it.status == DeviceStatus.OFFLINE || it.status == DeviceStatus.BLOCKED }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NetSurface),
            border = BorderStroke(1.dp, if (alertedDevices.isNotEmpty()) Color(0xFFEF4444).copy(alpha = 0.5f) else NetOutline)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (alertedDevices.isNotEmpty()) Color(0xFFEF4444).copy(alpha = pulseAlpha) else Color(0xFF10B981).copy(alpha = pulseAlpha),
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = "REAL-TIME DIAGNOSTIC MONITOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alertedDevices.isNotEmpty()) Color(0xFFEF4444) else Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "${alertedDevices.size} ACTIVE ALERT(S)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NetSubtext,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (alertedDevices.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF10B981).copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Safe",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ALL NETWORK SEGMENTS STABLE | ZERO HIGH-LATENCY EVENTS",
                            fontSize = 10.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(alertedDevices) { dev ->
                            val isOffline = dev.status == DeviceStatus.OFFLINE || dev.status == DeviceStatus.BLOCKED
                            val alertColor = if (isOffline) Color(0xFFEF4444) else Color(0xFFF59E0B)
                            val alertText = if (isOffline) "CONNECTION LOSS" else "HIGH LATENCY (${dev.latencyMs.toInt()}ms)"
                            val alertIcon = if (isOffline) Icons.Default.WifiOff else Icons.Default.Speed
                            
                            Row(
                                modifier = Modifier
                                    .background(alertColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .border(1.dp, alertColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { onDeviceClick(dev) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = alertIcon,
                                    contentDescription = "Alert",
                                    tint = alertColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Column {
                                    Text(
                                        text = dev.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NetOnBackground
                                    )
                                    Text(
                                        text = alertText,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = alertColor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- D3 WEBVIEW VISUALIZATION ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, NetOutline),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF020617))
        ) {
            // Generating D3 JSONs
            val nodesJson = devices.map { dev ->
                val typeLabel = dev.type.label
                val typeName = dev.type.name
                val statusName = dev.status.name
                """
                {
                    "id": "${dev.id}",
                    "name": "${dev.name}",
                    "type": "$typeName",
                    "label": "$typeLabel",
                    "ip": "${dev.ipAddress}",
                    "mac": "${dev.macAddress}",
                    "vlanId": ${dev.vlanId},
                    "status": "$statusName",
                    "port": "${dev.interfacePort}",
                    "bandwidth": ${dev.bandwidthKbps},
                    "latency": ${dev.latencyMs}
                }
                """.trimIndent()
            }.joinToString(prefix = "[", postfix = "]")

            val linksList = mutableListOf<String>()
            devices.forEach { dev ->
                if (dev.connectedTo.isNotBlank()) {
                    val parent = devices.firstOrNull { it.name.equals(dev.connectedTo, ignoreCase = true) }
                    if (parent != null) {
                        linksList.add("""{"source": "${dev.id}", "target": "${parent.id}", "bandwidth": ${dev.bandwidthKbps}}""")
                    } else {
                        if (dev.type != DeviceType.ROUTER && dev.type != DeviceType.SWITCH) {
                            val coreSwitch = devices.firstOrNull { it.type == DeviceType.SWITCH }
                            if (coreSwitch != null) {
                                linksList.add("""{"source": "${dev.id}", "target": "${coreSwitch.id}", "bandwidth": ${dev.bandwidthKbps}}""")
                            }
                        }
                    }
                } else {
                    if (dev.type == DeviceType.SWITCH) {
                        val rtr = devices.firstOrNull { it.type == DeviceType.ROUTER }
                        if (rtr != null) {
                            linksList.add("""{"source": "${dev.id}", "target": "${rtr.id}", "bandwidth": ${dev.bandwidthKbps}}""")
                        }
                    }
                }
            }
            val linksJson = linksList.joinToString(prefix = "[", postfix = "]")

            val vlansJson = vlans.map { v ->
                """
                {
                    "id": ${v.id},
                    "name": "${v.name}",
                    "subnet": "${v.subnet}"
                }
                """.trimIndent()
            }.joinToString(prefix = "[", postfix = "]")

            val optimalPathJson = optimalPath.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }

            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                  <style>
                    body {
                      margin: 0;
                      padding: 0;
                      background-color: #020617;
                      color: #f1f5f9;
                      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                      overflow: hidden;
                      width: 100vw;
                      height: 100vh;
                      user-select: none;
                    }
                    svg {
                      width: 100%;
                      height: 100%;
                    }
                    .node {
                      cursor: pointer;
                      stroke-width: 2px;
                      transition: stroke-width 0.15s ease;
                    }
                    .node:hover {
                      stroke-width: 4px !important;
                    }
                    .link {
                      stroke-opacity: 0.6;
                      stroke-dasharray: 4;
                      animation: dash 10s linear infinite;
                    }
                    @keyframes dash {
                      to {
                        stroke-dashoffset: -100;
                      }
                    }
                    .hull {
                      fill-opacity: 0.08;
                      stroke-width: 2px;
                      stroke-opacity: 0.3;
                      stroke-linejoin: round;
                    }
                    .label {
                      font-size: 10px;
                      font-weight: 600;
                      fill: #e2e8f0;
                      pointer-events: none;
                      text-anchor: middle;
                    }
                    .subtext {
                      font-size: 8px;
                      fill: #94a3b8;
                      pointer-events: none;
                      text-anchor: middle;
                    }
                    /* Tooltip styling */
                    .tooltip {
                      position: absolute;
                      background: rgba(15, 23, 42, 0.95);
                      border: 1px solid #1e293b;
                      border-radius: 8px;
                      padding: 10px;
                      font-size: 11px;
                      color: #cbd5e1;
                      pointer-events: none;
                      opacity: 0;
                      transition: opacity 0.2s;
                      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
                      z-index: 10;
                    }
                    .tooltip-title {
                      font-weight: bold;
                      color: #f1f5f9;
                      margin-bottom: 4px;
                    }
                    /* Controls overlay */
                    .controls {
                      position: absolute;
                      bottom: 16px;
                      right: 16px;
                      display: flex;
                      flex-direction: column;
                      gap: 8px;
                      z-index: 5;
                    }
                    .btn {
                      background: rgba(30, 41, 59, 0.8);
                      border: 1px solid #334155;
                      color: #cbd5e1;
                      padding: 6px 10px;
                      border-radius: 6px;
                      font-size: 10px;
                      cursor: pointer;
                      font-weight: bold;
                      transition: all 0.2s;
                    }
                    .btn:hover {
                      background: #334155;
                      color: #f1f5f9;
                    }
                    /* Legend overlay */
                    .legend {
                      position: absolute;
                      top: 16px;
                      left: 16px;
                      background: rgba(15, 23, 42, 0.85);
                      border: 1px solid #1e293b;
                      border-radius: 10px;
                      padding: 10px;
                      max-width: 220px;
                      pointer-events: auto;
                      z-index: 5;
                    }
                    .legend-title {
                      font-weight: bold;
                      font-size: 11px;
                      color: #3b82f6;
                      margin-bottom: 6px;
                    }
                    .legend-item {
                      display: flex;
                      align-items: center;
                      gap: 6px;
                      font-size: 9px;
                      margin-top: 4px;
                    }
                    .legend-color {
                      width: 10px;
                      height: 10px;
                      border-radius: 20%;
                    }

                    /* Pulsating alert indicator (halo animation) */
                    @keyframes halo-glow {
                      0% {
                        transform: scale(0.9);
                        opacity: 0.85;
                      }
                      100% {
                        transform: scale(1.6);
                        opacity: 0;
                      }
                    }
                    .pulsing-halo {
                      animation: halo-glow 1.8s infinite ease-out;
                      transform-box: fill-box;
                      transform-origin: center;
                      pointer-events: none;
                      fill: none;
                    }

                    /* Click-to-expand details panel pop-up overlay */
                    .overlay-panel {
                      position: absolute;
                      bottom: -340px; /* Hidden off-screen */
                      left: 12px;
                      right: 12px;
                      background: rgba(15, 23, 42, 0.98);
                      border: 1.5px solid #334155;
                      border-radius: 16px;
                      padding: 14px;
                      color: #f1f5f9;
                      box-shadow: 0 -10px 25px -5px rgba(0, 0, 0, 0.6);
                      transition: bottom 0.3s cubic-bezier(0.16, 1, 0.3, 1);
                      z-index: 20;
                      font-size: 11px;
                      backdrop-filter: blur(10px);
                    }
                    .overlay-panel.active {
                      bottom: 12px;
                    }
                    .overlay-header {
                      display: flex;
                      justify-content: space-between;
                      align-items: center;
                      border-bottom: 1px solid #1e293b;
                      padding-bottom: 6px;
                      margin-bottom: 10px;
                    }
                    .overlay-title {
                      font-size: 13px;
                      font-weight: bold;
                      color: #38bdf8;
                    }
                    .overlay-status {
                      font-size: 9px;
                      font-weight: bold;
                      padding: 2px 6px;
                      border-radius: 4px;
                    }
                    .overlay-close {
                      background: rgba(255, 255, 255, 0.05);
                      border: none;
                      color: #94a3b8;
                      font-size: 14px;
                      font-weight: bold;
                      cursor: pointer;
                      width: 22px;
                      height: 22px;
                      border-radius: 50%;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      transition: all 0.2s;
                    }
                    .overlay-close:hover {
                      background: rgba(255, 255, 255, 0.15);
                      color: #f8fafc;
                    }
                    .overlay-grid {
                      display: grid;
                      grid-template-columns: 1fr 1fr;
                      gap: 10px;
                    }
                    .overlay-section {
                      background: rgba(30, 41, 59, 0.6);
                      border-radius: 8px;
                      padding: 8px;
                      border: 1px solid #1e293b;
                    }
                    .overlay-section-full {
                      grid-column: span 2;
                    }
                    .overlay-section-title {
                      font-size: 9px;
                      font-weight: bold;
                      color: #94a3b8;
                      text-transform: uppercase;
                      margin-bottom: 4px;
                      letter-spacing: 0.5px;
                      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
                      padding-bottom: 2px;
                    }
                    .overlay-item {
                      font-family: monospace;
                      font-size: 10px;
                      margin-top: 3px;
                      color: #cbd5e1;
                      display: flex;
                      justify-content: space-between;
                    }
                    .overlay-item strong {
                      color: #94a3b8;
                    }
                    .overlay-actions {
                      margin-top: 10px;
                      display: flex;
                      gap: 8px;
                    }
                    .overlay-btn {
                      flex: 1;
                      background: #0284c7;
                      color: white;
                      border: none;
                      padding: 6px 12px;
                      border-radius: 6px;
                      font-size: 10px;
                      font-weight: bold;
                      cursor: pointer;
                      transition: background 0.2s;
                      text-align: center;
                    }
                    .overlay-btn:hover {
                      background: #0369a1;
                    }
                    .overlay-btn-secondary {
                      background: rgba(30, 41, 59, 0.8);
                      border: 1px solid #475569;
                      color: #cbd5e1;
                    }
                    .overlay-btn-secondary:hover {
                      background: #334155;
                      color: #f1f5f9;
                    }
                  </style>
                  <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/7.8.5/d3.min.js"></script>
                </head>
                <body>
                  <div id="tooltip" class="tooltip"></div>
                  <div id="legend" class="legend">
                    <div class="legend-title">VLAN SEGMENT DIRECTORY</div>
                    <div id="legend-items"></div>
                  </div>
                  
                  <div class="controls">
                    <button class="btn" onclick="resetZoom()">RESET VIEW</button>
                    <button class="btn" onclick="reheatSimulation()">ARRANGE NODES</button>
                  </div>

                  <svg id="canvas"></svg>

                  <!-- Click-to-expand details panel overlay -->
                  <div id="device-overlay" class="overlay-panel">
                    <div class="overlay-header">
                      <div class="overlay-title" id="overlay-device-name">Device Name</div>
                      <div id="overlay-device-status-badge" class="overlay-status">ONLINE</div>
                      <button class="overlay-close" onclick="hideDeviceOverlay()">×</button>
                    </div>
                    <div class="overlay-grid">
                      <div class="overlay-section">
                        <div class="overlay-section-title">IP Configuration</div>
                        <div id="overlay-ip-config"></div>
                      </div>
                      <div class="overlay-section">
                        <div class="overlay-section-title">Interface Port Status</div>
                        <div id="overlay-port-status"></div>
                      </div>
                      <div class="overlay-section overlay-section-full">
                        <div class="overlay-section-title">Real-time Performance Metrics</div>
                        <div id="overlay-metrics" style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;"></div>
                      </div>
                    </div>
                    <div class="overlay-actions">
                      <button class="overlay-btn" id="overlay-ping-btn">RUN DIAGNOSTIC PING</button>
                      <button class="overlay-btn overlay-btn-secondary" onclick="hideDeviceOverlay()">CLOSE INSPECTOR</button>
                    </div>
                  </div>

                  <script>
                    let rawNodes = [];
                    let rawLinks = [];
                    let rawVlans = [];
                    let particles = [];
                    
                    const vlanColors = {
                      1: "#3b82f6",
                      10: "#10b981",
                      20: "#f59e0b",
                      30: "#8b5cf6",
                      40: "#ec4899",
                      50: "#14b8a6",
                      60: "#f43f5e",
                      70: "#06b6d4"
                    };

                    function getVlanColor(vlanId) {
                      return vlanColors[vlanId] || d3.interpolateRainbow((vlanId % 100) / 100);
                    }

                    const svg = d3.select("#canvas");
                    const container = svg.append("g");
                    
                    // Zoom behavior
                    const zoomBehavior = d3.zoom()
                      .scaleExtent([0.1, 4])
                      .on("zoom", (event) => {
                        container.attr("transform", event.transform);
                      });
                    
                    svg.call(zoomBehavior);

                    // Add click handler on background to hide overlay
                    svg.on("click", (event) => {
                      if (event.target.id === "canvas") {
                        hideDeviceOverlay();
                        hideTooltip();
                      }
                    });

                    function resetZoom() {
                      svg.transition().duration(750).call(
                        zoomBehavior.transform,
                        d3.zoomIdentity.translate(0, 0).scale(1)
                      );
                    }

                    // Set SVG size
                    let width = window.innerWidth;
                    let height = window.innerHeight;
                    svg.attr("width", width).attr("height", height);

                    window.addEventListener("resize", () => {
                      width = window.innerWidth;
                      height = window.innerHeight;
                      svg.attr("width", width).attr("height", height);
                    });

                    // Create subgroups in order to guarantee correct layered Z-index rendering
                    const hullGroup = container.append("g").attr("id", "hulls");
                    const linkGroup = container.append("g").attr("id", "links");
                    const particleGroup = container.append("g").attr("id", "particles-group");
                    const nodeGroup = container.append("g").attr("id", "nodes");

                    const simulation = d3.forceSimulation()
                      .force("link", d3.forceLink().id(d => d.id).distance(120))
                      .force("charge", d3.forceManyBody().strength(-500))
                      .force("center", d3.forceCenter(width / 2, height / 2))
                      .force("collision", d3.forceCollide().radius(50));

                    function isLinkInPath(link, path) {
                      if (!path || path.length < 2) return false;
                      const sId = link.source.id || link.source;
                      const tId = link.target.id || link.target;
                      
                      for (let i = 0; i < path.length - 1; i++) {
                        const p1 = path[i];
                        const p2 = path[i + 1];
                        if ((sId === p1 && tId === p2) || (sId === p2 && tId === p1)) {
                          return true;
                        }
                      }
                      return false;
                    }

                    function initializeParticles(links) {
                      particles = [];
                      links.forEach(link => {
                        const srcNode = rawNodes.find(n => n.id === link.source.id || n.id === link.source);
                        const tgtNode = rawNodes.find(n => n.id === link.target.id || n.id === link.target);
                        
                        const isBackbone = (srcNode && tgtNode) && (
                          (srcNode.type === "SWITCH" && tgtNode.type === "ROUTER") ||
                          (srcNode.type === "ROUTER" && tgtNode.type === "SWITCH")
                        );
                        
                        const inPath = isLinkInPath(link, window.activePath);
                        const count = inPath ? 6 : (isBackbone ? 8 : Math.max(2, Math.min(5, Math.floor(link.bandwidth / 400))));
                        
                        for (let i = 0; i < count; i++) {
                          particles.push({
                            link: link,
                            progress: i / count,
                            speed: inPath ? 0.015 : (isBackbone ? (0.008 + Math.random() * 0.004) : (0.003 + (link.bandwidth / 100000) + Math.random() * 0.002)),
                            size: inPath ? 5.5 : (isBackbone ? 4 : Math.max(1.5, Math.min(3, link.bandwidth / 2000))),
                            color: inPath ? "#fbbf24" : (isBackbone ? "#38bdf8" : "#10b981")
                          });
                        }
                      });
                      
                      const pSelection = particleGroup.selectAll(".particle")
                        .data(particles);
                        
                      pSelection.exit().remove();
                      
                      pSelection.enter().append("circle")
                        .attr("class", "particle")
                        .attr("r", d => d.size)
                        .attr("fill", d => d.color)
                        .merge(pSelection);
                    }

                    function updateTopology(nodes, links, vlans, activePath = []) {
                      rawNodes = nodes;
                      rawLinks = links;
                      rawVlans = vlans;
                      
                      // Update Legend
                      const legendDiv = d3.select("#legend-items");
                      legendDiv.html("");
                      vlans.forEach(vlan => {
                        legendDiv.append("div")
                          .attr("class", "legend-item")
                          .html(`
                            <div class="legend-color" style="background: ${'$'}{getVlanColor(vlan.id)}"></div>
                            <div><strong>VLAN ${'$'}{vlan.id}</strong>: ${'$'}{vlan.name} (${'$'}{vlan.subnet})</div>
                          `);
                      });

                      // Bind links
                      const link = linkGroup.selectAll(".link")
                        .data(links, d => d.source + "-" + d.target);

                      link.exit().remove();

                      const linkEnter = link.enter().append("line")
                        .attr("class", "link");

                      const allLinks = linkEnter.merge(link);
                      
                      allLinks
                        .attr("stroke", d => {
                          return isLinkInPath(d, window.activePath) ? "#eab308" : "#475569";
                        })
                        .attr("stroke-width", d => {
                          return isLinkInPath(d, window.activePath) ? 4.5 : Math.max(1.5, Math.min(6, d.bandwidth / 400));
                        })
                        .style("stroke-opacity", d => {
                          return isLinkInPath(d, window.activePath) ? 1.0 : 0.4;
                        });

                      // Initialize particle flows
                      initializeParticles(links);

                      // Bind nodes
                      const node = nodeGroup.selectAll(".node-container")
                        .data(nodes, d => d.id);

                      node.exit().remove();

                      const nodeEnter = node.enter().append("g")
                        .attr("class", "node-container")
                        .call(d3.drag()
                          .on("start", dragstarted)
                          .on("drag", dragged)
                          .on("end", dragended))
                        .on("click", (event, d) => {
                          if (window.AndroidInterface) {
                            window.AndroidInterface.onDeviceClick(d.id);
                          }
                          showTooltip(event, d);
                          showDeviceOverlay(d);
                          event.stopPropagation();
                        })
                        .on("mouseover", showTooltip)
                        .on("mouseout", hideTooltip);

                      // Pulsing alert halos (rendered behind node)
                      nodeEnter.append("circle")
                        .attr("class", d => {
                          const hasAlert = d.status === "THREAT_DETECTED" || d.status === "BLOCKED" || d.status === "OFFLINE" || d.latency > 100;
                          return hasAlert ? "pulsing-halo" : "";
                        })
                        .attr("r", d => {
                          if (d.type === "ROUTER") return 24;
                          if (d.type === "SWITCH") return 20;
                          return 16;
                        })
                        .attr("stroke", d => {
                          if (d.status === "THREAT_DETECTED" || d.status === "BLOCKED") return "#f43f5e";
                          if (d.status === "OFFLINE") return "#ef4444";
                          if (d.latency > 100) return "#fbbf24";
                          return "none";
                        })
                        .attr("stroke-width", d => {
                          const hasAlert = d.status === "THREAT_DETECTED" || d.status === "BLOCKED" || d.status === "OFFLINE" || d.latency > 100;
                          return hasAlert ? 2 : 0;
                        })
                        .attr("fill", "none");

                      // Node background circles
                      nodeEnter.append("circle")
                        .attr("class", "node")
                        .attr("r", d => {
                          if (d.type === "ROUTER") return 24;
                          if (d.type === "SWITCH") return 20;
                          return 16;
                        })
                        .attr("fill", "#0f172a")
                        .attr("stroke", d => {
                          if (d.status === "THREAT_DETECTED") return "#f43f5e";
                          if (d.status === "BLOCKED") return "#ef4444";
                          if (d.status === "OFFLINE") return "#64748b";
                          return getVlanColor(d.vlanId);
                        })
                        .attr("stroke-width", d => {
                          if (d.type === "ROUTER" || d.type === "SWITCH") return 3;
                          return 2;
                        });

                      // Node Icons (abbreviated characters)
                      nodeEnter.append("text")
                        .attr("dy", ".3em")
                        .style("text-anchor", "middle")
                        .style("font-size", d => {
                          if (d.type === "ROUTER" || d.type === "SWITCH") return "10px";
                          return "8px";
                        })
                        .style("font-weight", "bold")
                        .style("fill", "#cbd5e1")
                        .text(d => {
                          if (d.type === "ROUTER") return "RTR";
                          if (d.type === "SWITCH") return "SW";
                          if (d.type === "PC") return "PC";
                          if (d.type === "SERVER") return "SRV";
                          if (d.type === "FIREWALL") return "FW";
                          if (d.type === "LOAD_BALANCER") return "LB";
                          if (d.type === "TERMINAL") return "ATM";
                          if (d.type === "GATEWAY") return "GW";
                          if (d.type === "SMART_DEVICE") return "IoT";
                          return d.type.substring(0, 3);
                        });

                      // Device IP or Label under node
                      nodeEnter.append("text")
                        .attr("class", "label")
                        .attr("y", d => {
                          if (d.type === "ROUTER") return 38;
                          if (d.type === "SWITCH") return 34;
                          return 30;
                        })
                        .text(d => d.name);

                      nodeEnter.append("text")
                        .attr("class", "subtext")
                        .attr("y", d => {
                          if (d.type === "ROUTER") return 48;
                          if (d.type === "SWITCH") return 44;
                          return 39;
                        })
                        .text(d => d.ip);

                      const allNodes = nodeEnter.merge(node);
                      
                      allNodes.selectAll(".node")
                        .attr("stroke", d => {
                          if (d.status === "THREAT_DETECTED") return "#f43f5e";
                          if (d.status === "BLOCKED") return "#ef4444";
                          if (d.status === "OFFLINE") return "#64748b";
                          if (window.activePath && window.activePath.includes(d.id)) {
                            return "#eab308";
                          }
                          return getVlanColor(d.vlanId);
                        })
                        .attr("stroke-width", d => {
                          if (window.activePath && window.activePath.includes(d.id)) {
                            return 4;
                          }
                          if (d.type === "ROUTER" || d.type === "SWITCH") return 3;
                          return 2;
                        });

                      // Re-run simulation
                      simulation.nodes(nodes);
                      simulation.force("link").links(links);
                      simulation.alpha(1).restart();

                      // Tick listener
                      simulation.on("tick", () => {
                        allLinks
                          .attr("x1", d => d.source.x)
                          .attr("y1", d => d.source.y)
                          .attr("x2", d => d.target.x)
                          .attr("y2", d => d.target.y);

                        allNodes
                          .attr("transform", d => `translate(${'$'}{d.x}, ${'$'}{d.y})`);

                        drawHulls();
                      });
                    }

                    function drawHulls() {
                      const groups = d3.groups(rawNodes, d => d.vlanId);
                      
                      const hullData = groups.map(([vlanId, vlanNodes]) => {
                        if (vlanNodes.length < 3) return null;
                        const points = vlanNodes.map(n => [n.x, n.y]);
                        const hull = d3.polygonHull(points);
                        return { vlanId, hull };
                      }).filter(h => h !== null);

                      const hull = hullGroup.selectAll(".hull")
                        .data(hullData, d => d.vlanId);

                      hull.exit().remove();

                      const hullEnter = hull.enter().append("path")
                        .attr("class", "hull")
                        .attr("fill", d => getVlanColor(d.vlanId))
                        .attr("stroke", d => getVlanColor(d.vlanId));

                      hullEnter.merge(hull)
                        .attr("d", d => {
                          return "M" + d.hull.join("L") + "Z";
                        });
                    }

                    function reheatSimulation() {
                      simulation.alpha(1).restart();
                    }

                    function dragstarted(event, d) {
                      if (!event.active) simulation.alphaTarget(0.3).restart();
                      d.fx = d.x;
                      d.fy = d.y;
                    }

                    function dragged(event, d) {
                      d.fx = event.x;
                      d.fy = event.y;
                    }

                    function dragended(event, d) {
                      if (!event.active) simulation.alphaTarget(0);
                      d.fx = null;
                      d.fy = null;
                    }

                    const tooltip = d3.select("#tooltip");

                    function showTooltip(event, d) {
                      const statusText = d.status === "THREAT_DETECTED" ? "INTRUSION WARNING" : d.status;
                      tooltip.transition().duration(100).style("opacity", 1);
                      tooltip.html(`
                        <div class="tooltip-title">${'$'}{d.name}</div>
                        <div><strong>Type:</strong> ${'$'}{d.label}</div>
                        <div><strong>IP:</strong> ${'$'}{d.ip}</div>
                        <div><strong>MAC:</strong> ${'$'}{d.mac}</div>
                        <div><strong>VLAN:</strong> Segment ${'$'}{d.vlanId}</div>
                        <div><strong>Port:</strong> FastEthernet ${'$'}{d.port}</div>
                        <div><strong>Bandwidth:</strong> ${'$'}{d.bandwidth} Kbps</div>
                        <div><strong>Latency:</strong> ${'$'}{d.latency.toFixed(2)} ms</div>
                        <div style="margin-top: 6px; font-weight: bold; color: ${'$'}{d.status === 'ONLINE' ? '#10b981' : '#f43f5e'}">Status: ${'$'}{statusText}</div>
                      `)
                      .style("left", (event.pageX + 10) + "px")
                      .style("top", (event.pageY - 10) + "px");
                    }

                    function hideTooltip() {
                      tooltip.transition().duration(100).style("opacity", 0);
                    }

                    function showDeviceOverlay(d) {
                      const panel = d3.select("#device-overlay");
                      
                      d3.select("#overlay-device-name").text(d.name + " (" + d.label + ")");
                      
                      const badge = d3.select("#overlay-device-status-badge");
                      let statusBg, statusColor, statusText;
                      if (d.status === "ONLINE") {
                        statusBg = "rgba(16, 185, 129, 0.15)";
                        statusColor = "#10b981";
                        statusText = "ACTIVE / SECURE";
                      } else if (d.status === "THREAT_DETECTED") {
                        statusBg = "rgba(244, 63, 94, 0.15)";
                        statusColor = "#f43f5e";
                        statusText = "INTRUSION THREAT";
                      } else if (d.status === "BLOCKED") {
                        statusBg = "rgba(239, 68, 68, 0.15)";
                        statusColor = "#ef4444";
                        statusText = "ADMIN BLOCKED";
                      } else {
                        statusBg = "rgba(100, 116, 139, 0.15)";
                        statusColor = "#64748b";
                        statusText = "OFFLINE / DISCONNECTED";
                      }
                      badge.style("background", statusBg)
                           .style("color", statusColor)
                           .text(statusText);
                           
                      const ipRoot = d.ip.substring(0, d.ip.lastIndexOf('.')) || "192.168.1";
                      const gateway = ipRoot + ".1";
                      const subnet = d.vlanId === 10 ? "255.255.255.0" : (d.vlanId === 20 ? "255.255.255.128" : "255.255.255.192");
                      const dns = "8.8.8.8";
                      
                      d3.select("#overlay-ip-config").html(`
                        <div class="overlay-item"><strong>IPv4 Address</strong> <span>${'$'}{d.ip}</span></div>
                        <div class="overlay-item"><strong>Subnet Mask</strong> <span>${'$'}{subnet}</span></div>
                        <div class="overlay-item"><strong>Def Gateway</strong> <span>${'$'}{gateway}</span></div>
                        <div class="overlay-item"><strong>Primary DNS</strong> <span>${'$'}{dns}</span></div>
                        <div class="overlay-item"><strong>MAC Address</strong> <span style="font-size: 8px;">${'$'}{d.mac}</span></div>
                      `);
                      
                      const lineState = (d.status === "ONLINE" || d.status === "THREAT_DETECTED") ? "UP" : "DOWN";
                      const speed = (d.type === "ROUTER" || d.type === "SWITCH") ? "1.0 Gbps (Gigabit)" : "100 Mbps (Fast)";
                      const duplex = "Full-duplex";
                      
                      d3.select("#overlay-port-status").html(`
                        <div class="overlay-item"><strong>Interface</strong> <span>Fa0/${'$'}{d.port}</span></div>
                        <div class="overlay-item"><strong>Port Link</strong> <span style="color: ${'$'}{lineState === 'UP' ? '#10b981' : '#ef4444'};">${'$'}{lineState}</span></div>
                        <div class="overlay-item"><strong>Protocol</strong> <span style="color: ${'$'}{lineState === 'UP' ? '#10b981' : '#ef4444'};">${'$'}{lineState}</span></div>
                        <div class="overlay-item"><strong>Link Speed</strong> <span>${'$'}{speed}</span></div>
                        <div class="overlay-item"><strong>Duplex Mode</strong> <span>${'$'}{duplex}</span></div>
                      `);
                      
                      const pingColor = d.latency > 100 ? "#fbbf24" : (d.status === "ONLINE" ? "#10b981" : "#f43f5e");
                      d3.select("#overlay-metrics").html(`
                        <div class="overlay-section">
                          <div class="overlay-section-title">Bandwidth Allocation</div>
                          <div class="overlay-item"><strong>Port Cap</strong> <span>${'$'}{d.bandwidth} Kbps</span></div>
                          <div class="overlay-item"><strong>Utilization</strong> <span>${'$'}{d.status === 'ONLINE' ? '12.4%' : '0.0%'}</span></div>
                        </div>
                        <div class="overlay-section">
                          <div class="overlay-section-title">Latency Diagnostic</div>
                          <div class="overlay-item"><strong>Ping RTT</strong> <span style="color: ${'$'}{pingColor}; font-weight: bold;">${'$'}{d.latency.toFixed(2)} ms</span></div>
                          <div class="overlay-item"><strong>Jitter</strong> <span style="color: ${'$'}{pingColor};">${'$'}{(d.latency * 0.1).toFixed(2)} ms</span></div>
                        </div>
                      `);
                      
                      d3.select("#overlay-ping-btn").on("click", () => {
                        if (window.AndroidInterface) {
                          window.AndroidInterface.onDeviceClick(d.id);
                        }
                      });
                      
                      panel.classed("active", true);
                    }
                    
                    function hideDeviceOverlay() {
                      d3.select("#device-overlay").classed("active", false);
                    }

                    // Initial load
                    updateTopology($nodesJson, $linksJson, $vlansJson, $optimalPathJson);

                    // Particle animator timer (60fps continuous flow)
                    d3.timer(() => {
                      particles.forEach(p => {
                        p.progress += p.speed;
                        if (p.progress >= 1) {
                          p.progress = 0;
                        }
                        
                        const source = p.link.source;
                        const target = p.link.target;
                        
                        if (source && target && typeof source === 'object' && typeof target === 'object') {
                          p.x = source.x + (target.x - source.x) * p.progress;
                          p.y = source.y + (target.y - source.y) * p.progress;
                        }
                      });
                      
                      particleGroup.selectAll(".particle")
                        .attr("cx", d => d.x || 0)
                        .attr("cy", d => d.y || 0);
                    });
                  </script>
                </body>
                </html>
            """.trimIndent()

            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("d3_topology_webview"),
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                        }
                        webViewClient = WebViewClient()
                        addJavascriptInterface(
                            WebAppInterface(devices, onDeviceClick),
                            "AndroidInterface"
                        )
                        loadDataWithBaseURL("https://cdnjs.cloudflare.com", htmlContent, "text/html", "UTF-8", null)
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL("https://cdnjs.cloudflare.com", htmlContent, "text/html", "UTF-8", null)
                }
            )
        }

        // --- ACTIVE CCNA WORKSPACE TABS INTERACTIVE ENGINE ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
            border = BorderStroke(1.5.dp, Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Multi-Tab Selection Pill Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("CLI Monitor", "Path Optimizer", "SNMPv3 Setup", "CIS Audit").forEach { tab ->
                        val active = activeWorkspaceTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) Color(0xFF1E293B) else Color.Transparent)
                                .clickable { activeWorkspaceTab = tab }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Render Content Based on Active Workspace Tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (activeWorkspaceTab) {
                        "CLI Monitor" -> {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color(0xFF22C55E), CircleShape)
                                        )
                                        Text(
                                            text = "AI RUNTIME CLI MONITOR",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF22C55E)
                                        )
                                    }
                                    Text(
                                        text = "LIVE SHELL FEED",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .background(Color(0xFF030712), RoundedCornerShape(10.dp))
                                        .padding(8.dp)
                                ) {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(terminalLogs) { log ->
                                            Text(
                                                text = log,
                                                color = if (log.startsWith(">")) Color(0xFF22C55E) else if (log.contains("!!!")) Color(0xFFEF4444) else Color(0xFF94A3B8),
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                lineHeight = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "Path Optimizer" -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "ROUTING PATH OPTIMIZER & BOTTLENECK FINDER",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE2E8F0)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Source Dropdown Selection Box
                                    var showSrcSelect by remember { mutableStateOf(false) }
                                    val srcDeviceName = devices.firstOrNull { it.id == selectedSourceId }?.name ?: "Select Source"
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Source Node:", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(36.dp)
                                                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                                .clickable { showSrcSelect = !showSrcSelect }
                                                .padding(horizontal = 8.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(srcDeviceName, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        if (showSrcSelect) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 120.dp)
                                                    .background(Color(0xFF0F172A))
                                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(4.dp))
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(4.dp)
                                            ) {
                                                Column {
                                                    devices.forEach { dev ->
                                                        Text(
                                                            text = dev.name,
                                                            fontSize = 10.sp,
                                                            color = if (selectedSourceId == dev.id) Color(0xFF38BDF8) else Color.White,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    viewModel.selectSourceNode(dev.id)
                                                                    showSrcSelect = false
                                                                }
                                                                .padding(vertical = 6.dp, horizontal = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Destination Dropdown Selection Box
                                    var showDstSelect by remember { mutableStateOf(false) }
                                    val dstDeviceName = devices.firstOrNull { it.id == selectedDestId }?.name ?: "Select Destination"
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Destination Node:", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(36.dp)
                                                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                                .clickable { showDstSelect = !showDstSelect }
                                                .padding(horizontal = 8.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(dstDeviceName, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        if (showDstSelect) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 120.dp)
                                                    .background(Color(0xFF0F172A))
                                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(4.dp))
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(4.dp)
                                            ) {
                                                Column {
                                                    devices.forEach { dev ->
                                                        Text(
                                                            text = dev.name,
                                                            fontSize = 10.sp,
                                                            color = if (selectedDestId == dev.id) Color(0xFF38BDF8) else Color.White,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    viewModel.selectDestNode(dev.id)
                                                                    showDstSelect = false
                                                                }
                                                                .padding(vertical = 6.dp, horizontal = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (optimalPath.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .background(Color(0xFF030712), RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Select a valid source and destination node above to highlight the path and perform bottleneck checks.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Path Hops List
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Color(0xFF030712), RoundedCornerShape(10.dp))
                                                .padding(8.dp)
                                        ) {
                                            Text("OPTIMIZED PATH HOPS", fontSize = 9.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                            LazyColumn(
                                                modifier = Modifier.fillMaxSize().padding(top = 4.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                items(optimalPath.size) { index ->
                                                    val devId = optimalPath[index]
                                                    val dev = devices.firstOrNull { it.id == devId }
                                                    if (dev != null) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = "[H${index + 1}] ",
                                                                color = Color(0xFF38BDF8),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                fontFamily = FontFamily.Monospace
                                                            )
                                                            Text(
                                                                text = dev.name,
                                                                color = Color.White,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "(${dev.ipAddress})",
                                                                color = Color(0xFF64748B),
                                                                fontSize = 8.sp,
                                                                fontFamily = FontFamily.Monospace
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Path Bottlenecks List
                                        Column(
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .background(Color(0xFF030712), RoundedCornerShape(10.dp))
                                                .padding(8.dp)
                                        ) {
                                            Text("BOTTLENECK ANALYSIS", fontSize = 9.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                            if (pathBottlenecks.isEmpty()) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "✓ Path optimal.\nAll segments running at gigabit speeds with low congestion.",
                                                        fontSize = 9.sp,
                                                        color = Color(0xFF10B981),
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                        lineHeight = 12.sp
                                                    )
                                                }
                                            } else {
                                                LazyColumn(
                                                    modifier = Modifier.fillMaxSize().padding(top = 4.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    items(pathBottlenecks) { warn ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                            verticalAlignment = Alignment.Top
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Warning,
                                                                contentDescription = "Warning",
                                                                tint = Color(0xFFF59E0B),
                                                                modifier = Modifier.size(11.dp).padding(top = 1.dp)
                                                            )
                                                            Text(
                                                                text = warn,
                                                                color = Color(0xFFCBD5E1),
                                                                fontSize = 9.sp,
                                                                lineHeight = 12.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "SNMPv3 Setup" -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SNMPv3 REMOTE MONITORING PROFILE",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE2E8F0)
                                    )
                                    if (isSnmpConfigured) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("DEPLOYED", color = Color(0xFF10B981), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .background(Color(0xFF030712), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Params Details
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Group Name:", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                            Text(snmpGroupName, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("User Name:", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                            Text(snmpUsername, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Auth Engine:", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                            Text("SHA Crypto", fontSize = 9.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                        }
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Auth Pass:", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                            Text(if (snmpAuthPass.isEmpty()) "[Not Gen]" else snmpAuthPass, fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                                        }
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Priv Engine:", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                            Text("AES-128", fontSize = 9.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                        }
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Priv Pass:", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                            Text(if (snmpPrivPass.isEmpty()) "[Not Gen]" else snmpPrivPass, fontSize = 9.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                                        }
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("Trap Target:", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                            Text(snmpTrapReceiver, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Action button and overview
                                    Column(
                                        modifier = Modifier.weight(1.1f),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Automatically configure secure SNMPv3 encryption profiles on all managed switches and routers to enhance remote CCNA logging.",
                                            fontSize = 9.sp,
                                            color = Color(0xFF64748B),
                                            lineHeight = 12.sp
                                        )
                                        
                                        Button(
                                            onClick = { viewModel.generateAndApplySnmpV3() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(36.dp)
                                        ) {
                                            Text("AUTO-GENERATE & APPLY", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        "CIS Audit" -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CIS SECURITY AUDIT & HARDENING",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE2E8F0)
                                    )
                                    if (cisComplianceScore != null) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = if (cisComplianceScore == 100) Color(0xFF10B981).copy(alpha = 0.15f)
                                                            else if (cisComplianceScore!! >= 70) Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                            else Color(0xFFEF4444).copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "SCORE: $cisComplianceScore%",
                                                color = if (cisComplianceScore == 100) Color(0xFF10B981)
                                                        else if (cisComplianceScore!! >= 70) Color(0xFFF59E0B)
                                                        else Color(0xFFEF4444),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (cisComplianceScore == null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .background(Color(0xFF030712), RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "Perform an automated audit of the active Cisco configurations against CIS Benchmarks for Banking security compliance.",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                modifier = Modifier.padding(bottom = 12.dp),
                                                lineHeight = 13.sp
                                            )
                                            Button(
                                                onClick = { viewModel.runCisSecurityScan() },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                if (isCisScanning) {
                                                    Text("AUDITING CONFIGS...", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                                } else {
                                                    Text("RUN SECURITY SCAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Failed rules and recommendations
                                        Column(
                                            modifier = Modifier
                                                .weight(1.3f)
                                                .background(Color(0xFF030712), RoundedCornerShape(10.dp))
                                                .padding(8.dp)
                                        ) {
                                            Text("AUDIT RECOMMENDATIONS", fontSize = 9.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                            LazyColumn(
                                                modifier = Modifier.fillMaxSize().padding(top = 4.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                items(cisAuditResults) { audit ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalAlignment = Alignment.Top
                                                    ) {
                                                        Icon(
                                                            imageVector = if (audit.status) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                            contentDescription = if (audit.status) "Pass" else "Fail",
                                                            tint = if (audit.status) Color(0xFF10B981) else Color(0xFFEF4444),
                                                            modifier = Modifier.size(11.dp).padding(top = 1.dp)
                                                        )
                                                        Column {
                                                            Text(
                                                                text = "${audit.ruleId}: ${audit.title}",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (audit.status) Color(0xFFE2E8F0) else Color(0xFFEF4444)
                                                            )
                                                            if (!audit.status) {
                                                                Text(
                                                                    text = "Recom: ${audit.recommendation}",
                                                                    fontSize = 8.sp,
                                                                    color = Color(0xFF94A3B8),
                                                                    lineHeight = 11.sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Remediate button panel
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Color(0xFF030712), RoundedCornerShape(10.dp))
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (cisComplianceScore == 100) "✓ Network configurations are fully secure & compliant with CIS Banking Standards!"
                                                       else "Non-compliant elements detected. Deploy automated remediation to re-secure ports, encrypt passwords & block vulnerable routes.",
                                                fontSize = 9.sp,
                                                color = if (cisComplianceScore == 100) Color(0xFF10B981) else Color(0xFF94A3B8),
                                                lineHeight = 12.sp
                                            )

                                            if (cisComplianceScore!! < 100) {
                                                Button(
                                                    onClick = { viewModel.remediateCisCompliance() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                                ) {
                                                    Text("AUTO-REMEDIATE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(36.dp)
                                                        .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("ALL SECURE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopologyNode(
    name: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isCore: Boolean,
    status: DeviceStatus,
    onClick: () -> Unit
) {
    val borderColor = if (status == DeviceStatus.ONLINE) BluePrimary else ErrorRed
    
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isCore) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = NetSurface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(BlueSecondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = BluePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = NetOnBackground
                )
                Text(
                    text = subtext,
                    fontSize = 10.sp,
                    color = NetSubtext
                )
            }
        }
    }
}

// ==========================================
// TAB: AI ENGINE & WIZARDS
// ==========================================
@Composable
fun AIWizardTab(
    viewModel: NetworkViewModel,
    isDeployingAi: Boolean,
    networkOverview: String,
    activeCiscoConfig: String,
    vlans: List<VlanInfo>
) {
    var prompt by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Template Selector Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NetSurface),
            border = BorderStroke(1.dp, NetOutline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Guided Enterprise Deployments",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueOnSecondaryContainer
                )
                Text(
                    text = "Select a best-practice security model template, or prompt the AI engineer to customize custom VLAN segments and security filters.",
                    fontSize = 11.sp,
                    color = NetSubtext,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TemplateButton(
                        label = "Secure Bank",
                        onClick = {
                            prompt = "Configure secure bank network with teller PCs, surveillance cameras, printers and backend database servers on isolated VLANs"
                            viewModel.deployAiNetwork(prompt)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TemplateButton(
                        label = "Corporate Office",
                        onClick = {
                            prompt = "Configure multi-department corporate network with dynamic DHCP routing, static printers mapping, and isolated servers vault"
                            viewModel.deployAiNetwork(prompt)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TemplateButton(
                        label = "Guest Wi-Fi",
                        onClick = {
                            prompt = "Configure a dynamic sandboxed Guest Wi-Fi hotspot portal with isolated wireless clients, HTTP/HTTPS port limitation and short leases"
                            viewModel.deployAiNetwork(prompt)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Prompt input box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NetSurface),
            border = BorderStroke(1.dp, NetOutline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Request Custom Configuration",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NetOnBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("e.g. configure secure bank with 20 PCs, 5 printers, 10 cameras", fontSize = 13.sp) },
                    modifier = Modifier
                        .testTag("ai_prompt_input")
                        .fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = NetOutline,
                        cursorColor = BluePrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (prompt.isNotBlank()) {
                            viewModel.deployAiNetwork(prompt)
                        }
                    },
                    modifier = Modifier
                        .testTag("deploy_with_ai_button")
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isDeployingAi
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDeployingAi) {
                            Text("DEPLOYING INFRASTRUCTURE...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DEPLOY WITH NETARCHITECT AI", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isDeployingAi) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = BluePrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NetArchitect AI is assembling CCNA IOS scripts, calculating VLSM subnets, and enforcing ACL compliance checks...",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Overview / Result Panel
        if (activeCiscoConfig.isNotBlank() && !isDeployingAi) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NetSurface, RoundedCornerShape(24.dp))
                    .border(1.dp, NetOutline, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "AI Design Architecture Overview",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueOnSecondaryContainer
                )
                Text(
                    text = networkOverview,
                    fontSize = 13.sp,
                    color = NetOnBackground,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cisco IOS Running Config",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NetOnBackground
                    )
                    Text(
                        text = "COPY CONFIG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        modifier = Modifier
                            .clickable {
                                clipboardManager.setText(AnnotatedString(activeCiscoConfig))
                            }
                            .padding(4.dp)
                    )
                }

                // Cisco code viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = activeCiscoConfig,
                        color = Color(0xFF38BDF8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                // Save Project Section
                var saveName by remember { mutableStateOf("") }
                var saveDesc by remember { mutableStateOf("") }
                var savedSuccess by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BlueSecondaryContainer, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Save Design to Local Audit Database",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BlueOnSecondaryContainer
                    )
                    OutlinedTextField(
                        value = saveName,
                        onValueChange = { saveName = it },
                        placeholder = { Text("Project Name, e.g. Vault Segment v1", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = NetOutline
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = saveDesc,
                        onValueChange = { saveDesc = it },
                        placeholder = { Text("Short description", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = NetOutline
                        )
                    )
                    Button(
                        onClick = {
                            if (saveName.isNotBlank()) {
                                viewModel.saveCurrentDesign(saveName, saveDesc)
                                savedSuccess = true
                                saveName = ""
                                saveDesc = ""
                            }
                        },
                        modifier = Modifier
                            .testTag("save_project_button")
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("SAVE DESIGN TO DATABASE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (savedSuccess) {
                        Text(
                            text = "Project design archived successfully!",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(BlueSecondaryContainer, RoundedCornerShape(14.dp))
            .border(1.dp, BluePrimaryContainer, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BlueOnSecondaryContainer
        )
    }
}

// ==========================================
// TAB: MANUAL CISCO SHELL CONSOLE
// ==========================================
@Composable
fun CLIConsoleTab(
    viewModel: NetworkViewModel
) {
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    var consoleInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll terminal to bottom when log updates
    LaunchedEffect(terminalLogs.size) {
        if (terminalLogs.isNotEmpty()) {
            listState.animateScrollToItem(terminalLogs.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Explanatory CCNA card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NetSurface),
            border = BorderStroke(1.dp, NetOutline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Manual Switch & Router Shell",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueOnSecondaryContainer
                )
                Text(
                    text = "Simulated manual override. Advanced engineers can issue Cisco command segments directly. Type 'help' to start diagnostics.",
                    fontSize = 11.sp,
                    color = NetSubtext,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Terminal view
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF020617), RoundedCornerShape(20.dp))
                .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(terminalLogs) { log ->
                    Text(
                        text = log,
                        color = if (log.startsWith(">")) Color(0xFF22C55E) else if (log.contains("!!!")) Color(0xFFEF4444) else Color(0xFFCBD5E1),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Terminal Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = consoleInput,
                onValueChange = { consoleInput = it },
                modifier = Modifier
                    .testTag("terminal_cli_input")
                    .weight(1f),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = NetOnBackground),
                placeholder = { Text("Cisco commands, e.g. show ip int brief", fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (consoleInput.isNotBlank()) {
                        viewModel.executeConsoleCommand(consoleInput)
                        consoleInput = ""
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = NetOutline,
                    cursorColor = BluePrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (consoleInput.isNotBlank()) {
                        viewModel.executeConsoleCommand(consoleInput)
                        consoleInput = ""
                    }
                },
                modifier = Modifier
                    .testTag("execute_cli_button")
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Run command",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ==========================================
// COMPONENT: DEVICE INSPECTOR SHEET
// ==========================================
@Composable
fun DeviceInspectorSheet(
    device: NetworkDevice,
    onPingRequest: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = device.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueOnSecondaryContainer
                )
            }
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Sheet")
            }
        }

        // Information Grid Table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InspectorRow(label = "Device Role", value = device.type.label)
            InspectorRow(label = "IP Address", value = device.ipAddress)
            InspectorRow(label = "MAC Address", value = device.macAddress)
            InspectorRow(label = "Switch Port Interface", value = device.interfacePort.ifEmpty { "Fa0/3" })
            InspectorRow(label = "VLAN Allocation", value = if (device.vlanId > 1) "VLAN ${device.vlanId}" else "Management (VLAN 1)")
            InspectorRow(label = "Addressing Mode", value = if (device.isDhcp) "Dynamic (DHCP)" else "Static IP assignment")
            InspectorRow(label = "Real-time Port Bandwidth", value = "${device.bandwidthKbps} Kbps")
            InspectorRow(label = "Active Link Latency", value = "${String.format("%.2f", device.latencyMs)} ms")
            InspectorRow(label = "Connection Integrity", value = device.status.label)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onPingRequest(device.ipAddress) },
                modifier = Modifier
                    .testTag("ping_device_action")
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("PING & TRACE ROUTE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary),
                border = BorderStroke(1.dp, BluePrimary)
            ) {
                Text("CLOSE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun InspectorRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = NetSubtext, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 12.sp, color = NetOnBackground, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

// ==========================================
// COMPONENT: NAVIGATION BAR
// ==========================================
@Composable
fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, NetOutline)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "Console",
                icon = Icons.Default.Terminal,
                selected = selectedTab == "Console",
                onClick = { onTabSelected("Console") }
            )
            BottomNavItem(
                label = "Topology",
                icon = Icons.Default.AccountTree,
                selected = selectedTab == "Topology",
                onClick = { onTabSelected("Topology") }
            )
            BottomNavItem(
                label = "Bank Config",
                icon = Icons.Default.AccountTree,
                selected = selectedTab == "Bank Config",
                onClick = { onTabSelected("Bank Config") }
            )
            BottomNavItem(
                label = "AI Wizard",
                icon = Icons.Default.Settings,
                selected = selectedTab == "AI Wizard",
                onClick = { onTabSelected("AI Wizard") }
            )
            BottomNavItem(
                label = "CLI Console",
                icon = Icons.Default.Terminal,
                selected = selectedTab == "CLI Console",
                onClick = { onTabSelected("CLI Console") }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(if (selected) BluePrimaryContainer else Color.Transparent, shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) BlueOnPrimaryContainer else NetSubtext,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) BlueOnPrimaryContainer else NetSubtext
        )
    }
}

@Composable
fun BankConfigTab(viewModel: NetworkViewModel) {
    val depts by viewModel.bankDepartments.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState()
    
    var locationName by remember { mutableStateOf("Chicago Downtown Branch") }
    var expandedDeptId by remember { mutableStateOf<Int?>(null) }
    var deploySuccess by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    
    // Filter out only bank location projects in the history view
    val bankProjects = savedProjects.filter { it.name.startsWith("Bank Network: ") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NetSurface),
                border = BorderStroke(1.dp, NetOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bank Network Configurator",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueOnSecondaryContainer
                    )
                    Text(
                        text = "Design, compile, and deploy compliant standard banking networks. Engineers can specify custom device counts per department, compile full CCNA running-configs, and save/load locations.",
                        fontSize = 11.sp,
                        color = NetSubtext,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        // LOCATION INPUT & PRESETS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NetOutline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Location Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BlueOnSecondaryContainer
                    )
                    
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Branch Location Name") },
                        modifier = Modifier.fillMaxWidth().testTag("bank_location_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = NetOutline
                        )
                    )
                    
                    // Presets Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // Apply Corporate presets
                                depts.forEach { dept ->
                                    viewModel.updateBankDeptDeviceCount(dept.id, "terminal", 0)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "workstation", 8)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "server", 2)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "smart", 2)
                                }
                                viewModel.updateBankDeptDeviceCount(0, "terminal", 10) // Retail Tellers
                                viewModel.updateBankDeptDeviceCount(8, "server", 10)  // IT Central database
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary),
                            border = BorderStroke(1.dp, BluePrimary),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("HQ Preset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                // Apply Small Local Branch presets
                                depts.forEach { dept ->
                                    val isCore = dept.id in listOf(0, 8, 10, 14) // Retail, IT, Cust Service, E-banking
                                    viewModel.updateBankDeptDeviceCount(dept.id, "terminal", if (isCore) 2 else 0)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "workstation", if (isCore) 3 else 1)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "server", if (dept.id == 8) 2 else 0)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "smart", 1)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary),
                            border = BorderStroke(1.dp, BluePrimary),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("Branch Preset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        OutlinedButton(
                            onClick = {
                                // Reset all counts to 0 (except routers/switches)
                                depts.forEach { dept ->
                                    viewModel.updateBankDeptDeviceCount(dept.id, "terminal", 0)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "workstation", 0)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "server", 0)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "smart", 0)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "router", 1)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "switch", 1)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "gateway", 0)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "firewall", 0)
                                    viewModel.updateBankDeptDeviceCount(dept.id, "loadBalancer", 0)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = BorderStroke(1.dp, ErrorRed),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        // DEPLOY & SAVE BUTTONS CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BlueSecondaryContainer),
                border = BorderStroke(1.dp, BluePrimaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Compile & Action Center",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BlueOnSecondaryContainer
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (locationName.isNotBlank()) {
                                    viewModel.deployBankNetwork(locationName)
                                    deploySuccess = true
                                    saveSuccess = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(46.dp).testTag("compile_bank_network_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("COMPILE & DEPLOY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = {
                                if (locationName.isNotBlank()) {
                                    viewModel.saveBankLocationNetwork(locationName)
                                    saveSuccess = true
                                    deploySuccess = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(46.dp).testTag("save_bank_network_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SAVE CONFIG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    if (deploySuccess) {
                        Text(
                            text = "Bank Network successfully deployed to active Topology!",
                            color = BlueOnPrimaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (saveSuccess) {
                        Text(
                            text = "Configuration copy saved to audit history!",
                            color = SuccessGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        
        // DEPARTMENTS LIST HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bank Departments Allocation",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueOnSecondaryContainer
                )
                Text(
                    text = "16 Departments",
                    fontSize = 12.sp,
                    color = NetSubtext
                )
            }
        }
        
        // 16 DEPARTMENTS RENDER
        items(depts) { dept ->
            val isExpanded = expandedDeptId == dept.id
            val totalCount = dept.totalDevices()
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, if (isExpanded) BluePrimary else NetOutline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🔹 ${dept.id + 1}. ${dept.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueOnSecondaryContainer
                            )
                            Text(
                                text = dept.function,
                                fontSize = 11.sp,
                                color = NetSubtext,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        
                        Text(
                            text = "$totalCount Devices",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalCount > 0) BluePrimary else NetSubtext,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { expandedDeptId = if (isExpanded) null else dept.id },
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isExpanded) BluePrimary else BlueSecondaryContainer),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (isExpanded) "Collapse" else "Configure Devices",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpanded) Color.White else BlueOnSecondaryContainer
                            )
                        }
                    }
                    
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Device Allocation Matrix",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = NetSubtext
                            )
                            
                            // Stepper rows
                            DeviceStepperRow(
                                label = "Customer Terminals (ATMs, Tellers)",
                                count = dept.customerTerminals,
                                onValueChange = { viewModel.updateBankDeptDeviceCount(dept.id, "terminal", it) }
                            )
                            DeviceStepperRow(
                                label = "Workstations & Laptops",
                                count = dept.workstations,
                                onValueChange = { viewModel.updateBankDeptDeviceCount(dept.id, "workstation", it) }
                            )
                            DeviceStepperRow(
                                label = "Enterprise Servers",
                                count = dept.servers,
                                onValueChange = { viewModel.updateBankDeptDeviceCount(dept.id, "server", it) }
                            )
                            DeviceStepperRow(
                                label = "Smart Devices (CCTV, locks, IoT)",
                                count = dept.smartDevices,
                                onValueChange = { viewModel.updateBankDeptDeviceCount(dept.id, "smart", it) }
                            )
                            DeviceStepperRow(
                                label = "WAN Routers",
                                count = dept.routers,
                                onValueChange = { viewModel.updateBankDeptDeviceCount(dept.id, "router", it) }
                            )
                            DeviceStepperRow(
                                label = "LAN Switches",
                                count = dept.switches,
                                onValueChange = { viewModel.updateBankDeptDeviceCount(dept.id, "switch", it) }
                            )
                            DeviceStepperRow(
                                label = "Modern-to-Legacy Gateways",
                                count = dept.gateways,
                                onValueChange = { viewModel.updateBankDeptDeviceCount(dept.id, "gateway", it) }
                            )
                            DeviceStepperRow(
                                label = "Security Firewalls",
                                count = dept.firewalls,
                                onValueChange = { viewModel.updateBankDeptDeviceCount(dept.id, "firewall", it) }
                            )
                            DeviceStepperRow(
                                label = "Load Balancers",
                                count = dept.loadBalancers,
                                onValueChange = { viewModel.updateBankDeptDeviceCount(dept.id, "loadBalancer", it) }
                            )
                        }
                    }
                }
            }
        }
        
        // SAVED LOCATIONS / HISTORY SECTION
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Saved Bank Network Installations",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueOnSecondaryContainer
                )
                
                if (bankProjects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.dp, NetOutline, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No saved locations found. Configure above and click 'SAVE CONFIG' to persist layouts.",
                            fontSize = 11.sp,
                            color = NetSubtext
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        bankProjects.forEach { project ->
                            val cleanName = project.name.replace("Bank Network: ", "")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(14.dp))
                                    .border(1.dp, NetOutline, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cleanName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BlueOnSecondaryContainer
                                    )
                                    Text(
                                        text = "Total Devices: ${project.deviceCount} | Saved on: ${
                                            java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(project.timestamp))
                                        }",
                                        fontSize = 10.sp,
                                        color = NetSubtext
                                    )
                                }
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.loadSavedBankNetwork(project)
                                            locationName = cleanName
                                            deploySuccess = true
                                            saveSuccess = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BlueSecondaryContainer),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("LOAD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueOnSecondaryContainer)
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.deleteProject(project.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFECEF)),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("DELETE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceStepperRow(
    label: String,
    count: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = NetOnBackground,
            modifier = Modifier.weight(1f)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, NetOutline, RoundedCornerShape(6.dp))
                    .clickable { onValueChange(count - 1) },
                contentAlignment = Alignment.Center
            ) {
                Text("-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NetOnBackground)
            }
            
            Text(
                text = count.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BlueOnSecondaryContainer,
                modifier = Modifier.width(20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, NetOutline, RoundedCornerShape(6.dp))
                    .clickable { onValueChange(count + 1) },
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NetOnBackground)
            }
        }
    }
}

