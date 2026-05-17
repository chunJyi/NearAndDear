package com.chun.nearanddear.ui.navigation

object Routes {
    object Auth {
        const val SPLASH = "splash"
        const val LOGIN = "login"
    }

    object Main {
        const val HOME = "home"
        const val FRIENDS = "friends"
        const val FRIEND_DETAIL = "friend_detail/{userId}"
        const val FRIEND_LOCATION = "friend_location/{userId}"
        const val SETTINGS = "settings"

        fun friendDetail(userId: String) = "friend_detail/$userId"
        fun friendLocation(userId: String) = "friend_location/$userId"
    }
}
