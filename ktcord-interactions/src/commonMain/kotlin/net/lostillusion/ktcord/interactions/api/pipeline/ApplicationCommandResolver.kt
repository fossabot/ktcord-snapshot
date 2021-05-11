package net.lostillusion.ktcord.interactions.api.pipeline

import net.lostillusion.ktcord.interactions.models.responses.ApplicationCommandInteractionData

interface ApplicationCommandResolver {
    fun resolve(interactionData: ApplicationCommandInteractionData)
}