package com.telemetrico.app.protocol

data class F1PacketHeader(
    val packetFormat: Int,
    val gameYear: Int,
    val gameMajorVersion: Int,
    val gameMinorVersion: Int,
    val packetVersion: Int,
    val packetId: Int,
    val sessionUid: ULong,
    val sessionTime: Float,
    val frameIdentifier: Long,
    val overallFrameIdentifier: Long,
    val playerCarIndex: Int,
    val secondaryPlayerCarIndex: Int,
)
