package ml.dev.kotlin.openotp.util

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

internal actual fun createHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(Darwin, block)
