package com.wutsi.stream.file

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.stream.Event
import com.wutsi.stream.EventHandler
import com.wutsi.stream.ObjectMapperBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class FileStreamTest {
    lateinit var handler: EventHandler
    lateinit var stream: FileEventStream
    val root = File(System.getProperty("user.home") + "/wutsi/file-stream")

    @BeforeEach
    fun setUp() {
        root.deleteRecursively()

        handler = mock()
        stream = FileEventStream(name = "test", root = root, handler = handler)
    }

    @Test
    fun close() {
    }

    @Test
    fun `enqueued event stored input INPUT directory`() {
        stream.enqueue("foo", mapOf("yo" to "man"))

        val files = stream.input.listFiles()
        assertEquals(1, files.size)
    }

    @Test
    fun `enqueued event handled`() {
        stream.enqueue("foo", mapOf("yo" to "man"))
        Thread.sleep(15000)

        val event = argumentCaptor<Event>()
        verify(handler).onEvent(event.capture())

        assertEquals(36, event.firstValue.id.length)
        assertNotNull(event.firstValue.timestamp)
        assertEquals("foo", event.firstValue.type)
        assertEquals("{\"yo\":\"man\"}", event.firstValue.payload)
    }

    @Test
    fun `published event stored into OUTPUT directory`() {
        stream.publish("foo", mapOf("yo" to "man"))

        val files = stream.output.listFiles()
        assertEquals(1, files.size)
        val json = Files.readString(files[0].toPath())
        val event = ObjectMapperBuilder().build().readValue(json, Event::class.java)

        assertEquals(36, event.id.length)
        assertNotNull(event.timestamp)
        assertEquals("foo", event.type)
        assertEquals("{\"yo\":\"man\"}", event.payload)
    }

    @Test
    fun `subscribe to stream and receive events from that stream`() {
        val source = FileEventStream(
            root = root,
            name = "source",
            handler = mock()
        )

        stream.subscribeTo("source")

        source.publish("test", "oups")
        Thread.sleep(15000)

        assertEquals(1, source.output.listFiles().size)
        assertEquals(1, stream.input.listFiles().size)
    }
}
