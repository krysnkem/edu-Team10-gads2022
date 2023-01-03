package app.krys.bookspaceapp.workers

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.krys.bookspaceapp._util.*
import app.krys.bookspaceapp.data.model.BookInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostBookInfoWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(
    context,
    workerParams
) {
    val user = Firebase.auth.currentUser
    val database = Firebase.database.reference

    override suspend fun doWork(): Result {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        val bookName: String? = inputData.getString(KEY_BOOK_NAME)
        val ownerId: String? = inputData.getString(KEY_OWNER_ID)
        val folderId: String? = inputData.getString(KEY_FOLDER_ID)
        val author: String? = inputData.getString(KEY_AUTHOR)
        val size: Long = inputData.getLong(KEY_BOOK_SIZE, 0L)
        val dateAdded = inputData.getLong(KEY_DATE_ADDED, 0L)
        val downloadUrl = inputData.getString(KEY_BOOK_FILE_DOWNLOAD_URI)
        val bookImageUrl = inputData.getString(KEY_COVER_IMAGE_DOWNLOAD_URI)
        val bookId = inputData.getString(KEY_BOOK_ID)

        val notificationId = inputData.getInt(KEY_UNIQUE_NOTIFICATION_ID, NOTIFICATION_ID)


        if (user != null) {
            try {
                val bookInfo = BookInfo(
                    bookName = bookName,
                    ownerId = ownerId,
                    folderId = folderId,
                    author = author,
                    size = size,
                    dateAdded = dateAdded,
                    downloadUrl = downloadUrl,
                    bookImageUrl = bookImageUrl,
                    bookId = bookId
                )
                if (!bookInfo.isEmpty() && size != 0L && dateAdded != 0L) {
                    uploadBookDetails(bookInfo, user.uid).await()
//                    notificationManager.createNotification(
//                        BOOKSPACE_NOTIFICATION_TITLE,
//                        "Book Info Uploaded Successfully", applicationContext
//                    )
                    notificationManager.createUniqueNotification(
                        BOOKSPACE_NOTIFICATION_TITLE,
                        "Book Info Uploaded Successfully", applicationContext,
                        notificationId
                    )

                    return Result.success()

                } else {
                    notificationManager.createUniqueNotification(
                        BOOKSPACE_NOTIFICATION_TITLE,
                        "Failed to Upload Book Info, Retrying..", applicationContext,
                        notificationId
                    )
                    Result.retry()
                    return Result.failure()
                }


            } catch (e: Exception) {
                e.printStackTrace()
                notificationManager.createNotification(
                    BOOKSPACE_NOTIFICATION_TITLE,
                    "Failed to Upload Book Info", applicationContext
                )
                Result.retry()
                return Result.failure()
            }
        }
        notificationManager.createNotification(
            BOOKSPACE_NOTIFICATION_TITLE,
            "Failed to Upload Book Info", applicationContext
        )
        Result.retry()
        return Result.failure()


    }

    private suspend fun uploadBookDetails(bookInfo: BookInfo, uid: String): Task<Void> {

        return withContext(Dispatchers.IO) {
            database.updateChildren(
                mapOf<String, Any>(
                    "folders/$uid/${bookInfo.folderId}/${bookInfo.bookId}" to bookInfo.toMap(),
                    "foldersInfo/${uid}/${bookInfo.folderId}/numberOfFiles" to ServerValue.increment(
                        1
                    ),
                    "foldersInfo/${bookInfo.ownerId}/${bookInfo.folderId}/dateModified" to -1 * System.currentTimeMillis()

                )
            )
        }

    }
}