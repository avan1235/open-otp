package ml.dev.kotlin.openotp.util

import kotlinx.datetime.Clock

fun currentEpochMilliseconds(): Long =
    Clock.System.now().toEpochMilliseconds()
