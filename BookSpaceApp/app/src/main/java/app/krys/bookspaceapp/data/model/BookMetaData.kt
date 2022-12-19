package app.krys.bookspaceapp.data.model

import android.graphics.Bitmap
import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

data class BookMetaData(
    var author: String? = null,
    var title: String? = null,
    var size: Long? = null,
    var fileName: String? = null,
    var frontPage: Bitmap? = null
) {

    companion object {
        @BindingAdapter("bitmap")
        @JvmStatic
        fun loadImage(view: ImageView, bitmap: Bitmap?) {
            Glide.with(view.context).load(bitmap).into(view)
        }

        @JvmStatic
        @BindingAdapter("title", "fileName")
        fun showBookTile(view: TextView, bookTitle: String?, bookFileName: String?) {
            if (bookTitle == null && bookFileName == null) {
                view.text = "N/A"
            } else {
                if (TextUtils.isEmpty(bookTitle)) {
                    view.text = bookFileName
                } else {
                    view.text = bookTitle
                }
            }
        }

        @JvmStatic
        @BindingAdapter("author")
        fun showAuthor(view: TextView, author: String?) {
            if (TextUtils.isEmpty(author)) {
                view.text = "N/A"
            } else {
                view.text = author
            }
        }
    }

}

fun BookMetaData.isEmpty(): Boolean {
    return (
            author == null &&
                    title == null &&
                    size == null &&
                    fileName == null &&
                    frontPage == null
            )
}

fun BookMetaData.toBookInfo(): BookInfo {
    var title: String? = null
    var size: Long? = null
    var author: String? = null

    title = if (TextUtils.isEmpty(this.title))
        this.fileName
    else
        this.title

    size = this.size

    author = if (TextUtils.isEmpty(this.author))
        "N/A"
    else
        this.author

    return BookInfo(
        bookName = title,
        author = author,
        size = size
    )

}