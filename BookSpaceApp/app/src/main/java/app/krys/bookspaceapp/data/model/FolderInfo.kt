package app.krys.bookspaceapp.data.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize


@IgnoreExtraProperties
@Parcelize
data class FolderInfo(
    val folderName: String? = "",
    val folderId: String? = "",
    val numberOfFiles: Long? = 0,
    val dateCreated: Long? = 0,
    val dateModified: Long? = 0
):Parcelable {
    @Exclude
    fun toMap(): HashMap<String, Any>{
        return hashMapOf<String, Any>(
            "folderName" to folderName!!,
            "folderId" to folderId!!,
            "numberOfFiles" to numberOfFiles!!,
            "dateCreated" to dateCreated!!,
            "dateModified" to  dateModified!!,

        )
    }
}
