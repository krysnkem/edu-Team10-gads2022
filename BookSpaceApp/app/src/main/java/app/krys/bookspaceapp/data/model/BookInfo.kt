package app.krys.bookspaceapp.data.model

import android.os.Parcelable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import app.krys.bookspaceapp.R
import com.bumptech.glide.Glide
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
    var bookId: String? = null,
    var lastRead: Long? = 0,
    var currentPage: Long? = 0
) : Parcelable {
    companion object {
        @BindingAdapter("bookImage")
        @JvmStatic
        fun loadImage(view: ImageView, imageUrl: String?) {
            Glide.with(view.context)
                .load(imageUrl)
                .placeholder(ContextCompat.getDrawable(view.context, R.drawable.placeholder_book_img))
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
            "lastRead" to lastRead!!,
            "currentPage" to currentPage!!
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
                        bookId == null &&
                        lastRead == null &&
                        currentPage == null
                )
    }


}