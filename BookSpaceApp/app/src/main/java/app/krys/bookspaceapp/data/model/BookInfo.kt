package app.krys.bookspaceapp.data.model

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


data class BookInfo (
    var bookName: String? = null,
    var author: String? = null,
    var size: String? = null,
    var downloadUrl: String? = null,
    var dateAdded: Long? = null,
    var bookImageUrl: String? = null
        ){
    companion object {
        @BindingAdapter("bookImage")
        fun loadImage(view: ImageView, imageUrl: String?) {
            Glide.with(view.getContext())
                .load(imageUrl).apply(RequestOptions().circleCrop())
                .into(view)
        }
    }

}