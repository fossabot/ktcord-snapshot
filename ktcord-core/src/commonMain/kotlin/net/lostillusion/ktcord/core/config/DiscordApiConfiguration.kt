package net.lostillusion.ktcord.core.config

data class DiscordApiConfiguration(
        val token: String
)

data class DiscordApiConfigurationBuilder(
        var token: String,
//    var masterGatewayConfiguration: MasterGatewayConfiguration
)