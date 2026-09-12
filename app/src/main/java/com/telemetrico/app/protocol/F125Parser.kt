package com.telemetrico.app.protocol

import com.telemetrico.app.model.TelemetryState
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Parser for the original F1 25 / 2025 UDP format.
 *
 * EA specification:
 * - PacketHeader: 29 bytes
 * - Car Telemetry packet id: 6
 * - CarTelemetryData: 60 bytes per car
 * - PacketCarTelemetryData: 1352 bytes
 * - 22 cars maximum
 * - Little-endian packed structures
 */
object F125Parser {
    const val PACKET_FORMAT = 2025
    const val PACKET_ID_CAR_TELEMETRY = 6
    const val HEADER_SIZE = 29
    const val CAR_TELEMETRY_DATA_SIZE = 60
    const val MAX_CARS = 22
    const val CAR_TELEMETRY_PACKET_SIZE = 1352

    data class ParseResult(
        val header: F1PacketHeader,
        val telemetry: TelemetryState? = null,
    )

    fun parse(packet: ByteArray, length: Int = packet.size): ParseResult? {
        if (length < HEADER_SIZE) return null
        val buffer = ByteBuffer.wrap(packet, 0, length).order(ByteOrder.LITTLE_ENDIAN)
        val header = parseHeader(buffer)

        if (header.packetFormat != PACKET_FORMAT) {
            return ParseResult(header = header)
        }

        if (header.packetId != PACKET_ID_CAR_TELEMETRY) {
            return ParseResult(header = header)
        }

        if (length < CAR_TELEMETRY_PACKET_SIZE) return null
        if (header.playerCarIndex !in 0 until MAX_CARS) return null

        val offset = HEADER_SIZE + (header.playerCarIndex * CAR_TELEMETRY_DATA_SIZE)
        buffer.position(offset)

        val speed = buffer.u16()
        val throttle = buffer.float.coerceIn(0f, 1f)
        buffer.float // steer (parsed/consumed, not displayed in v0.3)
        val brake = buffer.float.coerceIn(0f, 1f)
        buffer.u8() // clutch
        val gear = buffer.get().toInt()
        val rpm = buffer.u16()
        val drs = buffer.u8() == 1
        val revPercent = buffer.u8()
        val revBits = buffer.u16()

        repeat(4) { buffer.u16() } // brake temperatures
        repeat(4) { buffer.u8() } // tyre surface temperatures
        val innerTemps = List(4) { buffer.u8() }

        return ParseResult(
            header = header,
            telemetry = TelemetryState(
                speedKph = speed,
                throttle = throttle,
                brake = brake,
                gear = gear,
                engineRpm = rpm,
                drsActive = drs,
                revLightsPercent = revPercent,
                revLightsBits = revBits,
                innerTyreTemps = innerTemps,
                playerCarIndex = header.playerCarIndex,
                frameIdentifier = header.frameIdentifier,
                sessionUid = header.sessionUid,
            )
        )
    }

    private fun parseHeader(buffer: ByteBuffer): F1PacketHeader {
        val packetFormat = buffer.u16()
        val gameYear = buffer.u8()
        val major = buffer.u8()
        val minor = buffer.u8()
        val packetVersion = buffer.u8()
        val packetId = buffer.u8()
        val sessionUid = buffer.long.toULong()
        val sessionTime = buffer.float
        val frameIdentifier = buffer.u32()
        val overallFrameIdentifier = buffer.u32()
        val playerCarIndex = buffer.u8()
        val secondaryPlayerCarIndex = buffer.u8()
        return F1PacketHeader(
            packetFormat = packetFormat,
            gameYear = gameYear,
            gameMajorVersion = major,
            gameMinorVersion = minor,
            packetVersion = packetVersion,
            packetId = packetId,
            sessionUid = sessionUid,
            sessionTime = sessionTime,
            frameIdentifier = frameIdentifier,
            overallFrameIdentifier = overallFrameIdentifier,
            playerCarIndex = playerCarIndex,
            secondaryPlayerCarIndex = secondaryPlayerCarIndex,
        )
    }

    private fun ByteBuffer.u8(): Int = get().toInt() and 0xFF
    private fun ByteBuffer.u16(): Int = short.toInt() and 0xFFFF
    private fun ByteBuffer.u32(): Long = int.toLong() and 0xFFFFFFFFL
}
