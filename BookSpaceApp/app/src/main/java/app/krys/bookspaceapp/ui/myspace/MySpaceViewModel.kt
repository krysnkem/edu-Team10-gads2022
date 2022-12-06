package app.krys.bookspaceapp.ui.myspace

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.krys.bookspaceapp.data.model.FolderInfo
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MySpaceViewModel() : ViewModel() {

    private val database = Firebase.database.reference
    private val user = Firebase.auth.currentUser
    private var uid: String? = null
    private lateinit var folderQuery: Query
    lateinit var options: FirebaseRecyclerOptions<FolderInfo>
    init {
        if (user != null) {
            uid = user.uid
            folderQuery = database.child("foldersInfo/${uid}").orderByChild("dateModified")
            options = FirebaseRecyclerOptions.Builder<FolderInfo>()
                .setQuery(folderQuery, FolderInfo::class.java)
               .build()
        }


    }

    fun createFolder(folder_name: String): Task<Void>? {
        var task: Task<Void>? = null
        if (user != null) {
            uid = user.uid
            val key = database.child("foldersInfo/${user.uid}").push().key
            if (key == null) {
                Log.w(TAG, "Couldn't get push key for folder")
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
                        /*"folders/${user.uid}/$key" to false,*/
                        "foldersInfo/${user.uid}/$key" to folderInfo
                    ) as Map<String, Any>
                )
        }
        return task


    }

    @Suppress("UNCHECKED_CAST")
    fun removeFoler(folderInfo: FolderInfo): Task<Void>? {

        var task: Task<Void>? = null
        if (user!= null) {
            try {
                task = database.updateChildren(
                    hashMapOf(
                        /*"folders/${user.uid}/${folderInfo.folderId!!}" to null,*/
                        "foldersInfo/${user.uid}/${folderInfo.folderId}" to null
                    ) as Map<String, Any>
                )
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

class MySpaceViewModelFactory() : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MySpaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MySpaceViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}