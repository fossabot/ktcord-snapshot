package net.lostillusion.ktcord.interactions.api.entity

data class User(
    val user: PartialUser,
    val member: PartialMember?
)