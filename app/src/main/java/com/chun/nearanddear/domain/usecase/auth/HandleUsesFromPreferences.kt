package com.chun.nearanddear.domain.usecase.auth

import com.chun.nearanddear.data.local.UserInfo
import com.chun.nearanddear.data.local.UserPreferencesManager
import com.chun.nearanddear.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetUserIdUseCase @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) {
    operator fun invoke(): String? {
        return userPreferencesManager.getUserId()
    }
}

@Singleton
class GetUserInfoUseCase @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) {
    operator fun invoke(): UserInfo? {
        return userPreferencesManager.getUserInfo()
    }
}

@Singleton
class IsUserLoggedInUseCase @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) {
    operator fun invoke(): Boolean {
        return userPreferencesManager.getUserId() != null
    }
}

@Singleton
class SaveUserIdUseCase @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) {
    operator fun invoke(user: User) {
        userPreferencesManager.saveUserInfo(
            userId = user.userID,
            userName = user.name,
            userEmail = user.email
        )
    }
}
