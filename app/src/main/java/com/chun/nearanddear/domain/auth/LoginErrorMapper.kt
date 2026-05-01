package com.chun.nearanddear.domain.auth

import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

/**
 * Maps low-level errors to user-facing [LoginOutcome.Failure] strings.
 */
object LoginErrorMapper {

    fun fromThrowable(throwable: Throwable): LoginOutcome.Failure {
        if (throwable is CancellationException) throw throwable

        val chain = causeChain(throwable)
        val representative = chain.firstOrNull { it is UnknownHostException }
            ?: chain.firstOrNull { it is ConnectException }
            ?: chain.firstOrNull { it is SocketTimeoutException || it is HttpRequestTimeoutException }
            ?: chain.firstOrNull { it is SSLHandshakeException || it is SSLException }
            ?: chain.firstOrNull { it is IOException }
            ?: throwable

        return when (representative) {
            is UnknownHostException -> LoginOutcome.Failure(
                title = "No internet connection",
                message = "We could not reach the server. Check your network and try again.",
                cause = throwable
            )
            is ConnectException -> LoginOutcome.Failure(
                title = "Connection failed",
                message = "Could not connect. Check your internet connection and try again.",
                cause = throwable
            )
            is SocketTimeoutException,
            is HttpRequestTimeoutException -> LoginOutcome.Failure(
                title = "Request timed out",
                message = "The server took too long to respond. Try again in a moment.",
                cause = throwable
            )
            is SSLHandshakeException,
            is SSLException -> LoginOutcome.Failure(
                title = "Secure connection failed",
                message = "We could not establish a secure connection. Check your date and time settings or try another network.",
                cause = throwable
            )
            is IOException -> LoginOutcome.Failure(
                title = "Network problem",
                message = representative.message?.takeIf { it.isNotBlank() }
                    ?: "Something went wrong with the network. Please try again.",
                cause = throwable
            )
            else -> LoginOutcome.Failure(
                title = "Something went wrong",
                message = representative.message?.takeIf { it.isNotBlank() }
                    ?: "Please try again.",
                cause = throwable
            )
        }
    }

    private fun causeChain(root: Throwable): Sequence<Throwable> = sequence {
        val seen = mutableSetOf<Throwable>()
        var current: Throwable? = root
        while (current != null && current !in seen) {
            seen.add(current)
            yield(current)
            current = current.cause?.takeIf { it != current }
        }
    }
}
