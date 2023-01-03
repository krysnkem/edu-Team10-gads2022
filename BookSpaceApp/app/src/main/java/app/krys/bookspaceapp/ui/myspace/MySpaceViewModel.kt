package app.krys.bookspaceapp.ui.myspace

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.krys.bookspaceapp._util.deleteFolderCachedFiles
import app.krys.bookspaceapp.data.model.FolderInfo
import app.krys.bookspaceapp.repository.FirebaseRepository
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

class MySpaceViewModel(val application: Application) : ViewModel() {


    private val firebaseRepository = FirebaseRepository()
    val options = firebaseRepository.getFolderInfosOption()



    fun createFolder(folder_name: String): Task<Void>? {
        return firebaseRepository.createFolder(folder_name)
    }
    @Suppress("UNCHECKED_CAST")
    fun removeFolder(folderInfo: FolderInfo): Task<Void>? {
        var task: Task<Void>? = null
        viewModelScope.launch {
            try {
                deleteFolderCachedFiles(application.applicationContext, folderId = folderInfo.folderId!!)
                task =  firebaseRepository.removeFolder(folderInfo)
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
        return task
    }

    companion object {
        const val TAG = "MySpaceViewModel"
    }

}

class MySpaceViewModelFactory(val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MySpaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MySpaceViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}