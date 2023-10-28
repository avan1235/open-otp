package ml.dev.kotlin.openotp.util

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*

internal actual fun createHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(OkHttp, block)
