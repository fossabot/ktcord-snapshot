package net.lostillusion.ktcord.audio.encryption

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EncryptionMode {
    // audio-related
    @SerialName("xsalsa20_poly1305")
    NORMAL,

    @SerialName("xsalsa20_poly1305_suffix")
    SUFFIX,

    @SerialName("xsalsa20_poly1305_lite")
    LITE,

    @SerialName("xsalsa20_poly1305_lite_rtpsize")
    LITE_RTPSIZE,

    // video stuff... unused. though required to allow for serialization of ready
    @SerialName("aead_aes256_gcm_rtpsize")
    AEAD_AES_GCM_RTPSIZE,

    @SerialName("aead_aes256_gcm")
    AEAD_AES_GCM,
}

