package com.route.maps

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform