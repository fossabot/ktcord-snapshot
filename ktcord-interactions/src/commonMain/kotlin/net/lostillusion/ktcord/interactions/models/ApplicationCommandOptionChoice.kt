package net.lostillusion.ktcord.interactions.models

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationCommandOptionChoice(
    val name: String,
    val value: String
) {
    init {
        require(name.length in 1..100)
    }
}