package net.lostillusion.ktcord.interactions.receiving

import net.lostillusion.ktcord.interactions.api.declarations.KTcordInteraction
import net.lostillusion.ktcord.interactions.models.responses.ApplicationCommandInteractionData

interface ApplicationCommandResolver {
    fun resolve(data: ApplicationCommandInteractionData) : KTcordInteraction
}