package com.jery.starrailhelper.data

data class EventItem(
    val event: String,
    val image: String,
    val duration: Pair<String, String>,
    val type: String,
    val isExpired: Boolean = false
) { }
