package ml.dev.kotlin.openotp

import kotlinx.coroutines.runBlocking
import ml.dev.kotlin.openotp.shared.*
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ResourcesTest {

    private val defaultLocale: Locale = Locale.getDefault()

    @AfterTest
    fun restoreLocale() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `given english locale when formatting string resource then argument is substituted`() = runBlocking {
        Locale.setDefault(Locale.ENGLISH)
        assertEquals("Copied \"123456\" to clipboard", getString(Res.string.copied_code_to_clipboard, "123456"))
        assertEquals("Camera is not available\non simulator. Please try\nto run on a real iOS device.", getString(Res.string.camera_not_available))
        assertEquals("", getString(Res.string.default_group_name_dont_sort_name))
    }

    @Test
    fun `given english locale when formatting plural resource then quantity form is selected`() = runBlocking {
        Locale.setDefault(Locale.ENGLISH)
        assertEquals("1 second", getPluralString(Res.plurals.totp_period_second_unit_presentation, 1, 1))
        assertEquals("30 seconds", getPluralString(Res.plurals.totp_period_second_unit_presentation, 30, 30))
    }

    @Test
    fun `given polish locale when formatting plural resource then quantity form is selected`() = runBlocking {
        Locale.setDefault(Locale.forLanguageTag("pl"))
        assertEquals("15 sekund", getPluralString(Res.plurals.totp_period_second_unit_presentation, 15, 15))
        assertEquals("Podano nieprawidłowy sekret", getString(Res.string.invalid_field_name_provided_formatted, getString(Res.string.secret_field)))
    }
}
