package net.lostillusion.ktcord.interactions.api.pipeline

import net.lostillusion.ktcord.interactions.receiving.webhook.ApplicationCallAdapter
import net.lostillusion.ktcord.interactions.receiving.webhook.types.InteractionRequest

@KtcordPipelineApi
class InteractionCall(
//    val discordInteractionPipeline: DiscordInteractionPipeline,
    val request: InteractionRequest,
    val call: ApplicationCallAdapter
)