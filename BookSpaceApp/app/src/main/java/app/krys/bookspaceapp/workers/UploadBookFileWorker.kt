package app.krys.bookspaceapp.workers

import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.krys.bookspaceapp._util.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class UploadBookFileWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(
    context,
    workerParams
) {
    val user = Firebase.auth.currentUser
    val storage = Firebase.storage.reference
    val database = Firebase.database.reference
    val notificationManager =
        ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)

    override suspend fun doWork(): Result {

        //get book file uri
        val bookFileUri = inputData.getString(BOOK_FILE_URI)
        //get book key
        val bookKey = inputData.getString(KEY_BOOK_ID)
        //get folderId
        val folderId = inputData.getString(KEY_FOLDER_ID)

        val bookName = inputData.getString(KEY_BOOK_NAME)

        val notificationId = inputData.getInt(KEY_UNIQUE_NOTIFICATION_ID, NOTIFICATION_ID)


        return try {

            //check that the uri input is not null

            //check if user is not not
            if (TextUtils.isEmpty(bookFileUri) ||
                TextUtils.isEmpty(bookKey) || TextUtils.isEmpty(folderId)
            ) {
                throw IllegalArgumentException("All inputs are not available")
            }
            user?.let {
                //upload to firebase Storage
                //get downloadUri
                //set download uri a output
                return uploadFileToFirebaseStorage(
                    folderId = folderId!!,
                    bookUri = Uri.parse(bookFileUri),
                    uid = user.uid,
                    bookKey = bookKey!!,
                    bookName,
                    notificationId

                )
            }
            return Result.failure()


        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }


    }

    private suspend fun uploadFileToFirebaseStorage(
        folderId: String,
        bookUri: Uri,
        uid: String,
        bookKey: String,
        bookName: String?,
        notificationId: Int
    ): Result {

        val fileRef = storage.child("folderFiles/$uid/$folderId/$bookKey.pdf")

        fileRef.putFile(bookUri).addOnProgressListener {
//            notificationManager?.createProgressiveNotification(
//                "Uploading $bookName",
//                ((100 * it.bytesTransferred) / it.totalByteCount).toInt(), applicationContext
//            )
            notificationManager?.createUniqueProgressiveNotification(
                "Uploading $bookName",
                ((100 * it.bytesTransferred) / it.totalByteCount).toInt(),
                applicationContext,
                notificationId
            )
        }.await()

        val uriMap: MutableMap<String, String> = mutableMapOf()

        fileRef.downloadUrl.await().let { fileUri ->
            uriMap[KEY_BOOK_FILE_DOWNLOAD_URI] = fileUri.toString()
        }
        val outputData = workDataOf(
            KEY_BOOK_FILE_DOWNLOAD_URI to uriMap[KEY_BOOK_FILE_DOWNLOAD_URI],
            KEY_COVER_IMAGE_DOWNLOAD_URI to inputData.getString(KEY_COVER_IMAGE_DOWNLOAD_URI),
            KEY_BOOK_ID to bookKey
        )
        return Result.success(outputData)


    }

}