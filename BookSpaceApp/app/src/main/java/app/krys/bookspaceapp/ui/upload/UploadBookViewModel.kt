package app.krys.bookspaceapp.ui.upload

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.work.Operation
import app.krys.bookspaceapp._util.writeBitmapToFile
import app.krys.bookspaceapp.data.model.BookInfo
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

    private lateinit var frontPageBitmapUri: Uri

    private val _metaData = MutableLiveData<BookMetaData>()
    val metadata: LiveData<BookMetaData> = _metaData

    private val _selectedFolderOption = MutableLiveData<FolderInfo>()
    val selectedFolderOption: LiveData<FolderInfo> get() = _selectedFolderOption




    fun loadBookFile(bookUri: Uri) {
        initBookUri(bookUri)
        initializeMetaData()
        getPdfMetadata()
    }

    private fun initBookUri(bookUri: Uri) {
        this.bookUri = bookUri
    }

    private fun initializeMetaData() {
        bookUri?.let {
            pdf = ReadPdfMetadata(it, application)
        }
    }

    private fun getPdfMetadata() {
        viewModelScope.launch {
            try {
                _metaData.postValue(pdf!!.getPdfMetadata())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }




    fun saveBitmapToFile(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                frontPageBitmapUri = writeBitmapToFile(application, bitmap)
            } catch (e: Exception) {
                e.printStackTrace()

            }

        }
    }

    fun createFileUploadWorkRequests(metaData: BookMetaData, folderId: String): Operation {
        return firebaseRepository.createFileUploadWorkRequests(
            metaData,
            folderId,
            application,
            frontPageBitmapUri.toString(),
            bookUri.toString()
        )
    }

    fun resetState(){
        _metaData.postValue(BookMetaData())
    }

    fun setSelectedItem(folderInfo: FolderInfo){
        _selectedFolderOption.postValue(folderInfo)
    }


    override fun onCleared() {
        super.onCleared()
        firebaseRepository.removeFolderInfoEventListener()
        Log.d(TAG, "onCleared: called")
    }

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