package app.krys.bookspaceapp.ui.upload

import android.app.Application
import android.app.NotificationManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.work.Operation
import app.krys.bookspaceapp._util.NOTIFICATION_ID
import app.krys.bookspaceapp._util.createIndeterminateNotification
import app.krys.bookspaceapp._util.writeBitmapToFile
import app.krys.bookspaceapp.data.model.BookMetaData
import app.krys.bookspaceapp.data.model.FolderInfo
import app.krys.bookspaceapp.data.pdf.ReadPdfMetadata
import app.krys.bookspaceapp.repository.FirebaseRepository
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch
import java.io.IOException

class UploadBookViewModel(private val application: Application) : ViewModel() {


    private val firebaseRepository = FirebaseRepository()

    val folderNamesList: LiveData<List<String>> = firebaseRepository.folderNamesList
    val folderInfoList: LiveData<List<FolderInfo>> = firebaseRepository.folderInfoList

    private var bookUri: Uri? = null
    private var pdf: ReadPdfMetadata? = null

    private var bookUriList = mutableListOf<Uri>()

//    private lateinit var frontPageBitmapUri: Uri

    private val _frontPagesLiveData = MutableLiveData<Map<String, Uri>>()
    val frontPagesLiveData: LiveData<Map<String, Uri>> = _frontPagesLiveData

//    private val _metaData = MutableLiveData<BookMetaData>()
//    val metadata: LiveData<BookMetaData> = _metaData

    private val _metaDataList = MutableLiveData<List<BookMetaData>>()
    val metadataList: LiveData<List<BookMetaData>> = _metaDataList

    private val _selectedFolderOption = MutableLiveData<FolderInfo>()
    val selectedFolderOption: LiveData<FolderInfo> get() = _selectedFolderOption


//    fun loadBookFile(bookUri: Uri) {
//        initBookUri(bookUri)
//        initializeMetaData()
//        getPdfMetadata()
//    }

    fun loadAllBookFiles(bookuriList: List<Uri>) {
        this.bookUriList.clear()
        this.bookUriList.addAll(bookuriList)
        initializeMetaData()
        getPdfMetadataList()

    }

    private fun initBookUri(bookUri: Uri) {
        this.bookUri = bookUri
    }

    private fun initializeMetaData() {
        pdf = ReadPdfMetadata(application)
    }

//    private fun getPdfMetadata() {
//        viewModelScope.launch {
//            try {
//                _metaData.postValue(bookUri?.let { pdf!!.getPdfMetadata(it) })
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }

    private fun getPdfMetadataList() {
        viewModelScope.launch {
            try {
                _metaDataList.postValue(pdf!!.getMetaDataList(bookUriList))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun attachFolderInfoEventListener() {
        firebaseRepository.attachFolderInfoEventListener()
    }

    fun detachFolderInfoEventListener() {
        firebaseRepository.removeFolderInfoEventListener()
    }


//    fun saveBitmapToFile(bitmap: Bitmap) {
//        viewModelScope.launch {
//            try {
//                frontPageBitmapUri = writeBitmapToFile(application, bitmap)
//            } catch (e: Exception) {
//                e.printStackTrace()
//
//            }
//
//        }
//    }
    fun saveAllBookCoverBitmapToFile(metaDataList: List<BookMetaData>){
        viewModelScope.launch {
            val frontPageMap = mutableMapOf<String, Uri>()
            try {
                metaDataList.forEach { metaData ->
                    frontPageMap.put(metaData.fileName!!, writeBitmapToFile(application, metaData.frontPage!!))
                }
                _frontPagesLiveData.postValue(frontPageMap)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }


    }

//    fun createFileUploadWorkRequests(metaData: BookMetaData, folderId: String): Operation {
//        val notificationManager = ContextCompat.getSystemService(
//            application.applicationContext,
//            NotificationManager::class.java
//        ) as NotificationManager
//        notificationManager.createIndeterminateNotification(
//            "Initializing",
//            application.applicationContext
//        )
//        return firebaseRepository.createFileUploadWorkRequests(
//            metaData,
//            folderId,
//            application,
//            frontPageBitmapUri.toString(),
//            NOTIFICATION_ID
//        )
//    }

    fun createMutipleFileUploadWorkRequests(
        metaDataList: List<BookMetaData>,
        folderId: String,
        frontPageBitmapsUris: Map<String, Uri>
    ): List<Operation> {
        return firebaseRepository.createMultipleFileUploadWorkRequests(
            metaDataList,
            folderId,
            application,
            frontPageBitmapsUris
        )
    }

    fun resetState() {
//        _metaData.postValue(BookMetaData())
        _metaDataList.postValue(emptyList())
        bookUriList.clear()
    }

    fun setSelectedItem(folderInfo: FolderInfo) {
        _selectedFolderOption.postValue(folderInfo)
    }


//    override fun onCleared() {
//        super.onCleared()
//        firebaseRepository.removeFolderInfoEventListener()
//        Log.d(TAG, "onCleared: called")
//    }
//

    fun createFolder(folder_name: String): Task<Void>? {
        return firebaseRepository.createFolder(folder_name)
    }


    companion object {
        const val TAG = "UploadBookViewModel"
    }
}

class UploadBookViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UploadBookViewModel::class.java))
            return UploadBookViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}