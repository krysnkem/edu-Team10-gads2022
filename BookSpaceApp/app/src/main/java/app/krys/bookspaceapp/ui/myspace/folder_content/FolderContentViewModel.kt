package app.krys.bookspaceapp.ui.myspace.folder_content

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.krys.bookspaceapp._util.deleteBookFromCache
import app.krys.bookspaceapp.data.model.BookInfo
import app.krys.bookspaceapp.repository.FirebaseRepository
import com.firebase.ui.database.FirebaseRecyclerOptions
import kotlinx.coroutines.launch

class FolderContentViewModel(val application: Application) : ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    fun getBookOptions(folderId: String): FirebaseRecyclerOptions<BookInfo> {
        return firebaseRepository.getBookOptions(folderId)
    }

    fun removeBook(bookInfo: BookInfo) {
        viewModelScope.launch {
            try {
                deleteBookFromCache(
                    application.applicationContext,
                    bookInfo.folderId!!,
                    "${bookInfo.bookId}.pdf"
                )
                firebaseRepository.removeBook(bookInfo).addOnCompleteListener {
                    if (!it.isSuccessful) {
                        it.exception?.let { e ->
                            throw e
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

}

@Suppress("UNCHECKED_CAST")
class FolderContentViewModelFactory(val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FolderContentViewModel::class.java))
            return FolderContentViewModel(application) as T
        throw IllegalArgumentException("Uknown ViewModel Class")
    }

}