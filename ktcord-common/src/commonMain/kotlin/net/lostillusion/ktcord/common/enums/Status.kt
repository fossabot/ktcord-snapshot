package net.lostillusion.ktcord.common.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Status {
    @SerialName("online")
    ONLINE,

    @SerialName("dnd")
    DO_NOT_DISTURB,

    @SerialName("idle")
    IDLE,

    @SerialName("invisible")
    INVISIBLE,

    @SerialName("offline")
    OFFLINE
}