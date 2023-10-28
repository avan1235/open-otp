package ml.dev.kotlin.openotp.util

import io.ktor.client.*

internal expect fun createHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient
