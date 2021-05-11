package net.lostillusion.ktcord.common.util

import io.ktor.client.engine.*
import io.ktor.client.engine.java.*

actual object Platform {
    actual val engine: HttpClientEngineFactory<*> = Java
}
