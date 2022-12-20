package app.krys.bookspaceapp.ui.myspace.folder_content

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.krys.bookspaceapp.data.model.BookInfo
import app.krys.bookspaceapp.repository.FirebaseRepository
import com.firebase.ui.database.FirebaseRecyclerOptions

class FolderContentViewModel(application: Application): ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    fun getBookOptions(folderId: String): FirebaseRecyclerOptions<BookInfo>{
        return firebaseRepository.getBookOptions(folderId)
    }

}

@Suppress("UNCHECKED_CAST")
class FolderContentViewModelFactory(val application: Application): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FolderContentViewModel::class.java))
            return FolderContentViewModel(application) as T
        throw IllegalArgumentException("Uknown ViewModel Class")
    }

}