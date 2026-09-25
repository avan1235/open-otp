package ml.dev.kotlin.openotp.util

import kotlin.time.Clock

fun currentEpochMilliseconds(): Long =
    Clock.System.now().toEpochMilliseconds()
