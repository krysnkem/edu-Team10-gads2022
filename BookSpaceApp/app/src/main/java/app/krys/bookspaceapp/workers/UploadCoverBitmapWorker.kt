package app.krys.bookspaceapp.workers

import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
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


class UploadCoverBitmapWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(
    context, workerParams
) {

    val user = Firebase.auth.currentUser
    val storage = Firebase.storage.reference
    val database = Firebase.database.reference

    override suspend fun doWork(): Result {

        val coverImageUri = inputData.getString(COVER_IMAGE_URI)
        val folderId = inputData.getString(KEY_FOLDER_ID)
        val bookName = inputData.getString(KEY_BOOK_NAME)
        val notificationId = inputData.getInt(KEY_UNIQUE_NOTIFICATION_ID, NOTIFICATION_ID)

        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
        return try {
//            notificationManager.createIndeterminateNotification(
//                "Preparing $bookName",
//                applicationContext
//            )
            notificationManager.createUniqueIndeterminateNotification(
                "Preparing $bookName",
                applicationContext,
                notificationId
            )

            if (TextUtils.isEmpty(coverImageUri) && TextUtils.isEmpty(folderId)) {
                Log.e(TAG, "Invalid input uri")
                throw IllegalArgumentException("Invalid cover image uri and folderId")
            }
            if (user == null) {
                Log.e(TAG, "User is null")
                throw IllegalArgumentException("User is null")
            }

            val bookKey = database.child("folders/${user.uid}/$folderId").push().key.toString()

            uploadCoverImageToFirebaseStorage(
                folderId = folderId!!, coverImage =
                Uri.parse(coverImageUri), uid = user.uid,
                bookKey = bookKey
            )


        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }


    }

    private suspend fun uploadCoverImageToFirebaseStorage(
        folderId: String,
        coverImage: Uri,
        uid: String, bookKey: String
    ): Result {
        //TODO: change path to "folderFiles/$uid/$bookKey.png"

        val imageRef = storage.child("folderFiles/$uid/$folderId/$bookKey.png")


        try {
            imageRef.putFile(coverImage).await()

            val uriMap: MutableMap<String, String> = mutableMapOf()
            imageRef.downloadUrl.await().let { imageUri ->
                uriMap.put(
                    KEY_COVER_IMAGE_DOWNLOAD_URI, imageUri.toString()
                )

            }
            val outputData = workDataOf(
                KEY_COVER_IMAGE_DOWNLOAD_URI to uriMap[KEY_COVER_IMAGE_DOWNLOAD_URI],
                KEY_BOOK_ID to bookKey
            )

            return Result.success(outputData)


        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }


    }

    companion object {
        const val TAG: String = "UploadCoverBitmapWorker"
    }
}