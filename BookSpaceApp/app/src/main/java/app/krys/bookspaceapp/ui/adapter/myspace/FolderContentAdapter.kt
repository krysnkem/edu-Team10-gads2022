package app.krys.bookspaceapp.ui.adapter.myspace

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import app.krys.bookspaceapp.data.model.BookInfo
import app.krys.bookspaceapp.databinding.BookItemLayoutBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class FolderContentAdapter(options: FirebaseRecyclerOptions<BookInfo>,
val onSelectBookOptionListener: OnSelectBookOptionListener) :
    FirebaseRecyclerAdapter<BookInfo, FolderContentAdapter.BookViewHolder>(options) {


    private val _dataChanged = MutableLiveData<Boolean>()
    val dataChanged: LiveData<Boolean> = _dataChanged

    inner class BookViewHolder(val binding: BookItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bookInfo: BookInfo) {
            binding.book = bookInfo
            binding.optionListener = onSelectBookOptionListener
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder(
            BookItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int, model: BookInfo) {
        holder.bind(getItem(position))
        Log.d(TAG, "onBindViewHolder: ${getItem(position)}")
    }

    override fun onDataChanged() {
        super.onDataChanged()
        _dataChanged.postValue(true)
    }
    companion object   {
        const val TAG = "FolderContentAdapter"
    }
}

class OnSelectBookOptionListener(val onOptionsClickListener: (bookInfo: BookInfo)-> Unit){
    fun onSelect(bookInfo: BookInfo) = onOptionsClickListener(bookInfo)
}