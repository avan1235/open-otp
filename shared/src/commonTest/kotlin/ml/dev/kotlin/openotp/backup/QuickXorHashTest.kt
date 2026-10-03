package ml.dev.kotlin.openotp.backup

import kotlin.test.Test
import kotlin.test.assertEquals

class QuickXorHashTest {

    @Test
    fun testQuickXorHash() {
        val dataHashes = listOf(
            ByteArray(0) to "AAAAAAAAAAAAAAAAAAAAAAAAAAA=",
            "a".encodeToByteArray() to "YQAAAAAAAAAAAAAAAQAAAAAAAAA=",
            "Hello, World!".encodeToByteArray() to "SCgDG9jwBhaA4ApvnQMbyBACAAA=",
            "The quick brown fox jumps over the lazy dog".encodeToByteArray() to "bMSlbysmxJL6S75XwfMcQZOpcr4=",
            ByteArray(1024) { it.toByte() } to "h7xr2dbCayZCQYR9KKhlwDuT4UI=",
        )
        for ((data, expectedHash) in dataHashes) {
            assertEquals(expectedHash, data.quickXorHashBase64())
        }
    }
}
