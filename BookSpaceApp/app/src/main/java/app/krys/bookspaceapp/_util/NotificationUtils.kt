package app.krys.bookspaceapp._util

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import app.krys.bookspaceapp.R


fun NotificationManager.createNotification(title: String, message: String, appContext: Context) {
    val builder = NotificationCompat.Builder(
        appContext,
        NOTIFICATION_CHANNEL_ID
    ).apply {
        setSmallIcon(R.drawable.book_space_logo)
        setContentTitle(title)
        setContentText(message)
        priority = NotificationCompat.PRIORITY_HIGH
        setVibrate(LongArray(0))
    }

    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.createUniqueNotification(
    title: String,
    message: String,
    appContext: Context,
    id: Int
) {
    val builder = NotificationCompat.Builder(
        appContext,
        NOTIFICATION_CHANNEL_ID
    ).apply {
        setSmallIcon(R.drawable.book_space_logo)
        setContentTitle(title)
        setContentText(message)
        priority = NotificationCompat.PRIORITY_HIGH
        setVibrate(LongArray(0))
    }

    notify(id, builder.build())
}


fun NotificationManager.cancelNotifications() = cancelAll()
fun NotificationManager.cancelNotificationWithId(id: Int) = cancel(id)

fun NotificationManager.createProgressiveNotification(
    title: String, progress: Int, appContext: Context
) {
    val builder = NotificationCompat.Builder(
        appContext,
        DOWNLOAD_NOTIFICATION_CHANNEL_ID
    ).apply {
        setSmallIcon(R.drawable.book_space_logo)
        setContentTitle(title.substring(0..5))
        setContentText("Uploading ${progress}%")
        setProgress(100, progress, false)
        priority = NotificationCompat.PRIORITY_DEFAULT

    }
    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.createUniqueProgressiveNotification(
    title: String, progress: Int, appContext: Context, id: Int
) {
    val builder = NotificationCompat.Builder(
        appContext,
        DOWNLOAD_NOTIFICATION_CHANNEL_ID
    ).apply {
        setSmallIcon(R.drawable.book_space_logo)
        setContentTitle(title.substring(0..5))
        setContentText("Uploading ${progress}%")
        setProgress(100, progress, false)
        priority = NotificationCompat.PRIORITY_DEFAULT

    }
    notify(id, builder.build())
}

fun NotificationManager.createIndeterminateNotification(text: String, appContext: Context) {
    val builder = NotificationCompat.Builder(
        appContext,
        DOWNLOAD_NOTIFICATION_CHANNEL_ID
    ).apply {
        setSmallIcon(R.drawable.book_space_logo)
        setContentText(text)
        setProgress(0, 0, true)
        setPriority(NotificationCompat.PRIORITY_DEFAULT)

    }
    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.createUniqueIndeterminateNotification(
    text: String,
    appContext: Context,
    id: Int
) {
    val builder = NotificationCompat.Builder(
        appContext,
        DOWNLOAD_NOTIFICATION_CHANNEL_ID
    ).apply {
        setSmallIcon(R.drawable.book_space_logo)
        setContentText(text)
        setProgress(0, 0, true)
        setPriority(NotificationCompat.PRIORITY_DEFAULT)

    }
    notify(id, builder.build())
}