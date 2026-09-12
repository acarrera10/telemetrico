package com.telemetrico.app.network

import java.net.Inet4Address
import java.net.NetworkInterface

object LocalIpResolver {
    fun resolveIpv4(): String? {
        return runCatching {
            val candidates = NetworkInterface.getNetworkInterfaces().toList()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { network ->
                    network.inetAddresses.toList()
                        .filterIsInstance<Inet4Address>()
                        .filter { !it.isLoopbackAddress }
                        .map { network.name to it }
                }

            candidates.firstOrNull { (name, address) ->
                (name.startsWith("wlan") || name.startsWith("wifi")) && address.isSiteLocalAddress
            }?.second?.hostAddress
                ?: candidates.firstOrNull { (_, address) -> address.isSiteLocalAddress }?.second?.hostAddress
                ?: candidates.firstOrNull()?.second?.hostAddress
        }.getOrNull()
    }
}

private fun <T> java.util.Enumeration<T>.toList(): List<T> = buildList {
    while (this@toList.hasMoreElements()) add(this@toList.nextElement())
}
