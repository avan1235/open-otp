@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.browser.document
import ml.dev.kotlin.openotp.OpenOtpApp
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentContext
import ml.dev.kotlin.openotp.component.OpenOtpAppComponentImpl
import ml.dev.kotlin.openotp.initOpenOtpKoin
import ml.dev.kotlin.openotp.util.BiometryAuthenticator
import org.koin.compose.KoinContext
import org.koin.dsl.module
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLHeadElement
import org.w3c.dom.HTMLMetaElement
import org.w3c.dom.asList

fun webOpenOtpApp() {
    initOpenOtpKoin {
        modules(module {
            single { OpenOtpAppComponentContext() }
            single { BiometryAuthenticator() }
        })
    }
    val head = document.head ?: error("no <head>")
    val body = document.body ?: error("no <body>")
    val lifecycle = LifecycleRegistry()
    val component = OpenOtpAppComponentImpl(DefaultComponentContext(lifecycle))
    lifecycle.resume()
    ComposeViewport(
        configure = { isA11YEnabled = false }
    ) {
        KoinContext {
            OpenOtpApp(
                component = component,
                onBackgroundColorChange = { backgroundColor ->
                    head.replaceThemeColor(backgroundColor)
                    body.replaceBackgroundColor(backgroundColor)
                },
            )
        }
    }
}

private fun HTMLHeadElement.replaceThemeColor(color: Color) {
    children.asList()
        .filterIsInstance<HTMLMetaElement>()
        .filter { it.getAttribute("name") == "theme-color" }
        .forEach { it.remove() }

    fun addThemeColor(content: String, media: String? = null) {
        val node = document.createElement("meta").apply {
            setAttribute("name", "theme-color")
            setAttribute("content", content)
            if (media != null) setAttribute("media", media)
        }
        appendChild(node)
    }

    val hex = color.toHex()
    addThemeColor(hex)
    addThemeColor(hex, "(prefers-color-scheme: light)")
    addThemeColor(hex, "(prefers-color-scheme: dark)")
}

private fun HTMLElement.replaceBackgroundColor(color: Color) {
    setAttribute("style", "background-color: rgb(${color.red.in255()}, ${color.green.in255()}, ${color.blue.in255()});")
}

private fun Color.toHex(): String =
    "#" + listOf(red, green, blue).joinToString("") { it.in255().toString(16).padStart(2, '0') }

private fun Float.in255(): Int = (this * 255).toInt().coerceIn(0, 255)
