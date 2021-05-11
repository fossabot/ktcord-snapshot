package net.lostillusion.ktcord.interactions.models.responses

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import net.lostillusion.ktcord.interactions.api.entity.PartialChannel
import net.lostillusion.ktcord.interactions.api.entity.PartialMember
import net.lostillusion.ktcord.interactions.api.entity.PartialRole
import net.lostillusion.ktcord.interactions.api.entity.PartialUser

@Serializable data class ApplicationCommandInteractionDataResolved(
    val users: Map<Long, PartialUser>? = null,
    val members: Map<Long, PartialMember>? = null,
    val roles: Map<Long, PartialRole>? = null,
    val channels: Map<Long, PartialChannel>? = null
)