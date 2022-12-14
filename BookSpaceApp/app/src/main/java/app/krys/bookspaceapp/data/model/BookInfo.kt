package app.krys.bookspaceapp.data.model

import android.os.Parcelable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize


@IgnoreExtraProperties
@Parcelize
data class BookInfo(
    var bookName: String? = null,
    var ownerId: String? = null,
    var folderId: String? = null,
    var author: String? = null,
    var size: Long? = null,
    var dateAdded: Long? = null,
    var downloadUrl: String? = null,
    var bookImageUrl: String? = null,
    var bookId: String? = null
) : Parcelable {
    companion object {
        @BindingAdapter("bookImage")
        @JvmStatic
        fun loadImage(view: ImageView, imageUrl: String?) {
            Glide.with(view.context)
                .load(imageUrl)
                .into(view)
        }
    }

    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "bookName" to bookName!!,
            "author" to author!!,
            "size" to size!!,
            "ownerId" to ownerId!!,
            "folderId" to folderId!!,
            "dateAdded" to dateAdded!!,
            "downloadUrl" to downloadUrl!!,
            "bookImageUrl" to bookImageUrl!!,
            "bookId" to bookId!!,
        )
    }

    fun isEmpty(): Boolean {
        return (
                bookName == null &&
                        author == null &&
                        size == null &&
                        ownerId == null &&
                        folderId == null &&
                        dateAdded == null &&
                        downloadUrl == null &&
                        bookImageUrl == null &&
                        bookId == null
                )
    }

}