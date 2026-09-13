package com.telemetrico.app.network

import com.telemetrico.app.model.ConnectionPhase
import com.telemetrico.app.model.ConnectionState
import com.telemetrico.app.model.TelemetryState
import com.telemetrico.app.protocol.F125Parser
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class UdpTelemetryReceiver(
    private val port: Int = 20777,
    private val onConnection: (ConnectionState) -> Unit,
    private val onTelemetry: (TelemetryState) -> Unit,
) {
    private val running = AtomicBoolean(false)
    /** Last Car Telemetry packet (packet id 6), used to decide whether the car is actually live. */
    private val lastLiveTelemetryAt = AtomicLong(0L)
    private val latestTelemetry = AtomicReference(TelemetryState())
    private val telemetryActive = AtomicBoolean(false)
    private var socket: DatagramSocket? = null
    private var receiveThread: Thread? = null
    private var monitorThread: Thread? = null

    fun start() {
        if (!running.compareAndSet(false, true)) return

        val ip = LocalIpResolver.resolveIpv4()
        onConnection(ConnectionState(ConnectionPhase.STARTING, tabletIp = ip, port = port))

        receiveThread = Thread({ receiveLoop(ip) }, "telemetrico-udp-receiver").apply {
            isDaemon = true
            start()
        }
        monitorThread = Thread({ monitorLoop(ip) }, "telemetrico-udp-monitor").apply {
            isDaemon = true
            start()
        }
    }

    fun stop() {
        running.set(false)
        socket?.close()
        socket = null
        receiveThread = null
        monitorThread = null
    }

    private fun receiveLoop(ip: String?) {
        try {
            val datagramSocket = DatagramSocket(port).also {
                it.reuseAddress = true
                it.receiveBufferSize = 256 * 1024
            }
            socket = datagramSocket
            onConnection(ConnectionState(ConnectionPhase.LISTENING, tabletIp = ip, port = port))

            val buffer = ByteArray(2048)
            val datagram = DatagramPacket(buffer, buffer.size)
            var lastUiPublishNanos = 0L
            var currentState = TelemetryState()

            while (running.get()) {
                datagram.length = buffer.size
                datagramSocket.receive(datagram)
                val result = F125Parser.parse(datagram.data, datagram.length, currentState) ?: continue

                if (result.header.packetFormat != F125Parser.PACKET_FORMAT) {
                    onConnection(
                        ConnectionState(
                            phase = ConnectionPhase.UNSUPPORTED_FORMAT,
                            tabletIp = ip,
                            port = port,
                            packetFormat = result.header.packetFormat,
                            message = "Formato UDP ${result.header.packetFormat} detectado. Seleccioná F1 25 / 2025 en el juego.",
                        )
                    )
                    continue
                }

                if (result.stateChanged) {
                    currentState = result.telemetry
                    latestTelemetry.set(currentState)
                }

                // Keep collecting all packet types, but only a live Car Telemetry packet means
                // the player is actually on track. This preserves the v0.3 auto-transition behavior.
                if (result.header.packetId == 6) {
                    val nowMillis = System.currentTimeMillis()
                    lastLiveTelemetryAt.set(nowMillis)
                    if (telemetryActive.compareAndSet(false, true)) {
                        onConnection(
                            ConnectionState(
                                phase = ConnectionPhase.RECEIVING,
                                tabletIp = ip,
                                port = port,
                                lastPacketAtMillis = nowMillis,
                                packetFormat = result.header.packetFormat,
                            )
                        )
                    }
                }

                if (telemetryActive.get()) {
                    val nowNanos = System.nanoTime()
                    if (nowNanos - lastUiPublishNanos >= 33_000_000L) {
                        onTelemetry(latestTelemetry.get())
                        lastUiPublishNanos = nowNanos
                    }
                }
            }
        } catch (_: SocketException) {
            if (running.get()) publishError(ip, "No se pudo abrir el puerto UDP $port")
        } catch (t: Throwable) {
            if (running.get()) publishError(ip, t.message ?: "Error inesperado escuchando UDP")
        } finally {
            socket?.close()
        }
    }

    private fun monitorLoop(ip: String?) {
        while (running.get()) {
            try {
                Thread.sleep(500)
                val last = lastLiveTelemetryAt.get()
                if (last > 0 && System.currentTimeMillis() - last > 2500L) {
                    lastLiveTelemetryAt.set(0L)
                    telemetryActive.set(false)
                    onConnection(
                        ConnectionState(
                            phase = ConnectionPhase.LISTENING,
                            tabletIp = ip,
                            port = port,
                            message = "Esperando paquetes de telemetría de carrera de F1 25",
                        )
                    )
                }
            } catch (_: InterruptedException) {
                return
            }
        }
    }

    private fun publishError(ip: String?, message: String) {
        onConnection(
            ConnectionState(
                phase = ConnectionPhase.ERROR,
                tabletIp = ip,
                port = port,
                message = message,
            )
        )
    }
}
