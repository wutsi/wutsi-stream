package com.wutsi.stream.file

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.stream.Event
import com.wutsi.stream.EventHandler
import com.wutsi.stream.ObjectMapperBuilder
import com.wutsi.stream.Stream
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class FileStreamTest {
    lateinit var handler: EventHandler
    lateinit var stream: Stream
    val directory = File(System.getProperty("user.home"), "test_file_stream")

    @BeforeEach
    fun setUp() {
        directory.deleteRecursively()

        handler = mock()
        stream = FileStream(handler, directory)
    }

    @Test
    fun close() {
    }

    @Test
    fun enqueue() {
        stream.enqueue("foo", mapOf("yo" to "man"))

        val event = argumentCaptor<Event>()
        verify(handler).onEvent(event.capture())

        assertEquals(36, event.firstValue.id.length)
        assertNotNull(event.firstValue.timestamp)
        assertEquals("foo", event.firstValue.type)
        assertEquals("{\"yo\":\"man\"}", event.firstValue.payload)
    }

    @Test
    fun publish() {
        stream.publish("foo", mapOf("yo" to "man"))

        val files = directory.listFiles()

        assertEquals(1, files.size)
        val json = Files.readString(files[0].toPath())
        val event = ObjectMapperBuilder().build().readValue(json, Event::class.java)

        assertEquals(36, event.id.length)
        assertNotNull(event.timestamp)
        assertEquals("foo", event.type)
        assertEquals("{\"yo\":\"man\"}", event.payload)
    }
}
