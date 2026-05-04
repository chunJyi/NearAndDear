package com.chun.nearanddear.data.session

import com.chun.nearanddear.domain.model.Location
import com.chun.nearanddear.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionDataStore @Inject constructor() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val userOrNull: User?
        get() = _currentUser.value

    fun setUser(user: User) {
        _currentUser.value = user
    }

    fun clearUser() {
        _currentUser.value = null
    }


    private val _location = MutableStateFlow<Location?>(null)
    val locationModel: StateFlow<Location?> = _location.asStateFlow()

    val locationOrNull: Location?
        get() = _location.value

    fun setLocation(location: Location) {
        _location.value = location
    }

    fun clear() {
        _location.value = null
    }
}
