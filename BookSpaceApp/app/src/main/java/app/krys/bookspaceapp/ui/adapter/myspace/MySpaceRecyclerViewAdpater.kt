package app.krys.bookspaceapp.ui.adapter.myspace

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import app.krys.bookspaceapp.data.model.FolderInfo
import app.krys.bookspaceapp.databinding.FolderListItemBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class MySpaceRecyclerViewAdpater(
    option: FirebaseRecyclerOptions<FolderInfo>,
    val folderOptionsClickListener: FolderOptionsClickListener,
    val folderClickListener: FolderClickListener
) :
    FirebaseRecyclerAdapter<FolderInfo, MySpaceRecyclerViewAdpater.FolderViewHolder>(option) {

    private val _dataChanged = MutableLiveData<Boolean>()
    val dataChanged: LiveData<Boolean> = _dataChanged

    private val _itemCount = MutableLiveData<Int>()
    val itemCount: LiveData<Int> get() = _itemCount

    private var count = 0


    inner class FolderViewHolder(val binding: FolderListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(info: FolderInfo) {
            binding.folder = info
            binding.optionListener = folderOptionsClickListener
            binding.folderClickListener = folderClickListener
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder(
            FolderListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int, model: FolderInfo) {
        ++count
        holder.bind(getItem(position))
        _itemCount.postValue(count)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        _dataChanged.postValue(true)
        _itemCount.postValue(count)
    }

    fun setCount() {
        _itemCount.postValue(count)
    }

    fun reduceCount() {
        --count
        _itemCount.postValue(count)
    }

}

class FolderOptionsClickListener(val onOptionsClickListener: (folderInfo: FolderInfo) -> Unit) {
    fun onClick(folderInfo: FolderInfo) = onOptionsClickListener(folderInfo)
}

class FolderClickListener(val onFolderClickListener: (folderInfo: FolderInfo) -> Unit) {
    fun onClick(folderInfo: FolderInfo) = onFolderClickListener(folderInfo)
}