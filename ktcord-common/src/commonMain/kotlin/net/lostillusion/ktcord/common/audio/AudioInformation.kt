package net.lostillusion.ktcord.common.audio

data class AudioInformation(
        val serverId: Long,
        val userId: Long,
        val sessionId: String,
        val token: String,
        val endpoint: String
)
