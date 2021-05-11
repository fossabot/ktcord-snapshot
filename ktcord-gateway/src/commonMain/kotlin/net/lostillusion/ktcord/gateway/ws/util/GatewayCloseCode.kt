package net.lostillusion.ktcord.gateway.ws.util

enum class GatewayCloseCode(val code: Short, val shouldReconnect: Boolean, val shouldResume: Boolean, val clientError: Boolean) {
    UNKNOWN_ERROR(4000, shouldReconnect = true, shouldResume = true, clientError = false),
    UNKNOWN_OPCODE(4001, shouldReconnect = false, shouldResume = false, clientError = true),
    DECODE_ERROR(4002, shouldReconnect = false, shouldResume = false, clientError = true),
    NOT_AUTHENTICATED(4003, shouldReconnect = false, shouldResume = false, clientError = true),
    AUTHENTICATION_FAILED(4004, shouldReconnect = false, shouldResume = false, clientError = true),
    ALREADY_AUTHENTICATED(4005, shouldReconnect = true, shouldResume = true, clientError = true),
    INVALID_SEQ(4007, shouldReconnect = true, shouldResume = false, clientError = true),
    RATE_LIMITED(4008, shouldReconnect = false, shouldResume = false, clientError = true),
    SESSION_TIMED_OUT(4009, shouldReconnect = true, shouldResume = false, clientError = false),
    INVALID_SHARD(4010, shouldReconnect = false, shouldResume = false, clientError = true),
    SHARDING_REQUIRED(4011, shouldReconnect = false, shouldResume = false, clientError = true),
    INVALID_API_VERSION(4012, shouldReconnect = false, shouldResume = false, clientError = true),
    INVALID_INTENTS(4013, shouldReconnect = false, shouldResume = false, clientError = true),
    DISALLOWED_INTENTS(4014, shouldReconnect = false, shouldResume = false, clientError = true),
    NORMAL(1000, shouldReconnect = false, shouldResume = false, clientError = false),
    GOING_AWAY(1001, shouldReconnect = true, shouldResume = true, clientError = false),
    CLOSED_ABNORMALLY(1006, shouldReconnect = true, shouldResume = false, clientError = false),
    UNKNOWN(-1, shouldReconnect = false, shouldResume = false, clientError = true);

    companion object {
        fun ofCode(code: Short) = values().find { it.code == code } ?: UNKNOWN
    }
}