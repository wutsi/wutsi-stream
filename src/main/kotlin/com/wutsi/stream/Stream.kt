package com.wutsi.stream

interface Stream {
    fun close()
    fun enqueue(type: String, payload: Any)
    fun publish(type: String, payload: Any)
}
