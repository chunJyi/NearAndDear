package com.chun.nearanddear.logging

import android.content.pm.ApplicationInfo
import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Logs uncaught crashes to private app storage for development troubleshooting.
 */
object CrashFileLogger {

    private const val LOG_DIR = "crash_logs"
    private const val MAX_LOG_FILES = 20

    fun install(context: Context) {
        if (!isDebugBuild(context)) return

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        val appContext = context.applicationContext

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                writeCrashLog(appContext, thread, throwable)
            }
            previousHandler?.uncaughtException(thread, throwable)
                ?: throw throwable
        }
    }

    private fun writeCrashLog(context: Context, thread: Thread, throwable: Throwable) {
        val logDir = File(context.filesDir, LOG_DIR).apply { mkdirs() }
        trimOldLogs(logDir)
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toString()
        }

        val timestamp = utcNow("yyyy-MM-dd'T'HH-mm-ss.SSS'Z'")
        val filename = "crash_$timestamp.txt"
        val file = File(logDir, filename)

        val logBody = buildString {
            appendLine("timestamp_utc=${utcNow("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")}")
            appendLine("app_id=${context.packageName}")
            appendLine("version_name=${packageInfo.versionName ?: "n/a"}")
            appendLine("version_code=$versionCode")
            appendLine("device=${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("android_sdk=${Build.VERSION.SDK_INT}")
            appendLine("thread=${thread.name} (id=${thread.id})")
            appendLine("exception_type=${throwable::class.java.name}")
            appendLine("message=${throwable.message ?: "n/a"}")
            appendLine()
            appendLine("--- stacktrace ---")
            appendLine(stackTrace(throwable))
        }

        file.writeText(logBody)
    }

    private fun trimOldLogs(logDir: File) {
        val files = logDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("crash_") && it.name.endsWith(".txt") }
            ?.sortedBy { it.lastModified() }
            ?: return

        val filesToDelete = files.size - MAX_LOG_FILES
        if (filesToDelete <= 0) return

        files.take(filesToDelete).forEach { file ->
            runCatching { file.delete() }
        }
    }

    private fun stackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

    private fun utcNow(pattern: String): String {
        return SimpleDateFormat(pattern, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
    }

    private fun isDebugBuild(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}
