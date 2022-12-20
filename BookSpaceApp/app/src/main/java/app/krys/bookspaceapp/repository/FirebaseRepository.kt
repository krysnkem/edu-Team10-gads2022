package app.krys.bookspaceapp.repository

import android.content.Context
import android.text.TextUtils
import android.util.Log
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


    private lateinit var folderQuery: Query
    lateinit var folderInfoOptions: FirebaseRecyclerOptions<FolderInfo>



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
            folderQuery = database.child("foldersInfo/${uid}").orderByChild("dateModified")
            folderInfoOptions = FirebaseRecyclerOptions.Builder<FolderInfo>()
                .setQuery(folderQuery, FolderInfo::class.java)
                .build()

            database.child("foldersInfo/${user.uid}").addValueEventListener(
                folderValueEventListener
            )
        }


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
                dateCreated = System.currentTimeMillis(),
                dateModified = System.currentTimeMillis()
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
                storage.child("folderFiles/$uid/${folderInfo.folderId}").listAll().await()
                    .let { listResult ->
                        for (item in listResult.items) {
                            item.delete().await()
                        }
                    }
                database.updateChildren(
                    hashMapOf(
                        "folders/${user.uid}/${folderInfo.folderId!!}" to null,
                        "foldersInfo/${user.uid}/${folderInfo.folderId}" to null
                    ) as Map<String, Any>
                )
            } catch (e: Exception) {
                e.printStackTrace()
               Log.e(TAG, "removeFolder: ${e.message}", )
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
        bookFileUri: String
    ): Operation {
        //build data
        val data = createData(metaData, folderId, coverImage, bookFileUri)
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
        val PostBookInfoWorkRequest = OneTimeWorkRequestBuilder<PostBookInfoWorker>()
            .setConstraints(constraints.build())
            .setInputData(data)
            .build()

        return WorkManager.getInstance(context).beginWith(uploadBookCoverWorkRequest)
            .then(uploadBookFileWorkRequest)
            .then(PostBookInfoWorkRequest)
            .enqueue()

    }

    private fun createData(
        metaData: BookMetaData,
        folderId: String, coverImage: String, bookFileUri: String
    ): Data {
        var bookName: String = metaData.title ?: metaData.fileName ?: "N/A"
        bookName = if (TextUtils.isEmpty(metaData.title)) {
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
            putLong(KEY_DATE_ADDED, System.currentTimeMillis())
            putString(COVER_IMAGE_URI, coverImage)
            putString(BOOK_FILE_URI, bookFileUri)
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

    suspend fun downloadBookFile(url: String): ByteArray? {

        return Firebase.storage.getReferenceFromUrl(url).getBytes(PDF_MAX_BYTES).await()

    }



    companion object {
        const val TAG = "FirebaseRepository"
    }

}