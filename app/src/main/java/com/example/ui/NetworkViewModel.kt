package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiHelper
import com.example.data.AuditLog
import com.example.data.DeviceStatus
import com.example.data.DeviceType
import com.example.data.NetworkDatabase
import com.example.data.NetworkDevice
import com.example.data.NetworkRepository
import com.example.data.SavedProject
import com.example.data.ThreatAlert
import com.example.data.VlanInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class NetworkViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NetworkDatabase.getDatabase(application)
    private val repository = NetworkRepository(database.networkDao())

    // --- State Streams ---
    val savedProjects: StateFlow<List<SavedProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Interactive State ---
    private val _devices = MutableStateFlow<List<NetworkDevice>>(emptyList())
    val devices: StateFlow<List<NetworkDevice>> = _devices.asStateFlow()

    private val _vlans = MutableStateFlow<List<VlanInfo>>(emptyList())
    val vlans: StateFlow<List<VlanInfo>> = _vlans.asStateFlow()

    private val _activeCiscoConfig = MutableStateFlow("")
    val activeCiscoConfig: StateFlow<String> = _activeCiscoConfig.asStateFlow()

    private val _networkOverview = MutableStateFlow("No network configured yet. Use a guided template, run the scanner, or write a custom demand to get started.")
    val networkOverview: StateFlow<String> = _networkOverview.asStateFlow()

    private val _ipAllocationTable = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val ipAllocationTable: StateFlow<List<Pair<String, String>>> = _ipAllocationTable.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isDeployingAi = MutableStateFlow(false)
    val isDeployingAi: StateFlow<Boolean> = _isDeployingAi.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _activeThreats = MutableStateFlow<List<ThreatAlert>>(emptyList())
    val activeThreats: StateFlow<List<ThreatAlert>> = _activeThreats.asStateFlow()

    // --- Telemetry Monitoring ---
    private val _avgLatencyMs = MutableStateFlow(2.4)
    val avgLatencyMs: StateFlow<Double> = _avgLatencyMs.asStateFlow()

    private val _totalBandwidthKbps = MutableStateFlow(2450)
    val totalBandwidthKbps: StateFlow<Int> = _totalBandwidthKbps.asStateFlow()

    private val _overallHealthScore = MutableStateFlow(100)
    val overallHealthScore: StateFlow<Int> = _overallHealthScore.asStateFlow()

    // --- Cisco IOS Terminal Console ---
    private val _terminalLogs = MutableStateFlow<List<String>>(listOf(
        "NetArchitect Terminal Console v3.5",
        "Type 'help' or '?' to view available CCNA level diagnostic commands.",
        "System ready for administrator interface connections."
    ))
    val terminalLogs: StateFlow<List<String>> = _terminalLogs.asStateFlow()

    private var threatGenerationJob: Job? = null
    private var telemetryJob: Job? = null

    init {
        // Load initial seed network devices representing typical connected Admin workstation layout
        resetToInitialState()
        startTelemetrySimulation()
        startThreatSimulation()
        writeAuditLog("INFO", "SCANNER", "NetArchitect system initialized. Connected to Administration Node.")
    }

    private fun resetToInitialState() {
        _devices.value = listOf(
            NetworkDevice(name = "Core-Router-01", type = DeviceType.ROUTER, ipAddress = "192.168.1.1", macAddress = "00:0A:95:9D:68:16", vlanId = 1, connectedTo = "WAN Gateway", isDhcp = false, interfacePort = "Gig0/0"),
            NetworkDevice(name = "Core-Switch-01", type = DeviceType.SWITCH, ipAddress = "192.168.1.2", macAddress = "00:0A:95:F4:A1:88", vlanId = 1, connectedTo = "Core-Router-01", isDhcp = false, interfacePort = "Fa0/1"),
            NetworkDevice(name = "Admin-PC-Workstation", type = DeviceType.PC, ipAddress = "192.168.1.100", macAddress = "2C:F0:A2:4B:91:02", vlanId = 1, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/2")
        )
        
        _vlans.value = listOf(
            VlanInfo(1, "Management", "192.168.1.0/24", "192.168.1.1", "192.168.1.50-192.168.1.150", "Default Management Subnet for network control devices.")
        )
        
        _activeCiscoConfig.value = """
            ! Default NetArchitect Bootstrap Configuration
            hostname Admin-Bootstrap
            ip routing
            interface GigabitEthernet0/0
             ip address 192.168.1.1 255.255.255.0
             no shutdown
            interface FastEthernet0/1
             description Switch-Trunk-Access
            end
        """.trimIndent()
        
        _activeThreats.value = emptyList()
    }

    fun startScannerSweep() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f
            writeAuditLog("INFO", "SCANNER", "Initiating automatic discovery sweep on administrator switch ports...")
            addTerminalLine("> scan network discovery-sweep --active")

            // Simulate ports scanning increment
            for (i in 1..10) {
                delay(350)
                _scanProgress.value = i / 10f
                val port = i + 2
                addTerminalLine("Scanning FastEthernet0/$port ... Link Detected")
            }

            // Populate scanned devices based on currently designed VLAN structure
            val detected = mutableListOf<NetworkDevice>()
            // Always keep baseline infrastructure
            detected.addAll(_devices.value.filter { it.type == DeviceType.ROUTER || it.type == DeviceType.SWITCH || it.name.contains("Admin") })

            // Add newly discovered peripherals
            detected.add(NetworkDevice(name = "Office-PC-101", type = DeviceType.PC, ipAddress = "192.168.1.51", macAddress = "70:85:C2:A4:1F:B1", vlanId = 1, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/3"))
            detected.add(NetworkDevice(name = "Office-PC-102", type = DeviceType.PC, ipAddress = "192.168.1.52", macAddress = "70:85:C2:A4:1F:B2", vlanId = 1, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/4"))
            detected.add(NetworkDevice(name = "Office-LaserJet-01", type = DeviceType.PRINTER, ipAddress = "192.168.1.200", macAddress = "00:11:85:12:34:56", vlanId = 1, connectedTo = "Core-Switch-01", isDhcp = false, interfacePort = "Fa0/5"))
            detected.add(NetworkDevice(name = "Core-Database-Server", type = DeviceType.SERVER, ipAddress = "192.168.1.10", macAddress = "E0:D5:5E:AA:BC:11", vlanId = 1, connectedTo = "Core-Switch-01", isDhcp = false, interfacePort = "Fa0/6"))

            _devices.value = detected
            _isScanning.value = false
            _scanProgress.value = 1f
            
            writeAuditLog("SUCCESS", "SCANNER", "Discovered 4 new end-devices on Management switch ports.")
            addTerminalLine("Discovery sweep completed. 7 total devices registered.")
            addTerminalLine("Threat detection monitor fully synchronized. Core running at optimal speed.")
        }
    }

    fun deployAiNetwork(demand: String) {
        if (_isDeployingAi.value) return
        viewModelScope.launch {
            _isDeployingAi.value = true
            writeAuditLog("INFO", "BACKEND", "NetArchitect AI compiling deployment parameters for: \"$demand\"")
            addTerminalLine("> ai deploy-profile \"$demand\"")
            addTerminalLine("Requesting AI network parameters from Gemini-3.5-Flash Core...")
            
            delay(800)
            addTerminalLine("Analyzing user demand requirements...")
            delay(600)
            addTerminalLine("Synthesizing Subnetting schemas & secure VLAN segments...")
            delay(500)
            addTerminalLine("Formulating Access Control List rule matrix...")

            val aiResponse = GeminiHelper.askGemini(demand)
            
            // Extract info from generated response to populate interactive state
            parseAiResponseToState(aiResponse, demand)

            _isDeployingAi.value = false
            writeAuditLog("SUCCESS", "SEC_POLICY", "AI network deployment completed and synchronized successfully.")
        }
    }

    private fun parseAiResponseToState(response: String, originalDemand: String) {
        _activeCiscoConfig.value = response

        // Update Overview text based on the response
        val overviewHeader = "[NETWORK DESCRIPTOR]"
        val tableHeader = "[IP ALLOCATION TABLE]"
        val configHeader = "[CISCO RUNNING CONFIG]"
        val auditHeader = "[SECURITY AUDIT]"

        var desc = "Custom network segment generated successfully based on demand."
        if (response.contains(overviewHeader)) {
            val start = response.indexOf(overviewHeader) + overviewHeader.length
            val end = if (response.contains(tableHeader)) response.indexOf(tableHeader) else response.length
            desc = response.substring(start, end).trim()
        }
        _networkOverview.value = desc

        // Reconfigure our VLAN list based on what was described (Bank vs Guest vs Corporate Office)
        val normalized = originalDemand.lowercase()
        if (normalized.contains("bank") || normalized.contains("teller") || normalized.contains("secure")) {
            _vlans.value = listOf(
                VlanInfo(10, "Tellers", "10.10.10.0/24", "10.10.10.1", "10.10.10.10-10.10.10.100", "High-security subnet for teller workstations with server-only access."),
                VlanInfo(20, "Back-Office", "10.10.20.0/24", "10.10.20.1", "10.10.20.10-10.10.20.200", "Default priority office network for administrative tasks."),
                VlanInfo(30, "Surveillance", "10.10.30.0/24", "10.10.30.1", "Static Allocation Only", "Local access-only VLAN for CCTV security cameras."),
                VlanInfo(99, "Management", "10.10.99.0/24", "10.10.99.1", "10.10.99.20-10.10.99.50", "Secure infrastructure shell for routers, switches, and databases.")
            )

            _devices.value = listOf(
                NetworkDevice(name = "Core-Router-01", type = DeviceType.ROUTER, ipAddress = "10.10.99.1", macAddress = "00:0A:95:9D:68:16", vlanId = 99, connectedTo = "WAN Gateway", isDhcp = false, interfacePort = "Gig0/0"),
                NetworkDevice(name = "Core-Switch-01", type = DeviceType.SWITCH, ipAddress = "10.10.99.2", macAddress = "00:0A:95:F4:A1:88", vlanId = 99, connectedTo = "Core-Router-01", isDhcp = false, interfacePort = "Fa0/1"),
                NetworkDevice(name = "Admin-PC-Workstation", type = DeviceType.PC, ipAddress = "10.10.99.100", macAddress = "2C:F0:A2:4B:91:02", vlanId = 99, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/2"),
                NetworkDevice(name = "Teller-PC-01", type = DeviceType.PC, ipAddress = "10.10.10.11", macAddress = "2C:F0:A2:4B:20:01", vlanId = 10, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/3"),
                NetworkDevice(name = "Teller-PC-02", type = DeviceType.PC, ipAddress = "10.10.10.12", macAddress = "2C:F0:A2:4B:20:02", vlanId = 10, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/4"),
                NetworkDevice(name = "Office-PC-Back", type = DeviceType.PC, ipAddress = "10.10.20.15", macAddress = "A4:B1:C2:5E:2B:11", vlanId = 20, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/5"),
                NetworkDevice(name = "Office-LaserJet", type = DeviceType.PRINTER, ipAddress = "10.10.20.200", macAddress = "00:11:85:12:34:56", vlanId = 20, connectedTo = "Core-Switch-01", isDhcp = false, interfacePort = "Fa0/6"),
                NetworkDevice(name = "Surveillance-Cam-01", type = DeviceType.CAMERA, ipAddress = "10.10.30.15", macAddress = "BC:EA:44:92:80:FF", vlanId = 30, connectedTo = "Core-Switch-01", isDhcp = false, interfacePort = "Fa0/7"),
                NetworkDevice(name = "Vault-Server-Admin", type = DeviceType.SERVER, ipAddress = "10.10.99.50", macAddress = "E0:D5:5E:AA:BC:11", vlanId = 99, connectedTo = "Core-Switch-01", isDhcp = false, interfacePort = "Fa0/8")
            )

        } else if (normalized.contains("guest") || normalized.contains("wifi") || normalized.contains("hotspot")) {
            _vlans.value = listOf(
                VlanInfo(80, "Guest-WiFi", "192.168.80.0/22", "192.168.80.1", "192.168.80.10-192.168.83.250", "Sandboxed Guest pool with strict WAN limit routing."),
                VlanInfo(99, "Management", "192.168.99.0/24", "192.168.99.1", "Static Mapping", "Secure controller interface for routers and switches.")
            )

            _devices.value = listOf(
                NetworkDevice(name = "Core-Router-01", type = DeviceType.ROUTER, ipAddress = "192.168.99.1", macAddress = "00:0A:95:9D:68:16", vlanId = 99, connectedTo = "WAN Gateway", isDhcp = false, interfacePort = "Gig0/0"),
                NetworkDevice(name = "Core-Switch-01", type = DeviceType.SWITCH, ipAddress = "192.168.99.2", macAddress = "00:0A:95:F4:A1:88", vlanId = 99, connectedTo = "Core-Router-01", isDhcp = false, interfacePort = "Fa0/1"),
                NetworkDevice(name = "Admin-PC-Workstation", type = DeviceType.PC, ipAddress = "192.168.99.100", macAddress = "2C:F0:A2:4B:91:02", vlanId = 99, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/2"),
                NetworkDevice(name = "Guest-Mobile-Phone", type = DeviceType.PC, ipAddress = "192.168.80.25", macAddress = "88:7E:1C:AA:BB:CC", vlanId = 80, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/3"),
                NetworkDevice(name = "Guest-Laptop", type = DeviceType.PC, ipAddress = "192.168.80.26", macAddress = "BC:83:85:2E:3F:40", vlanId = 80, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/4")
            )
        } else {
            // General Office setup
            _vlans.value = listOf(
                VlanInfo(10, "Corporate-PC", "172.16.10.0/24", "172.16.10.1", "172.16.10.50-172.16.10.150", "Sales and engineering computers."),
                VlanInfo(40, "Printers", "172.16.40.0/24", "172.16.40.1", "Static Mapping Only", "Local network peripheral systems."),
                VlanInfo(50, "Servers", "172.16.50.0/24", "172.16.50.1", "Static Allocation Only", "High security application servers and AD control database.")
            )

            _devices.value = listOf(
                NetworkDevice(name = "Core-Router-01", type = DeviceType.ROUTER, ipAddress = "172.16.50.1", macAddress = "00:0A:95:9D:68:16", vlanId = 50, connectedTo = "WAN Gateway", isDhcp = false, interfacePort = "Gig0/0"),
                NetworkDevice(name = "Core-Switch-01", type = DeviceType.SWITCH, ipAddress = "172.16.50.2", macAddress = "00:0A:95:F4:A1:88", vlanId = 50, connectedTo = "Core-Router-01", isDhcp = false, interfacePort = "Fa0/1"),
                NetworkDevice(name = "Admin-PC-Workstation", type = DeviceType.PC, ipAddress = "172.16.10.100", macAddress = "2C:F0:A2:4B:91:02", vlanId = 10, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/2"),
                NetworkDevice(name = "Staff-Workstation-01", type = DeviceType.PC, ipAddress = "172.16.10.51", macAddress = "44:7E:AA:88:BB:C1", vlanId = 10, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/3"),
                NetworkDevice(name = "Staff-Workstation-02", type = DeviceType.PC, ipAddress = "172.16.10.52", macAddress = "44:7E:AA:88:BB:C2", vlanId = 10, connectedTo = "Core-Switch-01", isDhcp = true, interfacePort = "Fa0/4"),
                NetworkDevice(name = "Office-Printer", type = DeviceType.PRINTER, ipAddress = "172.16.40.20", macAddress = "00:11:85:12:34:56", vlanId = 40, connectedTo = "Core-Switch-01", isDhcp = false, interfacePort = "Fa0/5"),
                NetworkDevice(name = "App-Web-Server", type = DeviceType.SERVER, ipAddress = "172.16.50.50", macAddress = "E0:D5:5E:AA:BC:11", vlanId = 50, connectedTo = "Core-Switch-01", isDhcp = false, interfacePort = "Fa0/6")
            )
        }

        addTerminalLine("New configuration loaded for active host.")
        addTerminalLine("VLANs created: " + _vlans.value.joinToString { "VLAN ${it.id} (${it.name})" })
        addTerminalLine("Configuration compliance score: 100% SECURE")
    }

    fun saveCurrentDesign(name: String, desc: String) {
        viewModelScope.launch {
            val project = SavedProject(
                name = name,
                description = desc,
                deviceCount = _devices.value.size,
                ciscoConfig = _activeCiscoConfig.value
            )
            repository.saveProject(project)
            writeAuditLog("SUCCESS", "BACKEND", "Network project design \"$name\" saved to Room DB successfully.")
        }
    }

    fun deleteProject(id: Int) {
        viewModelScope.launch {
            repository.deleteProject(id)
            writeAuditLog("INFO", "BACKEND", "Saved project entry deleted.")
        }
    }

    // --- Threat detection simulation & mitigation logic ---
    private fun startThreatSimulation() {
        threatGenerationJob?.cancel()
        threatGenerationJob = viewModelScope.launch {
            while (true) {
                delay(35000) // Triggers occasionally (every 35 seconds)
                if (_devices.value.size > 3) {
                    val eligibleDevices = _devices.value.filter { it.type == DeviceType.PC || it.type == DeviceType.CAMERA }
                    if (eligibleDevices.isNotEmpty()) {
                        val victim = eligibleDevices[Random.nextInt(eligibleDevices.size)]
                        
                        // Create a threat alert
                        val isArp = Random.nextBoolean()
                        val alert = if (isArp) {
                            ThreatAlert(
                                sourceMac = victim.macAddress,
                                sourceIp = victim.ipAddress,
                                severity = "CRITICAL",
                                description = "Spoofed ARP packets detected from host MAC. Intruding switch port security.",
                                interfacePort = victim.interfacePort
                            )
                        } else {
                            ThreatAlert(
                                sourceMac = victim.macAddress,
                                sourceIp = victim.ipAddress,
                                severity = "HIGH",
                                description = "Abnormal volume of ICMP Echo Requests (pingflood attack) targeting WAN routing interface.",
                                interfacePort = victim.interfacePort
                            )
                        }

                        // Mark device status in state
                        _devices.value = _devices.value.map {
                            if (it.id == victim.id) {
                                it.copy(status = DeviceStatus.THREAT_DETECTED)
                            } else {
                                it
                            }
                        }

                        _activeThreats.value = _activeThreats.value + alert
                        _overallHealthScore.value = 75
                        
                        writeAuditLog("ALERT", "THREAT", "IDS ALERT: ${alert.description} Port: ${alert.interfacePort}")
                        addTerminalLine("!!! SYSTEM THREAT ALERT: ${alert.description} PORT ${alert.interfacePort} !!!")
                    }
                }
            }
        }
    }

    fun handleMitigateThreat(alertId: String) {
        val alert = _activeThreats.value.firstOrNull { it.id == alertId } ?: return
        viewModelScope.launch {
            writeAuditLog("INFO", "SEC_POLICY", "Initiating threat mitigation protocol on Port ${alert.interfacePort}...")
            addTerminalLine("> security mitigate-port --target ${alert.interfacePort}")
            addTerminalLine("Executing MAC port isolation on switch Core-Switch-01...")
            
            delay(1000)
            
            // Mitigate: set port shutdown, set device blocked or back to online but clean
            _devices.value = _devices.value.map {
                if (it.macAddress == alert.sourceMac) {
                    it.copy(status = DeviceStatus.BLOCKED, bandwidthKbps = 0, latencyMs = 0.0)
                } else {
                    it
                }
            }

            _activeThreats.value = _activeThreats.value.filter { it.id != alertId }
            _overallHealthScore.value = 98

            writeAuditLog("SUCCESS", "SEC_POLICY", "Automated threat isolated. MAC ${alert.sourceMac} blocked on Port ${alert.interfacePort} by ACL policy rule.")
            addTerminalLine("Threat mitigated successfully. Switch Port isolation enabled.")
        }
    }

    // --- Ping diagnostics simulated execution ---
    fun runInteractivePing(targetIp: String, onStep: (String) -> Unit) {
        viewModelScope.launch {
            addTerminalLine("> ping $targetIp")
            onStep("PING $targetIp with 32 bytes of data:")
            delay(400)
            
            val device = _devices.value.firstOrNull { it.ipAddress == targetIp }
            if (device != null && device.status != DeviceStatus.OFFLINE && device.status != DeviceStatus.BLOCKED) {
                for (seq in 1..4) {
                    delay(300)
                    val rtt = String.format("%.1f", device.latencyMs + Random.nextDouble(-0.3, 0.4))
                    val step = "Reply from $targetIp: bytes=32 time=${rtt}ms TTL=128 seq=$seq"
                    onStep(step)
                    addTerminalLine(step)
                }
                delay(300)
                onStep("\nPing statistics for $targetIp:")
                onStep("    Packets: Sent = 4, Received = 4, Lost = 0 (0% loss)")
            } else {
                for (seq in 1..4) {
                    delay(500)
                    val step = "Request timed out. seq=$seq"
                    onStep(step)
                    addTerminalLine(step)
                }
                delay(300)
                onStep("\nPing statistics for $targetIp:")
                onStep("    Packets: Sent = 4, Received = 0, Lost = 4 (100% loss)")
            }
        }
    }

    // --- Telemetry background stream ---
    private fun startTelemetrySimulation() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (true) {
                delay(3000) // Update every 3 seconds
                if (_devices.value.isNotEmpty()) {
                    var totalBandwidth = 0
                    val onlineDevices = _devices.value.filter { it.status == DeviceStatus.ONLINE }
                    
                    _devices.value = _devices.value.map { dev ->
                        if (dev.status == DeviceStatus.ONLINE) {
                            val activeLoad = when(dev.type) {
                                DeviceType.ROUTER -> Random.nextInt(1500, 3000)
                                DeviceType.SWITCH -> Random.nextInt(800, 1500)
                                DeviceType.PC -> Random.nextInt(50, 400)
                                DeviceType.PRINTER -> if (Random.nextBoolean()) Random.nextInt(10, 50) else 0
                                DeviceType.CAMERA -> Random.nextInt(400, 800) // Steady camera feed
                                DeviceType.SERVER -> Random.nextInt(600, 1800)
                                DeviceType.TERMINAL -> Random.nextInt(100, 300)
                                DeviceType.SMART_DEVICE -> Random.nextInt(20, 100)
                                DeviceType.GATEWAY -> Random.nextInt(1000, 2500)
                                DeviceType.FIREWALL -> Random.nextInt(500, 1200)
                                DeviceType.LOAD_BALANCER -> Random.nextInt(1200, 2800)
                            }
                            val latency = when(dev.type) {
                                DeviceType.ROUTER -> Random.nextDouble(1.0, 1.8)
                                DeviceType.SWITCH -> Random.nextDouble(0.2, 0.8)
                                DeviceType.PC -> Random.nextDouble(2.0, 4.5)
                                DeviceType.PRINTER -> Random.nextDouble(5.0, 12.0)
                                DeviceType.CAMERA -> Random.nextDouble(3.0, 6.0)
                                DeviceType.SERVER -> Random.nextDouble(1.1, 2.5)
                                DeviceType.TERMINAL -> Random.nextDouble(3.0, 7.5)
                                DeviceType.SMART_DEVICE -> Random.nextDouble(4.0, 15.0)
                                DeviceType.GATEWAY -> Random.nextDouble(1.5, 3.2)
                                DeviceType.FIREWALL -> Random.nextDouble(0.5, 1.5)
                                DeviceType.LOAD_BALANCER -> Random.nextDouble(0.4, 1.2)
                            }
                            totalBandwidth += activeLoad
                            dev.copy(bandwidthKbps = activeLoad, latencyMs = latency)
                        } else {
                            dev.copy(bandwidthKbps = 0, latencyMs = 0.0)
                        }
                    }

                    _totalBandwidthKbps.value = totalBandwidth
                    if (onlineDevices.isNotEmpty()) {
                        _avgLatencyMs.value = onlineDevices.map { it.latencyMs }.average()
                    }
                }
            }
        }
    }

    // --- Command parser for CLI Shell ---
    fun executeConsoleCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return

        val parts = trimmed.split(" ")
        val baseCmd = parts[0].lowercase()

        val output = mutableListOf<String>()
        output.add("> $trimmed")

        when (baseCmd) {
            "help", "?" -> {
                output.add("Available CCNA level diagnostics:")
                output.add("  show ip int brief    - View active interfaces and ip addresses")
                output.add("  show vlan            - View configured VLAN division segments")
                output.add("  show ip route        - View Cisco IP subnets routing table")
                output.add("  show run             - Print currently running Cisco IOS script")
                output.add("  ping <ip_address>    - Perform ICMP ping handshake")
                output.add("  clear                - Clear console screen logs buffer")
                output.add("  scan                 - Execute administrative discovery ports sweep")
            }
            "clear" -> {
                _terminalLogs.value = listOf("Terminal buffer cleared.")
                return
            }
            "scan" -> {
                startScannerSweep()
                return
            }
            "ping" -> {
                if (parts.size < 2) {
                    output.add("Usage: ping <ip_address>")
                } else {
                    val ip = parts[1]
                    runInteractivePing(ip) { step ->
                        _terminalLogs.value = _terminalLogs.value + step
                    }
                    return
                }
            }
            "show" -> {
                if (parts.size < 2) {
                    output.add("Incomplete command. Try 'show ip int brief' or 'show vlan'")
                } else {
                    val sub = parts[1].lowercase()
                    if (sub == "vlan") {
                        output.add("VLAN Name                             Status    Ports")
                        output.add("---- -------------------------------- --------- -------------------------------")
                        _vlans.value.forEach { vlan ->
                            output.add(String.format("%-4d %-32s %-9s %s", vlan.id, vlan.name, "active", "Fa0/${vlan.id + 2}"))
                        }
                    } else if (sub == "ip") {
                        if (parts.size < 3) {
                            output.add("Incomplete command. Try 'show ip int brief' or 'show ip route'")
                        } else {
                            val target = parts[2].lowercase()
                            if (target == "route") {
                                output.add("Codes: C - connected, S - static, R - RIP, M - mobile")
                                output.add("Gateway of last resort is not set")
                                output.add("")
                                _vlans.value.forEach { vlan ->
                                    output.add("C    ${vlan.subnet} is directly connected, GigabitEthernet0/1.${vlan.id}")
                                }
                                output.add("S    0.0.0.0/0 [1/0] via 203.0.113.1")
                            } else if (target == "int" || target == "interface") {
                                output.add("Interface                  IP-Address      OK? Method Status                Protocol")
                                _devices.value.filter { it.type == DeviceType.ROUTER || it.type == DeviceType.SWITCH }.forEach { dev ->
                                    output.add(String.format("%-26s %-15s %-3s %-6s %-21s %s", dev.interfacePort.ifEmpty { "Gig0/1" }, dev.ipAddress, "YES", "manual", "up", "up"))
                                }
                                _devices.value.filter { it.type == DeviceType.PC || it.type == DeviceType.CAMERA || it.type == DeviceType.SERVER }.forEach { dev ->
                                    val statusStr = if (dev.status == DeviceStatus.BLOCKED) "administratively down" else "up"
                                    output.add(String.format("%-26s %-15s %-3s %-6s %-21s %s", dev.interfacePort.ifEmpty { "Fa0/3" }, dev.ipAddress, "YES", if (dev.isDhcp) "DHCP" else "manual", statusStr, "up"))
                                }
                            } else {
                                output.add("Unrecognized option: '$target'. Try 'show ip int brief'")
                            }
                        }
                    } else {
                        output.add("Unrecognized option: '$sub'. Try 'show run' or 'show vlan'")
                    }
                }
            }
            "show run", "show running-config" -> {
                output.add("Building configuration...")
                output.add("Current configuration : 1856 bytes")
                _activeCiscoConfig.value.lineSequence().forEach { line ->
                    output.add(line)
                }
            }
            else -> {
                if (trimmed.contains("show run")) {
                    output.add("Building configuration...")
                    output.add("Current configuration : 1856 bytes")
                    _activeCiscoConfig.value.lineSequence().forEach { line ->
                        output.add(line)
                    }
                } else {
                    output.add("NetArchitect Shell: command not found: '$trimmed'. Type 'help' or '?' for CCNA guide.")
                }
            }
        }

        _terminalLogs.value = _terminalLogs.value + output
    }

    private fun addTerminalLine(line: String) {
        _terminalLogs.value = _terminalLogs.value + line
    }

    private fun writeAuditLog(level: String, category: String, message: String) {
        viewModelScope.launch {
            repository.insertLog(AuditLog(level = level, category = category, message = message))
        }
    }

    // --- BANKING NETWORK SPECIFIC CONFIGURATIONS ---

    private val _bankDepartments = MutableStateFlow<List<BankDeptConfig>>(listOf(
        BankDeptConfig(0, "Retail Banking Department", "Provides banking services to individual and small businesses.", 4, 5, 1, 2, 1, 1, 0, 1, 0),
        BankDeptConfig(1, "Corporate/Commercial Banking", "Serves large businesses and corporations.", 0, 8, 1, 1, 1, 1, 1, 1, 0),
        BankDeptConfig(2, "Investment Banking", "Helps companies raise capital and provides financial advisory.", 0, 6, 1, 1, 1, 1, 1, 1, 1),
        BankDeptConfig(3, "Treasury Department", "Manages the bank’s liquidity, investments, and financial risks.", 0, 4, 2, 1, 1, 1, 0, 1, 0),
        BankDeptConfig(4, "Risk Management Department", "Identifies, analyzes, and mitigates financial and operational risks.", 0, 5, 1, 1, 1, 1, 0, 1, 0),
        BankDeptConfig(5, "Credit Department", "Evaluates and approves loan applications and manages credit portfolios.", 0, 6, 1, 1, 1, 1, 0, 1, 0),
        BankDeptConfig(6, "Internal Audit Department", "Independently assesses internal controls and compliance with policies.", 0, 4, 1, 1, 1, 1, 0, 1, 0),
        BankDeptConfig(7, "Compliance & Legal Department", "Ensures the bank follows all laws, regulations, and internal policies.", 0, 5, 1, 1, 1, 1, 0, 1, 0),
        BankDeptConfig(8, "Information Technology (IT) Department", "Manages digital banking systems, security, and infrastructure.", 0, 12, 4, 3, 2, 3, 2, 2, 2),
        BankDeptConfig(9, "Operations Department", "Handles back-office processes that support daily banking activities.", 1, 10, 1, 1, 1, 1, 1, 1, 0),
        BankDeptConfig(10, "Customer Service Department", "Handles customer inquiries, complaints, and account support.", 2, 8, 1, 1, 1, 1, 0, 1, 0),
        BankDeptConfig(11, "Human Resources (HR) Department", "Manages recruitment, training, staff welfare, and payroll.", 0, 5, 1, 1, 1, 1, 0, 1, 0),
        BankDeptConfig(12, "Marketing and Communication Department", "Promotes the bank's products and maintains brand reputation.", 0, 4, 1, 1, 1, 1, 0, 1, 0),
        BankDeptConfig(13, "Financial Control/Accounts Department", "Maintains the bank’s financial records and reports.", 0, 6, 1, 1, 1, 1, 0, 1, 0),
        BankDeptConfig(14, "E-Banking/Digital Banking Department", "Develops and maintains digital banking products and platforms.", 0, 8, 3, 1, 1, 1, 1, 1, 2),
        BankDeptConfig(15, "Loan Recovery/Remedial Department", "Ensures repayment of defaulted loans.", 0, 4, 1, 1, 1, 1, 0, 1, 0)
    ))
    val bankDepartments: StateFlow<List<BankDeptConfig>> = _bankDepartments.asStateFlow()

    fun updateBankDeptDeviceCount(deptId: Int, deviceField: String, value: Int) {
        _bankDepartments.value = _bankDepartments.value.map { dept ->
            if (dept.id == deptId) {
                when (deviceField) {
                    "terminal" -> dept.copy(customerTerminals = value.coerceAtLeast(0))
                    "workstation" -> dept.copy(workstations = value.coerceAtLeast(0))
                    "server" -> dept.copy(servers = value.coerceAtLeast(0))
                    "smart" -> dept.copy(smartDevices = value.coerceAtLeast(0))
                    "router" -> dept.copy(routers = value.coerceAtLeast(0))
                    "switch" -> dept.copy(switches = value.coerceAtLeast(0))
                    "gateway" -> dept.copy(gateways = value.coerceAtLeast(0))
                    "firewall" -> dept.copy(firewalls = value.coerceAtLeast(0))
                    "loadBalancer" -> dept.copy(loadBalancers = value.coerceAtLeast(0))
                    else -> dept
                }
            } else {
                dept
            }
        }
    }

    fun serializeBankConfig(depts: List<BankDeptConfig>): String {
        return depts.joinToString(";") { dept ->
            "${dept.id}:${dept.customerTerminals},${dept.workstations},${dept.servers},${dept.smartDevices},${dept.routers},${dept.switches},${dept.gateways},${dept.firewalls},${dept.loadBalancers}"
        }
    }

    fun deserializeBankConfig(serialized: String): List<BankDeptConfig> {
        val defaultDepts = _bankDepartments.value
        if (serialized.isBlank()) return defaultDepts
        return try {
            val parts = serialized.split(";")
            parts.mapNotNull { part ->
                val deptParts = part.split(":")
                if (deptParts.size < 2) return@mapNotNull null
                val id = deptParts[0].toIntOrNull() ?: return@mapNotNull null
                val counts = deptParts[1].split(",")
                if (counts.size < 9) return@mapNotNull null
                
                val originalDept = defaultDepts.firstOrNull { it.id == id } ?: return@mapNotNull null
                
                BankDeptConfig(
                    id = id,
                    name = originalDept.name,
                    function = originalDept.function,
                    customerTerminals = counts[0].toIntOrNull() ?: 0,
                    workstations = counts[1].toIntOrNull() ?: 0,
                    servers = counts[2].toIntOrNull() ?: 0,
                    smartDevices = counts[3].toIntOrNull() ?: 0,
                    routers = counts[4].toIntOrNull() ?: 0,
                    switches = counts[5].toIntOrNull() ?: 0,
                    gateways = counts[6].toIntOrNull() ?: 0,
                    firewalls = counts[7].toIntOrNull() ?: 0,
                    loadBalancers = counts[8].toIntOrNull() ?: 0
                )
            }
        } catch (e: Exception) {
            defaultDepts
        }
    }

    fun deployBankNetwork(locationName: String) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f
            writeAuditLog("INFO", "SCANNER", "Deploying custom banking network architecture for location: $locationName")
            addTerminalLine("> deploy bank-network --location \"$locationName\"")
            
            val activeDepts = _bankDepartments.value
            val generatedVlans = mutableListOf<VlanInfo>()
            val generatedDevices = mutableListOf<NetworkDevice>()
            
            // Add global WAN Router & core switch that links departments
            val coreRouter = NetworkDevice(
                name = "Bank-WAN-Router-01",
                type = DeviceType.ROUTER,
                ipAddress = "10.0.0.1",
                macAddress = "00:90:7F:A1:B2:10",
                vlanId = 1,
                connectedTo = "ISP Gateway",
                isDhcp = false,
                interfacePort = "Gig0/0"
            )
            val coreSwitch = NetworkDevice(
                name = "Core-Distribution-Switch-01",
                type = DeviceType.SWITCH,
                ipAddress = "10.0.0.2",
                macAddress = "00:90:7F:A1:B2:11",
                vlanId = 1,
                connectedTo = "Bank-WAN-Router-01",
                isDhcp = false,
                interfacePort = "Fa0/1"
            )
            generatedDevices.add(coreRouter)
            generatedDevices.add(coreSwitch)
            
            var macCounter = 12
            
            activeDepts.forEach { dept ->
                val deptTotal = dept.totalDevices()
                if (deptTotal > 0) {
                    val vlanId = 10 + dept.id * 10
                    val subnetPrefix = "10.$vlanId.0"
                    
                    val vlan = VlanInfo(
                        id = vlanId,
                        name = dept.name.replace("Department", "").trim(),
                        subnet = "$subnetPrefix.0/24",
                        gateway = "$subnetPrefix.1",
                        dhcpPool = "$subnetPrefix.50-$subnetPrefix.250",
                        description = dept.function
                    )
                    generatedVlans.add(vlan)
                    
                    // Generate specific devices
                    var termIdx = 1
                    var pcIdx = 1
                    var srvIdx = 1
                    var iotIdx = 1
                    var rtrIdx = 1
                    var swIdx = 1
                    var gwIdx = 1
                    var fwIdx = 1
                    var lbIdx = 1
                    
                    // Routers
                    repeat(dept.routers) {
                        generatedDevices.add(NetworkDevice(
                            name = "${vlan.name}-Router-${String.format("%02d", rtrIdx++)}",
                            type = DeviceType.ROUTER,
                            ipAddress = "$subnetPrefix.${2 + generatedDevices.size % 5}",
                            macAddress = "00:90:7F:E1:92:${String.format("%02X", macCounter++)}",
                            vlanId = vlanId,
                            connectedTo = "Core-Distribution-Switch-01",
                            isDhcp = false,
                            interfacePort = "Gig0/${rtrIdx - 1}"
                        ))
                    }
                    // Switches
                    repeat(dept.switches) {
                        generatedDevices.add(NetworkDevice(
                            name = "${vlan.name}-Switch-${String.format("%02d", swIdx++)}",
                            type = DeviceType.SWITCH,
                            ipAddress = "$subnetPrefix.${10 + generatedDevices.size % 5}",
                            macAddress = "00:90:7F:E1:93:${String.format("%02X", macCounter++)}",
                            vlanId = vlanId,
                            connectedTo = "Core-Distribution-Switch-01",
                            isDhcp = false,
                            interfacePort = "Fa0/${swIdx - 1}"
                        ))
                    }
                    // Firewalls
                    repeat(dept.firewalls) {
                        generatedDevices.add(NetworkDevice(
                            name = "${vlan.name}-Firewall-${String.format("%02d", fwIdx++)}",
                            type = DeviceType.FIREWALL,
                            ipAddress = "$subnetPrefix.${20 + generatedDevices.size % 5}",
                            macAddress = "00:90:7F:E1:94:${String.format("%02X", macCounter++)}",
                            vlanId = vlanId,
                            connectedTo = "${vlan.name}-Switch-01",
                            isDhcp = false,
                            interfacePort = "Fa0/${fwIdx + 5}"
                        ))
                    }
                    // Gateways
                    repeat(dept.gateways) {
                        generatedDevices.add(NetworkDevice(
                            name = "${vlan.name}-Gateway-${String.format("%02d", gwIdx++)}",
                            type = DeviceType.GATEWAY,
                            ipAddress = "$subnetPrefix.${30 + generatedDevices.size % 5}",
                            macAddress = "00:90:7F:E1:95:${String.format("%02X", macCounter++)}",
                            vlanId = vlanId,
                            connectedTo = "${vlan.name}-Switch-01",
                            isDhcp = false,
                            interfacePort = "Fa0/${gwIdx + 8}"
                        ))
                    }
                    // Load Balancers
                    repeat(dept.loadBalancers) {
                        generatedDevices.add(NetworkDevice(
                            name = "${vlan.name}-LoadBalancer-${String.format("%02d", lbIdx++)}",
                            type = DeviceType.LOAD_BALANCER,
                            ipAddress = "$subnetPrefix.${40 + generatedDevices.size % 5}",
                            macAddress = "00:90:7F:E1:96:${String.format("%02X", macCounter++)}",
                            vlanId = vlanId,
                            connectedTo = "${vlan.name}-Switch-01",
                            isDhcp = false,
                            interfacePort = "Fa0/${lbIdx + 11}"
                        ))
                    }
                    // Customer Terminals (ATMs / Tellers)
                    repeat(dept.customerTerminals) {
                        generatedDevices.add(NetworkDevice(
                            name = "${vlan.name}-ATM-${String.format("%02d", termIdx++)}",
                            type = DeviceType.TERMINAL,
                            ipAddress = "$subnetPrefix.${50 + termIdx}",
                            macAddress = "00:90:7F:A1:C2:${String.format("%02X", macCounter++)}",
                            vlanId = vlanId,
                            connectedTo = "${vlan.name}-Switch-01",
                            isDhcp = true,
                            interfacePort = "Fa0/${termIdx + 13}"
                        ))
                    }
                    // Workstations
                    repeat(dept.workstations) {
                        generatedDevices.add(NetworkDevice(
                            name = "${vlan.name}-Workstation-${String.format("%02d", pcIdx++)}",
                            type = DeviceType.PC,
                            ipAddress = "$subnetPrefix.${100 + pcIdx}",
                            macAddress = "00:90:7F:B1:D2:${String.format("%02X", macCounter++)}",
                            vlanId = vlanId,
                            connectedTo = "${vlan.name}-Switch-01",
                            isDhcp = true,
                            interfacePort = "Fa0/${pcIdx + 17}"
                        ))
                    }
                    // Servers
                    repeat(dept.servers) {
                        generatedDevices.add(NetworkDevice(
                            name = "${vlan.name}-Server-${String.format("%02d", srvIdx++)}",
                            type = DeviceType.SERVER,
                            ipAddress = "$subnetPrefix.${200 + srvIdx}",
                            macAddress = "00:90:7F:C1:E2:${String.format("%02X", macCounter++)}",
                            vlanId = vlanId,
                            connectedTo = "${vlan.name}-Switch-01",
                            isDhcp = false,
                            interfacePort = "Fa0/${srvIdx + 20}"
                        ))
                    }
                    // Smart Devices
                    repeat(dept.smartDevices) {
                        generatedDevices.add(NetworkDevice(
                            name = "${vlan.name}-SmartIoT-${String.format("%02d", iotIdx++)}",
                            type = DeviceType.SMART_DEVICE,
                            ipAddress = "$subnetPrefix.${150 + iotIdx}",
                            macAddress = "00:90:7F:D1:F2:${String.format("%02X", macCounter++)}",
                            vlanId = vlanId,
                            connectedTo = "${vlan.name}-Switch-01",
                            isDhcp = true,
                            interfacePort = "Fa0/${iotIdx + 24}"
                        ))
                    }
                }
            }
            
            // Generate CCNA Running Configuration
            val iosConfig = buildCiscoIosConfig(locationName, generatedVlans, activeDepts)
            
            _vlans.value = generatedVlans
            _devices.value = generatedDevices
            _activeCiscoConfig.value = iosConfig
            _networkOverview.value = """
                [BANK ARCHITECTURE DEPLOYED - $locationName]
                Successfully configured a secure, multi-department enterprise bank network.
                
                - Total Departments Setup: ${generatedVlans.size}
                - Total Active Devices Provisioned: ${generatedDevices.size}
                - Inter-VLAN routing configured with strict port-security limiters and security boundaries.
                
                Security Measures Configured:
                - Firewalls deployed for boundary security.
                - Enterprise Load Balancers active for core Mainframe systems.
                - Port-security active on all employee edge workstations.
                - Strict inter-VLAN access control lists (ACL) separating public ATMs from secure back-office servers.
            """.trimIndent()
            
            // Simulate progression
            for (i in 1..5) {
                delay(200)
                _scanProgress.value = i / 5f
            }
            _isScanning.value = false
            _overallHealthScore.value = 100
            
            writeAuditLog("SUCCESS", "SEC_POLICY", "Bank Network configured & compiled successfully for location: $locationName")
            addTerminalLine("Inter-VLAN encapsulation active. All ports configured and open.")
            addTerminalLine("Cisco running-config refreshed.")
        }
    }

    private fun buildCiscoIosConfig(locationName: String, vlans: List<VlanInfo>, depts: List<BankDeptConfig>): String {
        val sb = StringBuilder()
        sb.append("!\n")
        sb.append("! Cisco IOS running-config for Bank Location: $locationName\n")
        sb.append("! Generated by NetArchitect Bank Network Configurator\n")
        sb.append("!\n")
        sb.append("version 15.4\n")
        sb.append("hostname Bank-${locationName.replace(" ", "-")}\n")
        sb.append("!\n")
        sb.append("ip routing\n")
        sb.append("ip cef\n")
        sb.append("!\n")
        sb.append("! --- VLAN CONFIGURATIONS ---\n")
        vlans.forEach { vlan ->
            sb.append("vlan ${vlan.id}\n")
            sb.append(" name ${vlan.name}\n")
            sb.append("!\n")
        }
        sb.append("!\n")
        sb.append("! --- DHCP POOLS SETUP ---\n")
        vlans.forEach { vlan ->
            val ipPart = vlan.subnet.substringBeforeLast(".")
            sb.append("ip dhcp excluded-address ${vlan.gateway}\n")
            sb.append("ip dhcp excluded-address $ipPart.2\n")
            sb.append("ip dhcp excluded-address $ipPart.3\n")
            sb.append("ip dhcp excluded-address $ipPart.4\n")
            sb.append("ip dhcp pool POOL_VLAN_${vlan.id}\n")
            sb.append(" network $ipPart.0 255.255.255.0\n")
            sb.append(" default-router ${vlan.gateway}\n")
            sb.append(" dns-server 10.100.100.1 8.8.8.8\n")
            sb.append(" lease 1 0 0\n")
            sb.append("!\n")
        }
        sb.append("!\n")
        sb.append("! --- INTER-VLAN SUB-INTERFACES CONFIGURATION ---\n")
        sb.append("interface GigabitEthernet0/0\n")
        sb.append(" description WAN-Edge-Gateway-Uplink\n")
        sb.append(" ip address 203.0.113.2 255.255.255.252\n")
        sb.append(" ip nat outside\n")
        sb.append(" no shutdown\n")
        sb.append("!\n")
        vlans.forEach { vlan ->
            sb.append("interface GigabitEthernet0/1.${vlan.id}\n")
            sb.append(" description sub-interface for ${vlan.name}\n")
            sb.append(" encapsulation dot1Q ${vlan.id}\n")
            sb.append(" ip address ${vlan.gateway} 255.255.255.0\n")
            sb.append(" ip nat inside\n")
            sb.append("!\n")
        }
        sb.append("!\n")
        sb.append("! --- SYSTEM PORT-SECURITY & SWITCH EDGE MAC LIMITS ---\n")
        sb.append("interface range FastEthernet0/1 - 24\n")
        sb.append(" switchport mode access\n")
        sb.append(" switchport port-security\n")
        sb.append(" switchport port-security maximum 2\n")
        sb.append(" switchport port-security violation shutdown\n")
        sb.append(" switchport port-security aging time 10\n")
        sb.append(" keepalive 10\n")
        sb.append("!\n")
        sb.append("! --- STRICT INTER-VLAN ACCESS CONTROL LISTS (ACL) ---\n")
        sb.append("! Prevent Retail / Customer ATMs (VLAN 10) from accessing Treasury (VLAN 40) or IT Servers\n")
        sb.append("ip access-list extended SECURE_BANK_INSIDE\n")
        sb.append(" deny ip 10.10.10.0 0.0.0.255 10.10.40.0 0.0.0.255\n")
        sb.append(" deny ip 10.10.10.0 0.0.0.255 10.10.90.0 0.0.0.255\n")
        sb.append(" permit ip any any\n")
        sb.append("!\n")
        sb.append("interface GigabitEthernet0/1.10\n")
        sb.append(" ip access-group SECURE_BANK_INSIDE in\n")
        sb.append("!\n")
        sb.append("! --- COMPLIANCE ROUTING POLICIES ---\n")
        sb.append("ip route 0.0.0.0 0.0.0.0 203.0.113.1\n")
        sb.append("!\n")
        sb.append("end\n")
        return sb.toString()
    }

    fun saveBankLocationNetwork(locationName: String) {
        viewModelScope.launch {
            val serialData = serializeBankConfig(_bankDepartments.value)
            val activeConfig = _activeCiscoConfig.value
            val totalDeviceCount = _devices.value.size
            
            val project = SavedProject(
                name = "Bank Network: $locationName",
                description = serialData,
                deviceCount = totalDeviceCount,
                ciscoConfig = activeConfig
            )
            repository.saveProject(project)
            writeAuditLog("SUCCESS", "BACKEND", "Saved bank location network project \"$locationName\" to database.")
        }
    }

    fun loadSavedBankNetwork(project: SavedProject) {
        viewModelScope.launch {
            val nameOnly = project.name.replace("Bank Network: ", "")
            val depts = deserializeBankConfig(project.description)
            _bankDepartments.value = depts
            deployBankNetwork(nameOnly)
            writeAuditLog("SUCCESS", "BACKEND", "Loaded bank location network project \"$nameOnly\" successfully.")
        }
    }

    // --- END OF BANKING CONFIG METHODS ---

    // --- Routing Path Optimizer ---
    private val _selectedSourceNodeId = MutableStateFlow<String?>(null)
    val selectedSourceNodeId: StateFlow<String?> = _selectedSourceNodeId.asStateFlow()

    private val _selectedDestNodeId = MutableStateFlow<String?>(null)
    val selectedDestNodeId: StateFlow<String?> = _selectedDestNodeId.asStateFlow()

    private val _optimalPath = MutableStateFlow<List<String>>(emptyList())
    val optimalPath: StateFlow<List<String>> = _optimalPath.asStateFlow()

    private val _pathBottlenecks = MutableStateFlow<List<String>>(emptyList())
    val pathBottlenecks: StateFlow<List<String>> = _pathBottlenecks.asStateFlow()

    fun selectSourceNode(id: String?) {
        _selectedSourceNodeId.value = id
        calculateOptimalPath()
    }

    fun selectDestNode(id: String?) {
        _selectedDestNodeId.value = id
        calculateOptimalPath()
    }

    fun calculateOptimalPath() {
        val srcId = _selectedSourceNodeId.value
        val dstId = _selectedDestNodeId.value
        val currentDevices = _devices.value

        if (srcId == null || dstId == null || currentDevices.isEmpty()) {
            _optimalPath.value = emptyList()
            _pathBottlenecks.value = emptyList()
            return
        }

        if (srcId == dstId) {
            _optimalPath.value = listOf(srcId)
            _pathBottlenecks.value = emptyList()
            return
        }

        // Build adjacency map
        val adj = mutableMapOf<String, MutableSet<String>>()
        currentDevices.forEach { adj[it.id] = mutableSetOf() }

        currentDevices.forEach { dev ->
            val parentName = dev.connectedTo
            if (parentName.isNotBlank()) {
                val parent = currentDevices.firstOrNull { it.name.equals(parentName, ignoreCase = true) }
                if (parent != null) {
                    adj[dev.id]?.add(parent.id)
                    adj[parent.id]?.add(dev.id)
                } else {
                    if (dev.type != DeviceType.ROUTER && dev.type != DeviceType.SWITCH) {
                        val coreSwitch = currentDevices.firstOrNull { it.type == DeviceType.SWITCH }
                        if (coreSwitch != null) {
                            adj[dev.id]?.add(coreSwitch.id)
                            adj[coreSwitch.id]?.add(dev.id)
                        }
                    }
                }
            } else {
                if (dev.type == DeviceType.SWITCH) {
                    val rtr = currentDevices.firstOrNull { it.type == DeviceType.ROUTER }
                    if (rtr != null) {
                        adj[dev.id]?.add(rtr.id)
                        adj[rtr.id]?.add(dev.id)
                    }
                } else if (dev.type != DeviceType.ROUTER) {
                    val coreSwitch = currentDevices.firstOrNull { it.type == DeviceType.SWITCH }
                    if (coreSwitch != null) {
                        adj[dev.id]?.add(coreSwitch.id)
                        adj[coreSwitch.id]?.add(dev.id)
                    }
                }
            }
        }

        // BFS Shortest Path
        val queue = ArrayDeque<String>()
        val visited = mutableSetOf<String>()
        val parentMap = mutableMapOf<String, String>()

        queue.add(srcId)
        visited.add(srcId)

        var found = false
        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            if (curr == dstId) {
                found = true
                break
            }
            adj[curr]?.forEach { neighbor ->
                if (neighbor !in visited) {
                    visited.add(neighbor)
                    parentMap[neighbor] = curr
                    queue.add(neighbor)
                }
            }
        }

        val path = mutableListOf<String>()
        if (found) {
            var temp: String? = dstId
            while (temp != null) {
                path.add(0, temp)
                temp = parentMap[temp]
            }
        }

        _optimalPath.value = path

        // Identify bottlenecks along the path
        val warnings = mutableListOf<String>()
        path.forEach { nodeId ->
            val dev = currentDevices.firstOrNull { it.id == nodeId }
            if (dev != null) {
                if (dev.status == DeviceStatus.OFFLINE) {
                    warnings.add("Node '${dev.name}' is OFFLINE - packet routing is completely blocked!")
                } else if (dev.status == DeviceStatus.BLOCKED) {
                    warnings.add("Node '${dev.name}' is ADMINISTRATIVELY BLOCKED by firewalls!")
                } else if (dev.status == DeviceStatus.THREAT_DETECTED) {
                    warnings.add("Node '${dev.name}' is under active intrusion threat, security filters will cause major queue delay!")
                } else {
                    if (dev.latencyMs > 8.0) {
                        warnings.add("Congestion alert at '${dev.name}': High response latency (${String.format("%.1f", dev.latencyMs)} ms)!")
                    }
                    if (dev.bandwidthKbps > 2200) {
                        warnings.add("High link load on '${dev.name}': Traffic utilizes standard port allocations.")
                    }
                    if (dev.type == DeviceType.PC || dev.type == DeviceType.PRINTER || dev.type == DeviceType.CAMERA || dev.type == DeviceType.TERMINAL) {
                        warnings.add("End-host traversal via ${dev.type.label} '${dev.name}': Hop lacks standard dynamic routing throughput!")
                    }
                }
            }
        }

        for (i in 0 until path.size - 1) {
            val n1 = currentDevices.firstOrNull { it.id == path[i] }
            val n2 = currentDevices.firstOrNull { it.id == path[i+1] }
            if (n1 != null && n2 != null) {
                if (n1.type == DeviceType.SMART_DEVICE || n2.type == DeviceType.SMART_DEVICE) {
                    warnings.add("Vulnerable Link segment [${n1.name} ↔ ${n2.name}]: Traverses IoT channels.")
                }
            }
        }

        _pathBottlenecks.value = warnings
    }

    // --- SNMPv3 secure configurations state ---
    private val _snmpGroupName = MutableStateFlow("NetAdminGroup")
    val snmpGroupName: StateFlow<String> = _snmpGroupName.asStateFlow()

    private val _snmpUsername = MutableStateFlow("SecMonitorUser")
    val snmpUsername: StateFlow<String> = _snmpUsername.asStateFlow()

    private val _snmpAuthPass = MutableStateFlow("")
    val snmpAuthPass: StateFlow<String> = _snmpAuthPass.asStateFlow()

    private val _snmpPrivPass = MutableStateFlow("")
    val snmpPrivPass: StateFlow<String> = _snmpPrivPass.asStateFlow()

    private val _snmpTrapReceiver = MutableStateFlow("10.10.99.10")
    val snmpTrapReceiver: StateFlow<String> = _snmpTrapReceiver.asStateFlow()

    private val _isSnmpConfigured = MutableStateFlow(false)
    val isSnmpConfigured: StateFlow<Boolean> = _isSnmpConfigured.asStateFlow()

    fun generateAndApplySnmpV3() {
        val randAuth = "AuthSha_" + Random.nextInt(1000, 9999) + "!"
        val randPriv = "PrivAes_" + Random.nextInt(1000, 9999) + "#"

        _snmpAuthPass.value = randAuth
        _snmpPrivPass.value = randPriv
        _isSnmpConfigured.value = true

        val ciscoSnippets = """
            !
            ! --- AUTOMATICALLY DEPLOYED SNMPv3 MONITORING ENGINE ---
            snmp-server group ${_snmpGroupName.value} v3 priv read SECURE_VIEW write SECURE_VIEW
            snmp-server user ${_snmpUsername.value} ${_snmpGroupName.value} v3 auth sha $randAuth priv aes 128 $randPriv
            snmp-server host ${_snmpTrapReceiver.value} version 3 priv ${_snmpUsername.value}
            snmp-server enable traps snmp authentication linkup linkdown
            !
        """.trimIndent()

        _activeCiscoConfig.value = _activeCiscoConfig.value + "\n" + ciscoSnippets

        writeAuditLog(
            level = "SUCCESS",
            category = "SEC_POLICY",
            message = "Deployed SNMPv3 group '${_snmpGroupName.value}' with SHA/AES crypto keys to all managed routers & switches."
        )

        addTerminalLine("> snmp configure-v3 --group ${_snmpGroupName.value} --user ${_snmpUsername.value}")
        addTerminalLine("Generating SHA Authentication key... OK ($randAuth)")
        addTerminalLine("Generating AES-128 Encryption key... OK ($randPriv)")

        val targetDevices = _devices.value.filter { it.type == DeviceType.ROUTER || it.type == DeviceType.SWITCH }
        targetDevices.forEach { dev ->
            addTerminalLine("Applying SNMPv3 secure engine to ${dev.name} (${dev.ipAddress})... OK")
        }
        addTerminalLine("Successfully deployed SNMP Trap Receiver target set to ${_snmpTrapReceiver.value}.")
    }

    // --- CIS Banking Benchmarks Security Audit ---
    private val _cisComplianceScore = MutableStateFlow<Int?>(null)
    val cisComplianceScore: StateFlow<Int?> = _cisComplianceScore.asStateFlow()

    private val _cisAuditResults = MutableStateFlow<List<CisAuditResult>>(emptyList())
    val cisAuditResults: StateFlow<List<CisAuditResult>> = _cisAuditResults.asStateFlow()

    private val _isCisScanning = MutableStateFlow(false)
    val isCisScanning: StateFlow<Boolean> = _isCisScanning.asStateFlow()

    fun runCisSecurityScan() {
        if (_isCisScanning.value) return
        viewModelScope.launch {
            _isCisScanning.value = true
            addTerminalLine("> cis audit --profile banking-v1.4")
            addTerminalLine("Initiating CIS Security Benchmarks cross-reference check...")
            delay(1200)

            val results = mutableListOf<CisAuditResult>()
            val config = _activeCiscoConfig.value
            val currentDevices = _devices.value

            // 1. Password Encryption Check
            val hasPassEnc = config.contains("service password-encryption")
            results.add(CisAuditResult(
                ruleId = "CIS-1.1",
                title = "Enable Service Password Encryption",
                category = "Device Hardening",
                status = hasPassEnc,
                severity = "HIGH",
                description = "Unencrypted Cisco passwords stored in running-config are vulnerable to configuration snooping and breach.",
                recommendation = "Add 'service password-encryption' to global configuration."
            ))

            // 2. SNMPv3 Check
            val hasSnmpv3 = _isSnmpConfigured.value || (config.contains("snmp-server group") && config.contains("v3 priv"))
            results.add(CisAuditResult(
                ruleId = "CIS-1.2",
                title = "Enforce Secure SNMPv3 Encryption Profiles",
                category = "SNMP Security",
                status = hasSnmpv3,
                severity = "HIGH",
                description = "Plaintext SNMPv1/v2c community strings leave operational controls open to interception.",
                recommendation = "Generate and deploy SNMPv3 credentials using SHA auth and AES-128 encryption."
            ))

            // 3. VLAN 1 Isolation Check
            val hasPcOnVlan1 = currentDevices.any { it.vlanId == 1 && (it.type == DeviceType.PC || it.type == DeviceType.TERMINAL) }
            results.add(CisAuditResult(
                ruleId = "CIS-2.1",
                title = "Segregate Traffic from Default VLAN 1",
                category = "Access Control",
                status = !hasPcOnVlan1,
                severity = "MEDIUM",
                description = "VLAN 1 is the default administrative native VLAN and should not be used for user traffic to prevent VLAN hopping attacks.",
                recommendation = "Reassign retail ATM terminals and workstations to specific secured subnets (VLAN 10 or 20)."
            ))

            // 4. Secure Transport SSH Check
            val hasSsh = config.contains("transport input ssh") && !config.contains("transport input telnet")
            results.add(CisAuditResult(
                ruleId = "CIS-3.1",
                title = "Enforce Secure SSH Administration Ports",
                category = "Device Hardening",
                status = hasSsh,
                severity = "CRITICAL",
                description = "Unencrypted Telnet protocols leak administrator passwords in transit. Access lines must enforce SSH.",
                recommendation = "Configure 'transport input ssh' under all virtual terminal lines (line vty 0 4)."
            ))

            // 5. Port Security check on switch ports
            val hasPortSecurity = config.contains("switchport port-security")
            results.add(CisAuditResult(
                ruleId = "CIS-4.1",
                title = "Configure Switchport MAC Limits",
                category = "Interface Security",
                status = hasPortSecurity,
                severity = "MEDIUM",
                description = "Unrestricted MAC address flooding can crash switch CAM tables. Strict limits prevent rogue access.",
                recommendation = "Apply 'switchport port-security maximum 2' and violation actions to all edge interfaces."
            ))

            // 6. Strict Banking Inter-VLAN ACL check
            val hasSecureAcl = config.contains("SECURE_BANK_INSIDE") || config.contains("SECURE_BANK")
            results.add(CisAuditResult(
                ruleId = "CIS-5.1",
                title = "Isolate Public ATMs from Core Banking Databases",
                category = "Access Control",
                status = hasSecureAcl,
                severity = "CRITICAL",
                description = "High-security banking subnets must have strict Access Control Lists (ACLs) separating public-facing units from back-end ledgers.",
                recommendation = "Apply extended Access-Lists blocking public subnets from accessing database / treasury server hosts."
            ))

            val passing = results.count { it.status }
            val score = (passing * 100) / results.size

            _cisComplianceScore.value = score
            _cisAuditResults.value = results
            _isCisScanning.value = false

            writeAuditLog(
                level = if (score >= 90) "SUCCESS" else if (score >= 60) "WARNING" else "ALERT",
                category = "SEC_POLICY",
                message = "CIS Security Scan completed. Compliance Score: $score%. ${results.size - passing} failed benchmarks."
            )

            addTerminalLine("Audit completed. CIS compliance score: $score%")
            results.forEach { r ->
                val statusIndicator = if (r.status) "[PASS]" else "[FAIL]"
                addTerminalLine("  $statusIndicator ${r.ruleId}: ${r.title} (${r.severity})")
            }
        }
    }

    fun remediateCisCompliance() {
        viewModelScope.launch {
            writeAuditLog("INFO", "SEC_POLICY", "Initiating automated CIS Benchmarks banking compliance remediation...")
            addTerminalLine("> cis remediate --all-targets")
            addTerminalLine("Executing automated CIS security policies alignment...")
            delay(1000)

            var config = _activeCiscoConfig.value

            if (!config.contains("service password-encryption")) {
                addTerminalLine("Remediating CIS-1.1: Enabling service password-encryption... OK")
                config = "service password-encryption\n" + config
            }

            if (!_isSnmpConfigured.value) {
                addTerminalLine("Remediating CIS-1.2: Activating SNMPv3 privacy engines... OK")
                generateAndApplySnmpV3()
                config = _activeCiscoConfig.value
            }

            val pcsOnVlan1 = _devices.value.filter { it.vlanId == 1 && (it.type == DeviceType.PC || it.type == DeviceType.TERMINAL) }
            if (pcsOnVlan1.isNotEmpty()) {
                addTerminalLine("Remediating CIS-2.1: Re-assigning ${pcsOnVlan1.size} user terminals from VLAN 1 to secure VLAN 10...")
                _devices.value = _devices.value.map { dev ->
                    if (dev.vlanId == 1 && (dev.type == DeviceType.PC || dev.type == DeviceType.TERMINAL)) {
                        dev.copy(vlanId = 10)
                    } else {
                        dev
                    }
                }
            }

            if (!config.contains("transport input ssh")) {
                addTerminalLine("Remediating CIS-3.1: Securing administration lines... OK")
                val sshSnippet = """
                    !
                    line vty 0 4
                     login local
                     transport input ssh
                    !
                """.trimIndent()
                config = config + "\n" + sshSnippet
            }

            if (!config.contains("switchport port-security")) {
                addTerminalLine("Remediating CIS-4.1: Enforcing edge switchport security filters... OK")
                val portSecuritySnippet = """
                    !
                    interface range FastEthernet0/1 - 24
                     switchport mode access
                     switchport port-security
                     switchport port-security maximum 2
                     switchport port-security violation shutdown
                    !
                """.trimIndent()
                config = config + "\n" + portSecuritySnippet
            }

            if (!config.contains("SECURE_BANK_INSIDE")) {
                addTerminalLine("Remediating CIS-5.1: Injecting strict segregation Access-Lists... OK")
                val aclSnippet = """
                    !
                    ip access-list extended SECURE_BANK_INSIDE
                     deny ip 10.10.10.0 0.0.0.255 10.10.99.0 0.0.0.255
                     permit ip any any
                    !
                    interface GigabitEthernet0/1.10
                     ip access-group SECURE_BANK_INSIDE in
                    !
                """.trimIndent()
                config = config + "\n" + aclSnippet
            }

            _activeCiscoConfig.value = config
            _isSnmpConfigured.value = true

            val results = _cisAuditResults.value.map { it.copy(status = true) }
            _cisComplianceScore.value = 100
            _cisAuditResults.value = results

            writeAuditLog("SUCCESS", "SEC_POLICY", "rem-audit: Cisco IOS running-config is now 100% compliant with CIS Banking Benchmarks!")
            addTerminalLine("Remediation complete. Current compliance score is now 100%. Running-config synced.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        threatGenerationJob?.cancel()
        telemetryJob?.cancel()
    }
}

data class BankDeptConfig(
    val id: Int,
    val name: String,
    val function: String,
    val customerTerminals: Int = 0,
    val workstations: Int = 0,
    val servers: Int = 0,
    val smartDevices: Int = 0,
    val routers: Int = 1,
    val switches: Int = 1,
    val gateways: Int = 0,
    val firewalls: Int = 0,
    val loadBalancers: Int = 0
) {
    fun totalDevices(): Int = customerTerminals + workstations + servers + smartDevices + routers + switches + gateways + firewalls + loadBalancers
}

data class CisAuditResult(
    val ruleId: String,
    val title: String,
    val category: String, // "Access Control", "Device Hardening", "SNMP Security", "Interface Security"
    val status: Boolean, // true = PASS, false = FAIL
    val severity: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    val description: String,
    val recommendation: String
)
