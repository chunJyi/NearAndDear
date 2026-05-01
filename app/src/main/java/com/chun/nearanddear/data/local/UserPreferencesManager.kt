package com.chun.nearanddear.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    fun saveUserInfo(userId: String, userName: String, userEmail: String) {
        sharedPreferences.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_EMAIL, userEmail)
            .apply()
    }

    fun getUserInfo(): UserInfo? {
        val userId = getUserId()
        return if (userId != null) {
            UserInfo(
                userId = userId,
                userName = sharedPreferences.getString(KEY_USER_NAME, "") ?: "",
                userEmail = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
            )
        } else {
            null
        }
    }

    fun clearUserData() {
        sharedPreferences.edit()
            .clear()
            .apply()
    }
}

data class UserInfo(
    val userId: String,
    val userName: String,
    val userEmail: String
)
