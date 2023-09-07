package ml.dev.kotlin.openotp.util

interface Named {
    val presentableName: String
}

fun <A> A.unit() {}

inline fun <A> A.letTrue(action: (A) -> Unit = {}): Boolean = let(action).let { true }
inline fun <A> A.letFalse(action: (A) -> Unit = {}): Boolean = let(action).let { false }

inline fun <T> runCatchingOrNull(action: () -> T): T? = try {
    action()
} catch (_: Throwable) {
    null
}
