package com.telemetrico.app.model

import kotlin.math.roundToInt

/** Normalised, version-agnostic state consumed by the dashboard UI. */
data class TelemetryState(
    // Live car telemetry
    val speedKph: Int = 0,
    val throttle: Float = 0f,
    val brake: Float = 0f,
    val gear: Int = 0,
    val engineRpm: Int = 0,
    val drsActive: Boolean = false,
    val revLightsPercent: Int = 0,
    val revLightsBits: Int = 0,
    val innerTyreTemps: List<Int> = listOf(0, 0, 0, 0), // RL, RR, FL, FR

    // Race / lap data
    val lastLapTimeMs: Long = 0L,
    val currentLapTimeMs: Long = 0L,
    val sector1TimeMs: Long = 0L,
    val sector2TimeMs: Long = 0L,
    val currentSector: Int = 0,
    val currentLapInvalid: Boolean = false,
    val gapAheadMs: Long = 0L,
    val gapLeaderMs: Long = 0L,
    val position: Int = 0,
    val currentLap: Int = 0,
    val totalLaps: Int = 0,
    val penaltiesSeconds: Int = 0,
    val totalWarnings: Int = 0,
    val cornerCuttingWarnings: Int = 0,
    val unservedDriveThrough: Int = 0,
    val unservedStopGo: Int = 0,
    val speedTrapFastestKph: Float = 0f,

    // Car status
    val pitLimiterActive: Boolean = false,
    val fuelInTank: Float = 0f,
    val fuelCapacity: Float = 0f,
    /** F1 MFD fuel delta expressed in laps; positive means surplus to race finish. */
    val fuelRemainingLaps: Float = 0f,
    val maxRpm: Int = 0,
    val drsAllowed: Boolean = false,
    val drsActivationDistanceM: Int = 0,
    val actualTyreCompound: Int = 0,
    val visualTyreCompound: Int = 0,
    val tyreAgeLaps: Int = 0,
    val fiaFlag: Int = -1,
    val ersStoreEnergyJ: Float = 0f,
    val ersDeployMode: Int = 0,

    // Tyres / history
    val tyreWear: List<Float> = listOf(0f, 0f, 0f, 0f), // RL, RR, FL, FR
    val tyreLifeSpanLaps: Int = 0,
    val tyreUsableLifeLaps: Int = 0,
    val bestLapTimeMs: Long = 0L,
    val bestSector1Ms: Long = 0L,
    val bestSector2Ms: Long = 0L,
    val bestSector3Ms: Long = 0L,

    // Participant / session identity
    val driverName: String = "DRIVER",
    val teamName: String = "F1",
    val raceNumber: Int = 0,
    val sessionType: Int = 0,
    val trackId: Int = -1,
    val trackName: String = "TRACK",
    val weather: Int = 0,
    val trackTemperatureC: Int = 0,
    val airTemperatureC: Int = 0,
    val safetyCarStatus: Int = 0,

    // Internal routing metadata (not displayed)
    val playerCarIndex: Int = 0,
    val frameIdentifier: Long = 0,
    val sessionUid: ULong = 0u,
) {
    val fuelPercent: Int
        get() = if (fuelCapacity > 0f) ((fuelInTank / fuelCapacity) * 100f).coerceIn(0f, 100f).roundToInt() else 0

    /** Estimated usable fuel laps: laps still to complete plus/minus the game's MFD fuel delta. */
    val estimatedFuelLapsAvailable: Float
        get() {
            if (totalLaps <= 0 || currentLap <= 0) return fuelRemainingLaps.coerceAtLeast(0f)
            val raceLapsRemaining = (totalLaps - currentLap).coerceAtLeast(0)
            return (raceLapsRemaining + fuelRemainingLaps).coerceAtLeast(0f)
        }

    /** F1 hybrid energy store usable capacity is represented as ~4 MJ in the game telemetry. */
    val ersPercent: Int
        get() = ((ersStoreEnergyJ / ERS_MAX_STORE_J) * 100f).coerceIn(0f, 100f).roundToInt()

    val currentSector3TimeMs: Long
        get() = if (currentSector == 2 && sector1TimeMs > 0 && sector2TimeMs > 0) {
            (currentLapTimeMs - sector1TimeMs - sector2TimeMs).coerceAtLeast(0L)
        } else 0L

    val tyreCompoundLabel: String
        get() = when (visualTyreCompound) {
            16 -> "SOFT"
            17 -> "MEDIUM"
            18 -> "HARD"
            7 -> "INTER"
            8 -> "WET"
            else -> if (visualTyreCompound > 0) "C$visualTyreCompound" else "—"
        }

    val ersModeLabel: String
        get() = when (ersDeployMode) {
            1 -> "MEDIUM"
            2 -> "HOTLAP"
            3 -> "OVERTAKE"
            else -> "NONE"
        }

    val weatherLabel: String
        get() = when (weather) {
            0 -> "CLEAR"
            1 -> "LIGHT CLOUD"
            2 -> "OVERCAST"
            3 -> "LIGHT RAIN"
            4 -> "HEAVY RAIN"
            5 -> "STORM"
            else -> "WEATHER"
        }

    val weatherSymbol: String
        get() = when (weather) {
            0 -> "☀"
            1 -> "◐"
            2 -> "☁"
            3, 4 -> "☂"
            5 -> "⚡"
            else -> "•"
        }

    val fiaFlagLabel: String?
        get() = when (fiaFlag) {
            1 -> "GREEN FLAG"
            2 -> "BLUE FLAG"
            3 -> "YELLOW FLAG"
            else -> null
        }

    val safetyCarLabel: String?
        get() = when (safetyCarStatus) {
            1 -> "SAFETY CAR"
            2 -> "VIRTUAL SAFETY CAR"
            3 -> "FORMATION LAP"
            else -> null
        }

    companion object {
        const val ERS_MAX_STORE_J = 4_000_000f
    }
}

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
