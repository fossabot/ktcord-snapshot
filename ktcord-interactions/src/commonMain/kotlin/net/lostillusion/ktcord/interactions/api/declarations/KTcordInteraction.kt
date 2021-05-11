package net.lostillusion.ktcord.interactions.api.declarations

import net.lostillusion.ktcord.interactions.models.CreateApplicationCommand

sealed interface KTcordInteraction {
    val command: CreateApplicationCommand
}