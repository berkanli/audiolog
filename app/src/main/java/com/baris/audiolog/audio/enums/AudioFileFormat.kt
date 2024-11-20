package com.baris.audiolog.audio.enums

import android.media.AudioFormat

enum class AudioFileFormat(val encoding: Int, val displayName: String, val extension: String) {
    DEFAULT(AudioFormat.ENCODING_DEFAULT, "Default", "raw"),
    PCM_16BIT(AudioFormat.ENCODING_PCM_16BIT, "PCM 16-bit", "pcm"),
    PCM_8BIT(AudioFormat.ENCODING_PCM_8BIT, "PCM 8-bit", "pcm"),
    PCM_FLOAT(AudioFormat.ENCODING_PCM_FLOAT, "PCM Float", "wav"),
    MP3(AudioFormat.ENCODING_MP3, "MP3", "mp3"),
    AAC_LC(AudioFormat.ENCODING_AAC_LC, "AAC LC", "aac"),
    AAC_HE_V1(AudioFormat.ENCODING_AAC_HE_V1, "AAC HE v1", "aac"),
    AAC_HE_V2(AudioFormat.ENCODING_AAC_HE_V2, "AAC HE v2", "aac"),
    AC3(AudioFormat.ENCODING_AC3, "AC3", "ac3"),
    E_AC3(AudioFormat.ENCODING_E_AC3, "Enhanced AC3", "eac3"),
    DTS(AudioFormat.ENCODING_DTS, "DTS", "dts"),
    DTS_HD(AudioFormat.ENCODING_DTS_HD, "DTS HD", "dtshd"),
    OPUS(AudioFormat.ENCODING_OPUS, "Opus", "opus"),
    PCM_24BIT_PACKED(AudioFormat.ENCODING_PCM_24BIT_PACKED, "PCM 24-bit Packed", "pcm"),
    PCM_32BIT(AudioFormat.ENCODING_PCM_32BIT, "PCM 32-bit", "pcm");

    companion object {
        fun fromEncoding(encoding: Int): AudioFileFormat {
            return entries.find { it.encoding == encoding } ?: DEFAULT
        }
    }
}
