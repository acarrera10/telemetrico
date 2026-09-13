package com.telemetrico.app.protocol

import com.telemetrico.app.model.TelemetryState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * Parser for F1 25 / packetFormat 2025.
 * All protocol-specific fields are normalised into TelemetryState so the UI stays version-agnostic.
 */
object F125Parser {
    const val PACKET_FORMAT = 2025
    const val HEADER_SIZE = 29
    const val MAX_CARS = 22

    private const val PACKET_ID_SESSION = 1
    private const val PACKET_ID_LAP_DATA = 2
    private const val PACKET_ID_PARTICIPANTS = 4
    private const val PACKET_ID_CAR_TELEMETRY = 6
    private const val PACKET_ID_CAR_STATUS = 7
    private const val PACKET_ID_CAR_DAMAGE = 10
    private const val PACKET_ID_SESSION_HISTORY = 11
    private const val PACKET_ID_TYRE_SETS = 12

    private const val LAP_DATA_SIZE = 57
    private const val CAR_TELEMETRY_DATA_SIZE = 60
    private const val CAR_STATUS_DATA_SIZE = 55
    private const val CAR_DAMAGE_DATA_SIZE = 46
    private const val PARTICIPANT_DATA_SIZE = 57
    private const val LAP_HISTORY_DATA_SIZE = 14
    private const val TYRE_SET_DATA_SIZE = 10

    data class ParseResult(
        val header: F1PacketHeader,
        val telemetry: TelemetryState,
        val stateChanged: Boolean,
    )

    fun parse(packet: ByteArray, length: Int = packet.size, current: TelemetryState = TelemetryState()): ParseResult? {
        if (length < HEADER_SIZE) return null
        val headerBuffer = ByteBuffer.wrap(packet, 0, length).order(ByteOrder.LITTLE_ENDIAN)
        val header = parseHeader(headerBuffer)

        if (header.packetFormat != PACKET_FORMAT) {
            return ParseResult(header, current, false)
        }

        if (header.playerCarIndex !in 0 until MAX_CARS) {
            return ParseResult(header, current, false)
        }

        val base = if (current.sessionUid != 0uL && current.sessionUid != header.sessionUid) {
            TelemetryState()
        } else current

        val common = base.copy(
            playerCarIndex = header.playerCarIndex,
            frameIdentifier = header.frameIdentifier,
            sessionUid = header.sessionUid,
        )

        val updated = when (header.packetId) {
            PACKET_ID_SESSION -> parseSession(packet, length, common)
            PACKET_ID_LAP_DATA -> parseLapData(packet, length, header.playerCarIndex, common)
            PACKET_ID_PARTICIPANTS -> parseParticipants(packet, length, header.playerCarIndex, common)
            PACKET_ID_CAR_TELEMETRY -> parseCarTelemetry(packet, length, header.playerCarIndex, common)
            PACKET_ID_CAR_STATUS -> parseCarStatus(packet, length, header.playerCarIndex, common)
            PACKET_ID_CAR_DAMAGE -> parseCarDamage(packet, length, header.playerCarIndex, common)
            PACKET_ID_SESSION_HISTORY -> parseSessionHistory(packet, length, header.playerCarIndex, common)
            PACKET_ID_TYRE_SETS -> parseTyreSets(packet, length, header.playerCarIndex, common)
            else -> common
        }

        return ParseResult(header, updated, updated != current)
    }

    private fun parseSession(packet: ByteArray, length: Int, current: TelemetryState): TelemetryState {
        if (length < 154) return current
        val b = buffer(packet, length)
        b.position(29)
        b.u8()
        b.s8()
        b.s8()
        val totalLaps = b.u8()
        b.u16()
        val sessionType = b.u8()
        val trackId = b.s8()
        b.u8()
        b.u16()
        b.u16()
        b.u8()
        b.u8()
        b.u8()
        b.u8()
        b.u8()
        b.u8()
        b.position(b.position() + 21 * 5)
        val safetyCarStatus = b.u8()
        return current.copy(
            totalLaps = totalLaps,
            sessionType = sessionType,
            trackId = trackId,
            trackName = trackName(trackId),
            safetyCarStatus = safetyCarStatus,
        )
    }

    private fun parseLapData(packet: ByteArray, length: Int, playerIdx: Int, current: TelemetryState): TelemetryState {
        val minSize = HEADER_SIZE + MAX_CARS * LAP_DATA_SIZE + 2
        if (length < minSize) return current
        val b = buffer(packet, length)
        b.position(HEADER_SIZE + playerIdx * LAP_DATA_SIZE)

        val lastLap = b.u32()
        val currentLapTime = b.u32()
        val s1 = sectorTime(b.u16(), b.u8())
        val s2 = sectorTime(b.u16(), b.u8())
        val gapAhead = sectorTime(b.u16(), b.u8())
        val gapLeader = sectorTime(b.u16(), b.u8())
        b.float
        b.float
        b.float
        val position = b.u8()
        val currentLap = b.u8()
        b.u8()
        b.u8()
        val sector = b.u8()
        val invalid = b.u8() == 1
        val penalties = b.u8()
        val warnings = b.u8()
        val cornerWarnings = b.u8()
        val driveThrough = b.u8()
        val stopGo = b.u8()
        b.u8()
        b.u8()
        b.u8()
        b.u8()
        b.u16()
        b.u16()
        b.u8()
        val speedTrap = b.float
        b.u8()

        return current.copy(
            lastLapTimeMs = lastLap,
            currentLapTimeMs = currentLapTime,
            sector1TimeMs = s1,
            sector2TimeMs = s2,
            currentSector = sector,
            currentLapInvalid = invalid,
            gapAheadMs = gapAhead,
            gapLeaderMs = gapLeader,
            position = position,
            currentLap = currentLap,
            penaltiesSeconds = penalties,
            totalWarnings = warnings,
            cornerCuttingWarnings = cornerWarnings,
            unservedDriveThrough = driveThrough,
            unservedStopGo = stopGo,
            speedTrapFastestKph = speedTrap,
        )
    }

    private fun parseCarTelemetry(packet: ByteArray, length: Int, playerIdx: Int, current: TelemetryState): TelemetryState {
        if (length < 1352) return current
        val b = buffer(packet, length)
        b.position(HEADER_SIZE + playerIdx * CAR_TELEMETRY_DATA_SIZE)

        val speed = b.u16()
        val throttle = b.float.coerceIn(0f, 1f)
        b.float
        val brake = b.float.coerceIn(0f, 1f)
        b.u8()
        val gear = b.s8()
        val rpm = b.u16()
        val drs = b.u8() == 1
        val revPercent = b.u8().coerceIn(0, 100)
        val revBits = b.u16()
        repeat(4) { b.u16() }
        repeat(4) { b.u8() }
        val innerTemps = List(4) { b.u8() }

        return current.copy(
            speedKph = speed,
            throttle = throttle,
            brake = brake,
            gear = gear,
            engineRpm = rpm,
            drsActive = drs,
            revLightsPercent = revPercent,
            revLightsBits = revBits,
            innerTyreTemps = innerTemps,
        )
    }

    private fun parseCarStatus(packet: ByteArray, length: Int, playerIdx: Int, current: TelemetryState): TelemetryState {
        if (length < 1239) return current
        val b = buffer(packet, length)
        b.position(HEADER_SIZE + playerIdx * CAR_STATUS_DATA_SIZE)
        b.u8()
        b.u8()
        b.u8()
        b.u8()
        val pitLimiter = b.u8() == 1
        val fuel = b.float
        val capacity = b.float
        val fuelLaps = b.float
        val maxRpm = b.u16()
        b.u16()
        b.u8()
        val drsAllowed = b.u8() == 1
        val drsDistance = b.u16()
        val actualCompound = b.u8()
        val visualCompound = b.u8()
        val tyreAge = b.u8()
        val fiaFlag = b.s8()
        b.float
        b.float
        val ersEnergy = b.float
        val ersMode = b.u8()
        b.float
        b.float
        b.float
        b.u8()

        return current.copy(
            pitLimiterActive = pitLimiter,
            fuelInTank = fuel,
            fuelCapacity = capacity,
            fuelRemainingLaps = fuelLaps,
            maxRpm = maxRpm,
            drsAllowed = drsAllowed,
            drsActivationDistanceM = drsDistance,
            actualTyreCompound = actualCompound,
            visualTyreCompound = visualCompound,
            tyreAgeLaps = tyreAge,
            fiaFlag = fiaFlag,
            ersStoreEnergyJ = ersEnergy,
            ersDeployMode = ersMode,
        )
    }

    private fun parseCarDamage(packet: ByteArray, length: Int, playerIdx: Int, current: TelemetryState): TelemetryState {
        if (length < 1041) return current
        val b = buffer(packet, length)
        b.position(HEADER_SIZE + playerIdx * CAR_DAMAGE_DATA_SIZE)
        val wear = List(4) { b.float.coerceIn(0f, 100f) }
        return current.copy(tyreWear = wear)
    }

    private fun parseParticipants(packet: ByteArray, length: Int, playerIdx: Int, current: TelemetryState): TelemetryState {
        if (length < 1284) return current
        val b = buffer(packet, length)
        b.position(HEADER_SIZE)
        val activeCars = b.u8()
        if (playerIdx >= activeCars) return current
        b.position(HEADER_SIZE + 1 + playerIdx * PARTICIPANT_DATA_SIZE)
        b.u8()
        b.u8()
        b.u8()
        val teamId = b.u8()
        b.u8()
        val raceNumber = b.u8()
        b.u8()
        val nameBytes = ByteArray(32)
        b.get(nameBytes)
        val nullIndex = nameBytes.indexOf(0)
        val nameLength = if (nullIndex >= 0) nullIndex else nameBytes.size
        val name = String(nameBytes, 0, nameLength, StandardCharsets.UTF_8).trim().ifBlank { "DRIVER" }

        return current.copy(
            driverName = name,
            teamName = teamName(teamId),
            raceNumber = raceNumber,
        )
    }

    private fun parseSessionHistory(packet: ByteArray, length: Int, playerIdx: Int, current: TelemetryState): TelemetryState {
        if (length < 1460) return current
        val b = buffer(packet, length)
        b.position(HEADER_SIZE)
        val carIdx = b.u8()
        if (carIdx != playerIdx) return current
        val numLaps = b.u8()
        b.u8()
        val bestLapNum = b.u8()
        val bestS1LapNum = b.u8()
        val bestS2LapNum = b.u8()
        val bestS3LapNum = b.u8()
        val historyBase = b.position()

        fun record(lapNum: Int): LapHistoryRecord? {
            if (lapNum !in 1..numLaps.coerceAtMost(100)) return null
            val r = buffer(packet, length)
            r.position(historyBase + (lapNum - 1) * LAP_HISTORY_DATA_SIZE)
            val lap = r.u32()
            val s1 = sectorTime(r.u16(), r.u8())
            val s2 = sectorTime(r.u16(), r.u8())
            val s3 = sectorTime(r.u16(), r.u8())
            r.u8()
            return LapHistoryRecord(lap, s1, s2, s3)
        }

        return current.copy(
            bestLapTimeMs = record(bestLapNum)?.lapMs ?: current.bestLapTimeMs,
            bestSector1Ms = record(bestS1LapNum)?.s1Ms ?: current.bestSector1Ms,
            bestSector2Ms = record(bestS2LapNum)?.s2Ms ?: current.bestSector2Ms,
            bestSector3Ms = record(bestS3LapNum)?.s3Ms ?: current.bestSector3Ms,
        )
    }

    private fun parseTyreSets(packet: ByteArray, length: Int, playerIdx: Int, current: TelemetryState): TelemetryState {
        if (length < 231) return current
        val b = buffer(packet, length)
        b.position(HEADER_SIZE)
        val carIdx = b.u8()
        if (carIdx != playerIdx) return current
        val fittedIdxOffset = HEADER_SIZE + 1 + 20 * TYRE_SET_DATA_SIZE
        val fittedIdx = packet[fittedIdxOffset].toInt() and 0xFF
        if (fittedIdx !in 0 until 20) return current

        b.position(HEADER_SIZE + 1 + fittedIdx * TYRE_SET_DATA_SIZE)
        b.u8()
        b.u8()
        b.u8()
        b.u8()
        b.u8()
        val lifeSpan = b.u8()
        val usableLife = b.u8()
        return current.copy(
            tyreLifeSpanLaps = lifeSpan,
            tyreUsableLifeLaps = usableLife,
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

    private data class LapHistoryRecord(val lapMs: Long, val s1Ms: Long, val s2Ms: Long, val s3Ms: Long)

    private fun sectorTime(msPart: Int, minutesPart: Int): Long = minutesPart * 60_000L + msPart.toLong()

    private fun teamName(id: Int): String = when (id) {
        0 -> "MERCEDES"
        1 -> "FERRARI"
        2 -> "RED BULL RACING"
        3 -> "WILLIAMS"
        4 -> "ASTON MARTIN"
        5 -> "ALPINE"
        6 -> "RB"
        7 -> "HAAS"
        8 -> "McLAREN"
        9 -> "SAUBER"
        41 -> "F1 GENERIC"
        104 -> "CUSTOM TEAM"
        129, 155 -> "KONNERSPORT"
        142, 154 -> "APXGP"
        185 -> "MERCEDES '24"
        186 -> "FERRARI '24"
        187 -> "RED BULL '24"
        188 -> "WILLIAMS '24"
        189 -> "ASTON MARTIN '24"
        190 -> "ALPINE '24"
        191 -> "RB '24"
        192 -> "HAAS '24"
        193 -> "McLAREN '24"
        194 -> "SAUBER '24"
        else -> "TEAM $id"
    }

    private fun trackName(id: Int): String = when (id) {
        0 -> "MELBOURNE"
        2 -> "SHANGHAI"
        3 -> "BAHRAIN"
        4 -> "CATALUNYA"
        5 -> "MONACO"
        6 -> "MONTREAL"
        7 -> "SILVERSTONE"
        9 -> "HUNGARORING"
        10 -> "SPA"
        11 -> "MONZA"
        12 -> "SINGAPORE"
        13 -> "SUZUKA"
        14 -> "ABU DHABI"
        15 -> "TEXAS"
        16 -> "BRAZIL"
        17 -> "AUSTRIA"
        19 -> "MEXICO"
        20 -> "BAKU"
        26 -> "ZANDVOORT"
        27 -> "IMOLA"
        29 -> "JEDDAH"
        30 -> "MIAMI"
        31 -> "LAS VEGAS"
        32 -> "LOSAIL"
        39 -> "SILVERSTONE REV."
        40 -> "AUSTRIA REV."
        41 -> "ZANDVOORT REV."
        else -> if (id >= 0) "TRACK $id" else "TRACK"
    }

    private fun buffer(packet: ByteArray, length: Int): ByteBuffer =
        ByteBuffer.wrap(packet, 0, length).order(ByteOrder.LITTLE_ENDIAN)

    private fun ByteBuffer.u8(): Int = get().toInt() and 0xFF
    private fun ByteBuffer.s8(): Int = get().toInt()
    private fun ByteBuffer.u16(): Int = short.toInt() and 0xFFFF
    private fun ByteBuffer.u32(): Long = int.toLong() and 0xFFFFFFFFL
}
