package com.example.shediz.messaging

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.shediz.utils.Constants.EXCHANGE_NAME
import com.example.shediz.utils.Constants.HOSTNAME
import com.example.shediz.utils.Constants.QUEUE_NAME
import com.example.shediz.utils.Constants.ROUTING_KEY
import com.rabbitmq.client.*


class ReceiverService : Service()
{
    companion object
    {
        private val TAG = "TAG_" + ReceiverService::class.simpleName
    }

    private var subscribeThread: Thread? = null

    private var factory: ConnectionFactory? = null

    private var consumer: ActualConsumer? = null

    private var consumerTag: String? = null

    private var channel: Channel? = null

    override fun onBind(intent: Intent): IBinder?
    {
        return null
    }

    override fun onCreate()
    {
        Log.i(TAG, "Service onCreate called")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
    {
        Log.i(TAG, "onStartCommand called")

        if (factory == null)
        {
            setupConnectionFactory()
            setupSubscription()
        }
        return START_STICKY
    }

    override fun onDestroy()
    {
        Log.i(TAG, "onDestroy called")

        if (channel != null && channel!!.isOpen)
            channel?.basicCancel(consumerTag)

        subscribeThread!!.interrupt()

        super.onDestroy()
    }

    private fun setupConnectionFactory()
    {
        factory = ConnectionFactory()
        factory!!.host = HOSTNAME
    }

    private fun setupSubscription()
    {
        if (subscribeThread != null)
        {
            subscribeThread!!.interrupt()
            subscribeThread!!.start()
            return
        }

        subscribeThread = Thread {
            try
            {

                val connection = factory!!.newConnection()
                channel = connection.createChannel()
                channel!!.exchangeDeclare(EXCHANGE_NAME, "direct")
                channel!!.queueDeclareNoWait(QUEUE_NAME, false, false, false, null)

                // bind queue to channel
                channel!!.queueBindNoWait(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY, null)

                consumer = ActualConsumer(this, channel)

                consumerTag = channel!!.basicConsume(QUEUE_NAME, true, consumer)

            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }

        subscribeThread!!.start()
    }
}
