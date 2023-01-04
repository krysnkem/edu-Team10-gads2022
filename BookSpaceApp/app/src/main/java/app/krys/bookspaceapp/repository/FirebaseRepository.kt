package app.krys.bookspaceapp.repository

import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import app.krys.bookspaceapp._util.*
import app.krys.bookspaceapp.data.model.BookInfo
import app.krys.bookspaceapp.data.model.BookMetaData
import app.krys.bookspaceapp.data.model.FolderInfo
import app.krys.bookspaceapp.ui.myspace.MySpaceViewModel
import app.krys.bookspaceapp.ui.upload.UploadBookViewModel
import app.krys.bookspaceapp.workers.PostBookInfoWorker
import app.krys.bookspaceapp.workers.UploadBookFileWorker
import app.krys.bookspaceapp.workers.UploadCoverBitmapWorker
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val database = Firebase.database.reference
    private val storage = Firebase.storage.reference
    private val user = Firebase.auth.currentUser
    private var uid: String? = null

    private val _folderList = MutableLiveData<List<String>>()
    val folderNamesList: LiveData<List<String>> = _folderList

    private val _folderInfoList = MutableLiveData<List<FolderInfo>>()
    val folderInfoList: LiveData<List<FolderInfo>> = _folderInfoList


    private val folderValueEventListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val folderList = mutableListOf<String>()
            val folderInfoList = mutableListOf<FolderInfo>()
            for (fileInfoSnapShot in snapshot.children) {
                val fileInfo = fileInfoSnapShot.getValue<FolderInfo>()
                folderList.add(fileInfo?.folderName!!)
                folderInfoList.add(fileInfo)
            }
            _folderList.postValue(folderList)
            _folderInfoList.postValue(folderInfoList)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(UploadBookViewModel.TAG, "onCancelled: ${error.message}")
        }
    }

    init {
        if (user != null) {
            uid = user.uid
//            database.child("foldersInfo/${user.uid}").addValueEventListener(
//                folderValueEventListener
//            )
        }

    }

    fun attachFolderInfoEventListener() {
        database.child("foldersInfo/${user?.uid}").addValueEventListener(
            folderValueEventListener
        )
    }

    fun removeFolderInfoEventListener() {
        database.child("foldersInfo/${user?.uid}")
            .removeEventListener(folderValueEventListener)
    }


    fun createFolder(folder_name: String): Task<Void>? {
        var task: Task<Void>? = null
        if (user != null) {
            uid = user.uid
            val key = database.child("foldersInfo/${user.uid}").push().key
            if (key == null) {
                Log.w(MySpaceViewModel.TAG, "Couldn't get push key for folder")
                return task
            }
            val folderInfo = FolderInfo(
                folderName = folder_name,
                folderId = key.toString(),
                numberOfFiles = 0,
                dateCreated = -1 * System.currentTimeMillis(),
                dateModified = -1 * System.currentTimeMillis()
            ).toMap()
            task =
                database.updateChildren(
                    hashMapOf(
                        "foldersInfo/${user.uid}/$key" to folderInfo
                    ) as Map<String, Any>
                )

        }
        return task

    }

    @Suppress("UNCHECKED_CAST")
    suspend fun removeFolder(folderInfo: FolderInfo): Task<Void>? {
        if (user != null) {
            return try {
//                storage.child("folderFiles/$uid/${folderInfo.folderId}").listAll().await()
//                    .let { listResult ->
//                        for (item in listResult.items) {
//                            item.delete().await()
//                        }
//                    }
                val bookSnapShotList =
                    database.child("folders/${user.uid}/${folderInfo.folderId!!}").get().await()
                for (bookSnapShot in bookSnapShotList.children) {
                    val bookItem = bookSnapShot.getValue<BookInfo>()
                    if (bookItem != null) {
                        Log.d(TAG, "removeFolder: Removing ${bookItem.bookName}")
                        removeBook(bookItem)
                    }
                }
                database.updateChildren(
                    hashMapOf(
                        "folders/${user.uid}/${folderInfo.folderId}" to null,
                        "foldersInfo/${user.uid}/${folderInfo.folderId}" to null
                    ) as Map<String, Any>
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "removeFolder: ${e.message}")
                null
            }
        } else {
            return null
        }
    }

    fun createFileUploadWorkRequests(
        metaData: BookMetaData,
        folderId: String,
        context: Context,
        coverImage: String,
        notificationId: Int
    ): Operation {
        //build data
        val data = createDataItem(metaData, folderId, coverImage, notificationId)
        //create WorkRequests
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
        val uploadBookCoverWorkRequest = OneTimeWorkRequestBuilder<UploadCoverBitmapWorker>()
            .setConstraints(constraints.build())
            .setInputData(data)
            .build()
        val uploadBookFileWorkRequest = OneTimeWorkRequestBuilder<UploadBookFileWorker>()
            .setConstraints(constraints.build())
            .setInputData(data)
            .build()
        val postBookInfoWorkRequest = OneTimeWorkRequestBuilder<PostBookInfoWorker>()
            .setConstraints(constraints.build())
            .setInputData(data)
            .build()

        return WorkManager.getInstance(context).beginWith(uploadBookCoverWorkRequest)
            .then(uploadBookFileWorkRequest)
            .then(postBookInfoWorkRequest)
            .enqueue()

    }

    fun createMultipleFileUploadWorkRequests(
        metaDataList: List<BookMetaData>,
        folderId: String,
        context: Context,
        coverImages: Map<String, Uri>,
    ): List<Operation> {

        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        //create a list of operations
        val operationList = mutableListOf<Operation>()
        //build data

        metaDataList.forEach { metaData ->
            val coverImage = coverImages[metaData.fileName!!]?.toString()
            val notificationId = System.currentTimeMillis().toInt()

            notificationManager.createUniqueIndeterminateNotification(
                "Initializing",
                context,
                notificationId
            )
            if (coverImage != null) {
                operationList.add(
                    createFileUploadWorkRequests(
                        metaData,
                        folderId,
                        context,
                        coverImage,
                        notificationId
                    )
                )

            }
        }
        return operationList


    }


//    private fun createData(
//        metaData: BookMetaData,
//        folderId: String, coverImage: String, bookFileUri: String
//    ): Data {
//        val bookName: String = if (TextUtils.isEmpty(metaData.title)) {
//            metaData.fileName.toString()
//        } else {
//            metaData.title.toString()
//        }
//        val dataBuilder = Data.Builder().apply {
//            putString(KEY_BOOK_NAME, bookName)
//            putString(KEY_OWNER_ID, user!!.uid)
//            putString(KEY_FOLDER_ID, folderId)
//            putString(KEY_AUTHOR, metaData.author)
//            putLong(KEY_BOOK_SIZE, metaData.size!!)
//            putLong(KEY_DATE_ADDED, -1 * System.currentTimeMillis())
//            putString(COVER_IMAGE_URI, coverImage)
//            putString(BOOK_FILE_URI, bookFileUri)
//            putInt(KEY_UNIQUE_NOTIFICATION_ID, System.currentTimeMillis().toInt())
//        }
//        return dataBuilder.build()
//    }

    private fun createDataItem(
        metaData: BookMetaData,
        folderId: String, coverImage: String, notificationId: Int
    ): Data {
        val bookName: String = if (TextUtils.isEmpty(metaData.title)) {
            metaData.fileName.toString()
        } else {
            metaData.title.toString()
        }
        val dataBuilder = Data.Builder().apply {
            putString(KEY_BOOK_NAME, bookName)
            putString(KEY_OWNER_ID, user!!.uid)
            putString(KEY_FOLDER_ID, folderId)
            putString(KEY_AUTHOR, metaData.author)
            putLong(KEY_BOOK_SIZE, metaData.size!!)
            putLong(KEY_DATE_ADDED, -1 * System.currentTimeMillis())
            putString(COVER_IMAGE_URI, coverImage)
            putString(BOOK_FILE_URI, metaData.uri.toString())
            putInt(KEY_UNIQUE_NOTIFICATION_ID, notificationId)
        }
        return dataBuilder.build()
    }

    private fun getBookQuery(folderId: String): Query {
        return database.child("folders/${uid}/$folderId").orderByChild("dateAdded")
    }

    fun getBookOptions(folderId: String): FirebaseRecyclerOptions<BookInfo> {
        return FirebaseRecyclerOptions.Builder<BookInfo>()
            .setQuery(getBookQuery(folderId), BookInfo::class.java)
            .build()
    }

    fun getRecentBooksOptions(): FirebaseRecyclerOptions<BookInfo> {
        return FirebaseRecyclerOptions.Builder<BookInfo>()
            .setQuery(getRecentBooksQuery(), BookInfo::class.java)
            .build()
    }

    private fun getRecentBooksQuery(): Query {
        return database.child("recentBooks/${uid}").orderByChild("lastRead")
    }

    suspend fun removeBook(bookInfo: BookInfo): Task<Void> {
        database.updateChildren(
            mapOf(
                "folders/${bookInfo.ownerId}/${bookInfo.folderId}/${bookInfo.bookId}" to null,
                "recentBooks/${bookInfo.ownerId}/${bookInfo.bookId}" to null,
                "foldersInfo/${bookInfo.ownerId}/${bookInfo.folderId}/numberOfFiles" to ServerValue.increment(
                    -1
                ),
                "foldersInfo/${bookInfo.ownerId}/${bookInfo.folderId}/dateModified" to -1 * System.currentTimeMillis()
            )
        ).await()

        //TODO: change child path to "folderFiles/${bookInfo.ownerId}/${bookInfo.bookId}.pdf"
        storage.child("folderFiles/${bookInfo.ownerId}/${bookInfo.folderId}/${bookInfo.bookId}.pdf")
            .delete().await()
        //TODO: change child path to "folderFiles/${bookInfo.ownerId}/${bookInfo.bookId}.png"
        return storage.child("folderFiles/${bookInfo.ownerId}/${bookInfo.folderId}/${bookInfo.bookId}.png")
            .delete()
    }

    suspend fun downloadBookFile(url: String): ByteArray? {
        return Firebase.storage.getReferenceFromUrl(url).getBytes(PDF_MAX_BYTES).await()
    }

    suspend fun addBookToRecent(bookInfo: BookInfo) {
        database.updateChildren(
            mapOf(
                "folders/${bookInfo.ownerId}/${bookInfo.folderId}/${bookInfo.bookId}" to bookInfo.toMap(),
                "recentBooks/${bookInfo.ownerId}/${bookInfo.bookId}" to bookInfo.toMap()
            )
        )

    }


    fun getFolderInfosOption(): FirebaseRecyclerOptions<FolderInfo> {
        val folderQuery: Query =
            database.child("foldersInfo/${uid}").orderByChild("dateModified")
        val folderInfoOptions = FirebaseRecyclerOptions.Builder<FolderInfo>()
            .setQuery(folderQuery, FolderInfo::class.java)
            .build()

        return folderInfoOptions
    }


    companion object {
        const val TAG = "FirebaseRepository"
    }

}