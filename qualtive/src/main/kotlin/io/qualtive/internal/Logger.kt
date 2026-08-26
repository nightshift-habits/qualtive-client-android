package io.qualtive.internal

internal object Logger {
    private const val TAG = "Qualtive"

    fun hintNewVersion(detail: String) {
        val message =
            "Unknown Qualtive API field ($detail). Consider updating the Qualtive client library."
        try {
            android.util.Log.i(TAG, message)
        } catch (_: Throwable) {
            // android.util.Log may be unavailable in plain JVM unit tests.
            System.err.println("$TAG: $message")
        }
    }
}
