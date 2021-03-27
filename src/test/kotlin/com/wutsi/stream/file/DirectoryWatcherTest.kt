package com.wutsi.stream.file

import com.wutsi.stream.Event
import com.wutsi.stream.EventHandler
import com.wutsi.stream.ObjectMapperBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class DirectoryWatcherTest {
    lateinit var directory: File
    lateinit var handler: EventHandler
    var event: Event? = null

    @BeforeEach
    fun setUp() {
        directory = File(System.getProperty("user.home") + "/wutsi/directory-watcher")
        directory.deleteRecursively()
        directory.mkdirs()

        event = null
        handler = object : EventHandler {
            override fun onEvent(evt: Event) {
                System.out.println("onEvent($evt)")
                event = evt
            }
        }
    }

    @Test
    fun `consume new file`() {
        val watcher = DirectoryWatcher(directory, handler)

        // Write file
        val evt = createEvent()
        val json = ObjectMapperBuilder().build().writeValueAsString(evt)
        val file = File(directory, "test.json")
        Files.writeString(file.toPath(), json)
        System.out.println("$file stored")

        // Wait
        Thread.sleep(15000)

        // Validate
        assertNotNull(this.event)
        assertEquals(evt.id, this.event?.id)
        assertEquals(evt.type, this.event?.type)
        assertEquals(evt.timestamp.toInstant().toEpochMilli(), this.event?.timestamp?.toInstant()?.toEpochMilli())
        assertEquals(evt.payload, this.event?.payload)
    }

    private fun createEvent() = Event(
        id = UUID.randomUUID().toString(),
        type = "test",
        timestamp = OffsetDateTime.now(),
        payload = "foo"
    )
}
