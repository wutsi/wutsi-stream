package com.wutsi.stream

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test

internal class EventSubscriptionTest {
    @Test
    fun `subscribe`() {
        val to = mock<EventStream>()

        EventSubscription("from", to)

        verify(to).subscribeTo("from")
    }
}
