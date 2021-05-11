package net.lostillusion.ktcord.audio.ws.util

enum class AudioCloseCode(val code: Short, val shouldReconnect: Boolean, val shouldResume: Boolean, val libraryError: Boolean) {
    UNKNOWN_OPCODE(4001, shouldReconnect = false, shouldResume = false, libraryError = true),
    FAILED_TO_DECODE_PAYLOAD(4002, shouldReconnect = false, shouldResume = false, libraryError = true),
    NOT_AUTHENTICATED(4003, shouldReconnect = false, shouldResume = false, libraryError = true),
    AUTHENTICATION_FAILED(4004, shouldReconnect = false, shouldResume = false, libraryError = true),
    ALREADY_AUTHENTICATED(4005, shouldReconnect = true, shouldResume = false, libraryError = true),
    SESSION_NO_LONGER_VALID(4006, shouldReconnect = true, shouldResume = true, libraryError = false),
    SESSION_TIMEOUT(4009, shouldReconnect = true, shouldResume = true, libraryError = false),
    SERVER_NOT_FOUND(4011, shouldReconnect = false, shouldResume = false, libraryError = true),
    UNKNOWN_PROTOCOL(4012, shouldReconnect = false, shouldResume = false, libraryError = true),
    DISCONNECTED(4014, shouldReconnect = false, shouldResume = false, libraryError = false),
    VOICE_SERVER_CRASHED(4015, shouldReconnect = true, shouldResume = true, libraryError = false),
    UNKNOWN_ENCRYPTION_ERROR(4016, shouldReconnect = false, shouldResume = false, libraryError = true),
    CLOSED_ABNORMALLY(1006, shouldReconnect = true, shouldResume = false, libraryError = false),
    GOING_AWAY(1001, shouldReconnect = true, shouldResume = true, libraryError = false),
    UNKNOWN(-1, shouldReconnect = false, shouldResume = false, libraryError = true);

    companion object {
        fun ofCode(code: Short) = values().find { it.code == code } ?: UNKNOWN
    }
}