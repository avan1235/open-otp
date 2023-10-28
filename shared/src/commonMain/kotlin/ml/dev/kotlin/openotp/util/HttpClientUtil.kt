package ml.dev.kotlin.openotp.util

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createJsonHttpClient(): HttpClient = createHttpClient {
    install(ContentNegotiation) {
        json(HttpClientJson)
    }
}

private val HttpClientJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

suspend inline fun <reified T> HttpClient.safeRequest(
    block: HttpRequestBuilder.() -> Unit,
): Result<T> = try {
    val response = request { block() }
    Result.success(response.body())
} catch (e: Throwable) {
    Result.failure(e)
}

suspend inline fun HttpClient.safHttpRequest(
    block: HttpRequestBuilder.() -> Unit,
): Result<HttpResponse> = try {
    val response = request { block() }
    Result.success(response)
} catch (e: Throwable) {
    Result.failure(e)
}
