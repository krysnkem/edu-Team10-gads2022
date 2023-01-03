package app.krys.bookspaceapp.ui.adapter.metadata

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.krys.bookspaceapp.data.model.BookMetaData
import app.krys.bookspaceapp.databinding.ViewBookLayoutBinding

class BookMetaDataAdapter() :
    ListAdapter<BookMetaData, BookMetaDataAdapter.BookMetaDataViewHolder>(MetaDataDiffUtil()) {

    inner class BookMetaDataViewHolder(val binding: ViewBookLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(metadata: BookMetaData) {
            binding.bookData = metadata
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookMetaDataViewHolder {
        return BookMetaDataViewHolder(
            ViewBookLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BookMetaDataViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}

class MetaDataDiffUtil() : DiffUtil.ItemCallback<BookMetaData>() {
    override fun areItemsTheSame(oldItem: BookMetaData, newItem: BookMetaData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: BookMetaData, newItem: BookMetaData): Boolean {
        return oldItem.fileName == newItem.fileName
    }
}