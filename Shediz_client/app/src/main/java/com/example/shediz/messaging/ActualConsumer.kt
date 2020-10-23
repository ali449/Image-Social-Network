package com.example.shediz.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.shediz.R
import com.example.shediz.utils.Constants.CHANNEL_ID
import com.example.shediz.utils.SharedPref
import com.example.shediz.view.FollowRequestActivity
import com.google.gson.Gson
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import java.io.IOException

class ActualConsumer(private val context: Context, channel: Channel?) : DefaultConsumer(channel)
{
    companion object
    {
        private val TAG = "TAG_" + ActualConsumer::class.simpleName
    }

    @Throws(IOException::class)
    override fun handleDelivery(consumerTag: String?, envelope: Envelope?, properties: AMQP.BasicProperties?,
                                body: ByteArray?)
    {
        val message = String(body!!)

        Log.i(TAG, message)

        val followMessage = Gson().fromJson(message, FollowMessage::class.java)

        //Maybe application context not exist while running service, so we don't use singleton SharedPrefs
        val currentUserName = SharedPref(context).getUserName()

        if (followMessage.targetUserName == currentUserName)
            notifyToUser(followMessage)
    }

    private fun notifyToUser(followMessage: FollowMessage)
    {
        createNotificationChannel()

        val intent = Intent(context, FollowRequestActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK //or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("sourceUserName", followMessage.sourceUserName)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val contentText = context.resources.getString(R.string.push_text, followMessage.sourceUserName)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.resources.getString(R.string.request_follow))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setWhen(System.currentTimeMillis())
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NotificationID.uniqueID, builder.build())
        }

    }

    private fun createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {

            val name = context.resources.getString(R.string.request_follow)
            val descriptionText = context.resources.getString(R.string.push_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

}