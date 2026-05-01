package com.chun.nearanddear.domain.usecase.auth

import com.chun.nearanddear.domain.model.User
import com.chun.nearanddear.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): User? = repository.getCurrentUser()
}

