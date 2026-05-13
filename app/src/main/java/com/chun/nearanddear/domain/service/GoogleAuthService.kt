package com.chun.nearanddear.domain.service

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.NoCredentialException
import com.chun.nearanddear.domain.auth.LoginErrorMapper
import com.chun.nearanddear.domain.auth.LoginOutcome
import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.domain.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GoogleAuthService"

@Singleton
class GoogleAuthService @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    /**
     * Sign in with Google account and Supabase using the ID token.
     *
     * @return [LoginOutcome.Success] with user, [LoginOutcome.Cancelled] if the user backs out,
     * or [LoginOutcome.Failure] with a specific reason.
     */
    suspend fun getGoogleIdToken(context: Context): LoginOutcome = withContext(Dispatchers.IO) {
        val credentialManager = CredentialManager.create(context)

        val rawNonce = UUID.randomUUID().toString()
        val digest = MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray())
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("520307766285-7srfb5at6fj0jdn5u8koc8sp6p7tuvgt.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        try {
            val result = credentialManager.getCredential(request = request, context = context)
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            if (googleIdToken.isEmpty()) {
                Log.e(TAG, "Google ID token is null or empty")
                return@withContext LoginOutcome.Failure(
                    title = "Sign-in incomplete",
                    message = "Google did not return a valid sign-in token. Please try again.",
                    recoverable = true
                )
            }

            supabaseClient.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
                nonce = rawNonce
            }

            val session = supabaseClient.auth.currentSessionOrNull()
            val user = session?.user ?: return@withContext LoginOutcome.Failure(
                title = "Sign-in failed",
                message = "We could not start your session. Please try again.",
                recoverable = true
            )
            val userId = user.id
            val email = user.email ?: "No Email"
            val phone = "No Phone"
            val name = user.userMetadata?.get("name")?.toString()?.trim('"') ?: "No Name"
            val avatarUrl = user.userMetadata?.get("avatar_url")?.toString()?.trim('"') ?: ""
            val updatedAt = Instant.now().toString()

            val userToInsert = User(
                userID = userId,
                name = name.toString(),
                email = email,
                phone = phone,
                role = UserRole.USER,
                avatarUrl = avatarUrl.toString(),
                updatedAt = updatedAt,
                createdAt = updatedAt
            )
            LoginOutcome.Success(userToInsert)
        } catch (e: CancellationException) {
            throw e
        } catch (_: GetCredentialCancellationException) {
            Log.w(TAG, "User cancelled the sign-in")
            LoginOutcome.Cancelled
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No credential: ${e.message}", e)
            LoginOutcome.Failure(
                title = "No Google account",
                message = "No usable Google sign-in was found. Add a Google account on this device or try again.",
                recoverable = true,
                cause = e
            )
        } catch (e: GetCredentialInterruptedException) {
            Log.w(TAG, "Sign-in interrupted: ${e.message}", e)
            LoginOutcome.Failure(
                title = "Sign-in interrupted",
                message = "The sign-in flow was interrupted. Please try again.",
                recoverable = true,
                cause = e
            )
        } catch (e: GetCredentialProviderConfigurationException) {
            Log.e(TAG, "Credential provider configuration: ${e.message}", e)
            LoginOutcome.Failure(
                title = "Sign-in not available",
                message = "Google sign-in is not configured correctly on this device.",
                recoverable = false,
                cause = e
            )
        } catch (e: GetCredentialUnknownException) {
            Log.e(TAG, "Unknown credential error: ${e.message}", e)
            LoginOutcome.Failure(
                title = "Sign-in failed",
                message = "Something went wrong during Google sign-in. Please try again.",
                recoverable = true,
                cause = e
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Token parsing failed: ${e.message}", e)
            LoginOutcome.Failure(
                title = "Invalid sign-in response",
                message = "We could not read the sign-in data from Google. Please try again.",
                recoverable = true,
                cause = e
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in failed: ${e.message}", e)
            LoginErrorMapper.fromThrowable(e)
        }
    }
}
