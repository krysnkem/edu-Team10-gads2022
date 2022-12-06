package app.krys.bookspaceapp.ui.adapter.myspace

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.krys.bookspaceapp.data.model.FolderInfo
import app.krys.bookspaceapp.databinding.FolderListItemBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class MySpaceRecyclerViewAdpater(option: FirebaseRecyclerOptions<FolderInfo>, val folderOptionsClickListener: FolderOptionsClickListener) :
    FirebaseRecyclerAdapter<FolderInfo, MySpaceRecyclerViewAdpater.FolderViewHolder>(option) {


    inner class FolderViewHolder(val binding: FolderListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(info: FolderInfo) {
            binding.folder = info
            binding.listener = folderOptionsClickListener
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
        holder.bind(getItem(position))
    }
}

class FolderOptionsClickListener(val onOptionsClickListener: (folderInfo: FolderInfo) -> Unit)
{
    fun onClick(folderInfo: FolderInfo) = onOptionsClickListener(folderInfo)
}