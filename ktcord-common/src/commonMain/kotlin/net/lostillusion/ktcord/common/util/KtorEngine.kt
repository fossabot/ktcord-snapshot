package net.lostillusion.ktcord.common.util

import io.ktor.client.engine.*

expect object Platform {
    val engine: HttpClientEngineFactory<*>
}
