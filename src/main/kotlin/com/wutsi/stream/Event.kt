package com.wutsi.stream

import java.time.OffsetDateTime

data class Event(
    val id: String = "",
    val type: String = "",
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val payload: String = ""
)
