package com.example.data

import java.util.UUID

enum class DeviceType(val label: String) {
    ROUTER("Router"),
    SWITCH("Switch"),
    PC("Workstation PC"),
    PRINTER("Network Printer"),
    CAMERA("Surveillance Camera"),
    SERVER("Enterprise Server"),
    TERMINAL("Customer Terminal"),
    SMART_DEVICE("Smart IoT Device"),
    GATEWAY("Network Gateway"),
    FIREWALL("Firewall"),
    LOAD_BALANCER("Load Balancer")
}

enum class DeviceStatus(val label: String) {
    ONLINE("Online"),
    OFFLINE("Offline"),
    BLOCKED("Blocked"),
    THREAT_DETECTED("Intrusion Alert")
}

data class NetworkDevice(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    val type: DeviceType,
    var ipAddress: String,
    val macAddress: String,
    var vlanId: Int,
    var connectedTo: String, // Core-Switch-01, Core-Router-01
    var status: DeviceStatus = DeviceStatus.ONLINE,
    var bandwidthKbps: Int = 0,
    var latencyMs: Double = 0.0,
    var isDhcp: Boolean = true,
    var interfacePort: String = ""
)

data class VlanInfo(
    val id: Int,
    val name: String,
    val subnet: String,
    val gateway: String,
    val dhcpPool: String,
    val description: String
)

data class AclPolicy(
    val id: Int,
    val vlanId: Int,
    val action: String, // PERMIT, DENY
    val protocol: String, // IP, TCP, UDP, ICMP
    val source: String,
    val destination: String,
    val description: String
)

data class ThreatAlert(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val sourceMac: String,
    val sourceIp: String,
    val severity: String, // CRITICAL, MEDIUM, LOW
    val description: String,
    val interfacePort: String,
    var resolved: Boolean = false
)
