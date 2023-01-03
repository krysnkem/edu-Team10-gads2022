package app.krys.bookspaceapp.ui.recent

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.krys.bookspaceapp.repository.FirebaseRepository

class RecentBooksViewModel(val application: Application): ViewModel() {
    val firebaseRepository = FirebaseRepository()
    val options = firebaseRepository.getRecentBooksOptions()
}

@Suppress("UNCHECKED_CAST")
class RecentBooksViewModelFactory(val application: Application): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecentBooksViewModel::class.java)){
            return  RecentBooksViewModel(application) as T
        }
        throw IllegalStateException("Unknown ViewModel")
    }
}