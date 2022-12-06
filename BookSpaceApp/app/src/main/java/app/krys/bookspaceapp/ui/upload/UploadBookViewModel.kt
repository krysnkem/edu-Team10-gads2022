package app.krys.bookspaceapp.ui.upload

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.krys.bookspaceapp.data.model.FolderInfo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class UploadBookViewModel() : ViewModel() {

    private val database = Firebase.database.reference
    private val user = Firebase.auth.currentUser

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
            Log.e(TAG, "onCancelled: ${error.message}")
        }

    }

    init {
        if (user != null) {
            database.child("foldersInfo/${user.uid}").addValueEventListener(
                folderValueEventListener
            )
        }
    }


    override fun onCleared() {
        super.onCleared()
        database.child("foldersInfo/${user?.uid}").removeEventListener(folderValueEventListener)
    }


    companion object {
        const val TAG = "UploadBookViewModel"
    }
}

class UploadBookViewModelFactory() : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UploadBookViewModel::class.java))
            return UploadBookViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}