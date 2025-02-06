package com.app.coinsage

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform