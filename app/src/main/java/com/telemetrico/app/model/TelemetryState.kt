package com.telemetrico.app.model

data class TelemetryState(
    val speedKph: Int = 0,
    val throttle: Float = 0f,
    val brake: Float = 0f,
    val gear: Int = 0,
    val engineRpm: Int = 0,
    val drsActive: Boolean = false,
    val revLightsPercent: Int = 0,
    val revLightsBits: Int = 0,
    val innerTyreTemps: List<Int> = listOf(0, 0, 0, 0), // RL, RR, FL, FR
    val playerCarIndex: Int = 0,
    val frameIdentifier: Long = 0,
    val sessionUid: ULong = 0u,
)

enum class ConnectionPhase {
    STARTING,
    LISTENING,
    RECEIVING,
    UNSUPPORTED_FORMAT,
    ERROR,
}

data class ConnectionState(
    val phase: ConnectionPhase = ConnectionPhase.STARTING,
    val tabletIp: String? = null,
    val port: Int = 20777,
    val lastPacketAtMillis: Long = 0L,
    val packetFormat: Int? = null,
    val message: String? = null,
) {
    val telemetryActive: Boolean get() = phase == ConnectionPhase.RECEIVING
}
