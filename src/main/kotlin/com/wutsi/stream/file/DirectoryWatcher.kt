package com.wutsi.stream.file

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.stream.Event
import com.wutsi.stream.EventHandler
import com.wutsi.stream.ObjectMapperBuilder
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Watch a directory. One a file is created in the directory
 * <ul>
 *     <li>The content of the file is deserialized to an @{link com.wutsi.stream.Event}</li>
 *     </li>Then the event is send to a {link com.wutsi.stream.EventHandler} to handle it</li>
 * </ul>
 */
class DirectoryWatcher(
    private val directory: File,
    private val handler: EventHandler,
    private val pollDelayMilliseconds: Long = 1000,
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
) : Runnable {
    private val key: WatchKey
    private val mapper: ObjectMapper

    init {
        mapper = ObjectMapperBuilder().build()

        val watcher = FileSystems.getDefault().newWatchService()
        if (!directory.exists()) {
            directory.mkdirs()
        }
        key = directory.toPath().register(watcher, StandardWatchEventKinds.ENTRY_CREATE)
        executor.scheduleAtFixedRate(this, 0, pollDelayMilliseconds, MILLISECONDS)
    }

    override fun run() {
        val events = key.pollEvents()
        events.forEach {
            val watch = it as WatchEvent<Path>
            val path = watch.context()
            val file = File(directory, path.toFile().name)
            try {
                System.out.println(">>> handing $file")
                val json = Files.readString(file.toPath())
                val event = mapper.readValue(json, Event::class.java)
                handler.onEvent(event)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        key.reset()
    }
}
