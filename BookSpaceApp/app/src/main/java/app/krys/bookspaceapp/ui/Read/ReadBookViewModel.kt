package app.krys.bookspaceapp.ui.Read

import android.app.Application
import androidx.lifecycle.*
import app.krys.bookspaceapp._util.loadBytesArrayFromFile
import app.krys.bookspaceapp._util.pdfFileExists
import app.krys.bookspaceapp._util.writeBitmapToFile
import app.krys.bookspaceapp._util.writeByteArrayToFile
import app.krys.bookspaceapp.data.model.BookInfo
import app.krys.bookspaceapp.repository.FirebaseRepository
import kotlinx.coroutines.launch

class ReadBookViewModel(val application: Application) : ViewModel() {
    val firebaseRepository = FirebaseRepository()
    private val _pdfBytes = MutableLiveData<ByteArray>()
    val pdfBytes: LiveData<ByteArray> get() = _pdfBytes

    fun loadBytes(bookInfo: BookInfo) {
        val filename = "${bookInfo.bookId}.pdf"

        viewModelScope.launch {
            try {
                val byteArray = firebaseRepository.downloadBookFile(bookInfo.downloadUrl!!)
                _pdfBytes.postValue(byteArray!!)
                writeByteArrayToFile(application.applicationContext, byteArray, filename, bookInfo.folderId!!)
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    fun loadBook(bookInfo: BookInfo) {
        val filename = "${bookInfo.bookId}.pdf"
        viewModelScope.launch {
            if (pdfFileExists(application.applicationContext, filename, bookInfo.folderId!!)) {
                _pdfBytes.postValue(
                    loadBytesArrayFromFile(
                        application.applicationContext,
                        filename,
                        bookInfo.folderId!!
                    )!!
                )
            }else {
                loadBytes(bookInfo)
            }

        }

    }
}

@Suppress("UNCHECKED_CAST")
class ReadBookViewModelFactory(val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReadBookViewModel::class.java))
            return ReadBookViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}