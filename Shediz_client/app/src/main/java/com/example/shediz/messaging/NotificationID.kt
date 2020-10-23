package com.example.shediz.messaging

import java.util.concurrent.atomic.AtomicInteger

// notificationId is a unique int for each notification that you must define
object NotificationID
{
    private val c = AtomicInteger(0)

    val uniqueID: Int
        get() = c.incrementAndGet()
}