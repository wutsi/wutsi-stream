package com.wutsi.stream.file

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.stream.Event
import com.wutsi.stream.EventHandler
import com.wutsi.stream.ObjectMapperBuilder
import com.wutsi.stream.Stream
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.Date
import java.util.UUID

/**
 * Rile implementation of {@link com.wutsi.stream.Stream}. Useful for file based
 */
class FileStream(
    private val handler: EventHandler,
    private val directory: File,
    private val deleteFilesOnExit: Boolean = true
) : Stream {
    private val mapper: ObjectMapper = ObjectMapperBuilder().build()

    override fun close() {
    }

    override fun enqueue(type: String, payload: Any) {
        handler.onEvent(
            event = createEvent(type, payload)
        )
    }

    override fun publish(type: String, payload: Any) {
        if (!directory.exists())
            directory.mkdirs()

        val event = createEvent(type, payload)
        val now = SimpleDateFormat("yyyyMMddHHmm").format(Date())
        val file = File(this.directory, "$now-${event.id}.json")
        Files.writeString(file.toPath(), mapper.writeValueAsString(event))

        if (deleteFilesOnExit)
            file.deleteOnExit()
    }

    private fun createEvent(type: String, payload: Any) = Event(
        id = UUID.randomUUID().toString(),
        type = type,
        timestamp = OffsetDateTime.now(),
        payload = mapper.writeValueAsString(payload)
    )
}
