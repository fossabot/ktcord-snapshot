package net.lostillusion.ktcord.common.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AllowedMentions(
    val parse: List<JsonObject> = listOf()
)