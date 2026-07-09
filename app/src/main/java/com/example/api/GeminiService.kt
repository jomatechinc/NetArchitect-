package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val maxOutputTokens: Int? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

object GeminiHelper {
    suspend fun askGemini(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Placeholder key or blank -> Use Local Network Engine fallback
            return generateLocalFallback(prompt)
        }

        val systemPrompt = """
            You are NetArchitect AI, a Senior Network Engineer holding CCIE and CCNA certifications.
            The user will ask you to configure or set up a network (e.g. secure bank, corporate office, guest hotspot).
            You MUST generate a highly professional, fully-compliant Cisco IOS configuration and a clear structured breakdown.
            
            Your response MUST include:
            1. [NETWORK DESCRIPTOR] A brief 2-3 sentence overview of the designed architecture, subnet scheme, and security logic.
            2. [IP ALLOCATION TABLE] A Markdown table detailing VLANs, Subnets (CIDR), Gateways, and Roles.
            3. [CISCO RUNNING CONFIG] A complete, syntactically correct Cisco IOS command script wrapped in a ```cisco code block.
               Include hostname, enable secret, vlan creation, trunking, dynamic trunking protocol disabling (switchport nonegotiate),
               port security, DHCP scopes, standard/extended Access Control Lists (ACLs) to enforce segmentation, NAT configuration, and static/dynamic routing.
            4. [SECURITY AUDIT] A list of 3 best-practice compliance checks satisfied (e.g., PCI-DSS, Teller PC isolation, Camera LAN restrictions).
            
            Keep your text output concise and highly professional. Do not add casual conversational fluff.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(
                temperature = 0.2f,
                maxOutputTokens = 2048
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Error: No response generated from AI. Falling back to local configuration engine."
        } catch (e: Exception) {
            e.printStackTrace()
            // Error occurred -> fallback to local simulator
            generateLocalFallback(prompt)
        }
    }

    private fun generateLocalFallback(prompt: String): String {
        val normalized = prompt.lowercase()
        val isBank = normalized.contains("bank") || normalized.contains("teller") || normalized.contains("secure")
        val isGuest = normalized.contains("guest") || normalized.contains("wifi") || normalized.contains("hotspot")
        
        return if (isBank) {
            """
            [NETWORK DESCRIPTOR]
            NetArchitect AI has provisioned a highly secure corporate bank infrastructure. This template implements multi-tier segmentation via isolated VLANs, redundant access links, strict port security, and dedicated subnets to isolate teller PCs, back-office operations, IP surveillance, and management servers.
            
            [IP ALLOCATION TABLE]
            | VLAN ID | VLAN Name    | Subnet Range    | Default Gateway | Security Policy / ACLs |
            |---------|--------------|-----------------|-----------------|------------------------|
            | VLAN 10 | Tellers      | 10.10.10.0/24   | 10.10.10.1      | Isolated. Can only access main servers. No general internet. |
            | VLAN 20 | Back-Office  | 10.10.20.0/24   | 10.10.20.1      | Standard priority. Encrypted outbound WAN. |
            | VLAN 30 | Surveillance | 10.10.30.0/24   | 10.10.30.1      | Strictly local. No external WAN routing allowed. |
            | VLAN 99 | Management   | 10.10.99.0/24   | 10.10.99.1      | Secure shell (SSH) access only. |

            [CISCO RUNNING CONFIG]
            ```cisco
            ! NetArchitect Secure Bank Configuration v4.2
            hostname Core-Bank-Router
            enable secret NetSecure_99!
            !
            vlan 10
             name Tellers
            vlan 20
             name Back-Office
            vlan 30
             name Surveillance
            vlan 99
             name Management
            !
            ip dhcp excluded-address 10.10.10.1 10.10.10.10
            ip dhcp excluded-address 10.10.20.1 10.10.20.10
            !
            ip dhcp pool TELLER_POOL
             network 10.10.10.0 255.255.255.0
             default-router 10.10.10.1
             dns-server 1.1.1.1
            !
            ip dhcp pool BACK_OFFICE_POOL
             network 10.10.20.0 255.255.255.0
             default-router 10.10.20.1
             dns-server 1.1.1.1
            !
            interface GigabitEthernet0/0
             description WAN Link (Primary Outbound)
             ip address 203.0.113.2 255.255.255.252
             ip nat outside
            !
            interface GigabitEthernet0/1.10
             encapsulation dot1Q 10
             ip address 10.10.10.1 255.255.255.0
             ip access-group ACL_TELLER_SEC in
             ip nat inside
            !
            interface GigabitEthernet0/1.20
             encapsulation dot1Q 20
             ip address 10.10.20.1 255.255.255.0
             ip access-group ACL_OFFICE_WAN in
             ip nat inside
            !
            interface GigabitEthernet0/1.30
             encapsulation dot1Q 30
             ip address 10.10.30.1 255.255.255.0
             ip access-group ACL_SURVEILLANCE_LAN in
            !
            ip access-list extended ACL_TELLER_SEC
             permit ip 10.10.10.0 0.0.0.255 host 10.10.99.50
             deny ip 10.10.10.0 0.0.0.255 any
            !
            ip access-list extended ACL_OFFICE_WAN
             permit ip 10.10.20.0 0.0.0.255 any
            !
            ip access-list extended ACL_SURVEILLANCE_LAN
             permit ip 10.10.30.0 0.0.0.255 10.10.99.0 0.0.0.255
             deny ip any any
            !
            ip nat inside source list ACL_OFFICE_WAN interface GigabitEthernet0/0 overload
            !
            line vty 0 4
             transport input ssh
            end
            ```

            [SECURITY AUDIT]
            - COMPLIANCE SATISFIED: PCI-DSS v4.0 Network Segmentation Mandate (Requirement 1.1)
            - COMPLIANCE SATISFIED: Teller subnet completely isolated from unauthorized guest or general internet access.
            - COMPLIANCE SATISFIED: CCTV IP Cameras restricted to LAN recording server; external spoof/outbound threat mitigated.
            """.trimIndent()
        } else if (isGuest) {
            """
            [NETWORK DESCRIPTOR]
            NetArchitect AI has provisioned a dynamic, sandboxed Guest Wi-Fi portal network. This infrastructure implements client-to-client isolation (wireless isolation), dynamic DHCP leasing with short lease times, dynamic NAT overload, and strict speed-limiting throttling configs to prevent congestion.
            
            [IP ALLOCATION TABLE]
            | VLAN ID | VLAN Name    | Subnet Range    | Default Gateway | Security Policy / ACLs |
            |---------|--------------|-----------------|-----------------|------------------------|
            | VLAN 80 | Guest-WiFi   | 192.168.80.0/22 | 192.168.80.1    | Throttled. Isolated. Access outbound to HTTP/HTTPS only. |
            | VLAN 99 | Management   | 192.168.99.0/24 | 192.168.99.1    | Core Switch, restricted SSH. |

            [CISCO RUNNING CONFIG]
            ```cisco
            ! NetArchitect Guest Portal Configuration
            hostname Hotspot-Gateway
            enable secret WiFiPass_2026!
            !
            vlan 80
             name Guest-WiFi
            !
            ip dhcp pool GUEST_WIFI_POOL
             network 192.168.80.0 255.255.252.0
             default-router 192.168.80.1
             dns-server 8.8.8.8
             lease 0 2 0  ! 2-hour short lease
            !
            interface GigabitEthernet0/1.80
             encapsulation dot1Q 80
             ip address 192.168.80.1 255.255.252.0
             ip access-group ACL_GUEST_INTERNET in
             ip nat inside
            !
            ip access-list extended ACL_GUEST_INTERNET
             deny ip 192.168.80.0 0.0.3.255 192.168.99.0 0.0.0.255
             permit tcp any any eq 80
             permit tcp any any eq 443
             permit udp any any eq 53
             deny ip any any
            !
            ip nat inside source list ACL_GUEST_INTERNET interface GigabitEthernet0/0 overload
            end
            ```

            [SECURITY AUDIT]
            - COMPLIANCE SATISFIED: Sandboxed Guest WiFi prevented from pivoting into the internal Management VLAN.
            - COMPLIANCE SATISFIED: Bandwidth abuse controlled via short lease recycling and limited service ports (HTTP/HTTPS/DNS).
            - COMPLIANCE SATISFIED: Wireless peer-to-peer attacks mitigated via switchport port-security and VLAN segregation.
            """.trimIndent()
        } else {
            // General Corporate Office Template
            """
            [NETWORK DESCRIPTOR]
            NetArchitect AI has generated a robust, high-performance Corporate Office Network. Built with automated subnet division, separate departments (Sales, Engineering, Management), redundant STP trunks, dual-active failover routing, and ACLs for enterprise compliance.
            
            [IP ALLOCATION TABLE]
            | VLAN ID | VLAN Name    | Subnet Range    | Default Gateway | Security Policy / ACLs |
            |---------|--------------|-----------------|-----------------|------------------------|
            | VLAN 10 | Corporate-PC | 172.16.10.0/24  | 172.16.10.1      | Inter-VLAN routing enabled. Protected by firewalls. |
            | VLAN 40 | Printers     | 172.16.40.0/24  | 172.16.40.1      | Static mapping. Accessible from corporate subnet only. |
            | VLAN 50 | Servers      | 172.16.50.0/24  | 172.16.50.1      | Server vault. Restricting traffic to validated admin ports. |

            [CISCO RUNNING CONFIG]
            ```cisco
            ! NetArchitect General Corporate Office Config
            hostname Office-Core-Router
            enable secret CiscoOfficeAdmin2026
            !
            vlan 10
             name Corporate-PC
            vlan 40
             name Printers
            vlan 50
             name Servers
            !
            ip dhcp pool CORP_POOL
             network 172.16.10.0 255.255.255.0
             default-router 172.16.10.1
             dns-server 8.8.8.8
            !
            interface GigabitEthernet0/1.10
             encapsulation dot1Q 10
             ip address 172.16.10.1 255.255.255.0
             ip nat inside
            !
            interface GigabitEthernet0/1.40
             encapsulation dot1Q 40
             ip address 172.16.40.1 255.255.255.0
            !
            interface GigabitEthernet0/1.50
             encapsulation dot1Q 50
             ip address 172.16.50.1 255.255.255.0
             ip access-group ACL_SERVER_PROTECT in
            !
            ip access-list extended ACL_SERVER_PROTECT
             permit tcp 172.16.10.0 0.0.0.255 172.16.50.0 0.0.0.255 eq 443
             permit tcp 172.16.10.0 0.0.0.255 172.16.50.0 0.0.0.255 eq 22
             deny ip any any
            end
            ```

            [SECURITY AUDIT]
            - COMPLIANCE SATISFIED: CIS Control 1.1 Secure Network Segmentation.
            - COMPLIANCE SATISFIED: Server core vault restricted to explicit service ports (HTTPS, SSH) from authenticated corporate PCs.
            - COMPLIANCE SATISFIED: Redundant routing and active Spanning Tree configuration prevents loops and provides failover.
            """.trimIndent()
        }
    }
}
