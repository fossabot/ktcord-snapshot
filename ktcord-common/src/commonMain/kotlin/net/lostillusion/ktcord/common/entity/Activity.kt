package net.lostillusion.ktcord.common.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.lostillusion.ktcord.common.enums.ActivityType

// TODO: use kotlinx datetime

@Serializable
class Activity internal constructor(
        val name: String,
        val type: ActivityType,
        val url: String?,
        @SerialName("created_at") val createdAt: Long?
)

class ActivityBuilder {
    var name: String = ""
    var url: String? = null
    var createdAt: Long? = null
    private var type: ActivityType? = null

    fun type(activityType: ActivityType) {
        type = activityType
    }

    fun build(): Activity {
        require(type != null) { "Type of Activity must not be set!" }
        return Activity(name, type!!, url, createdAt)
    }
}