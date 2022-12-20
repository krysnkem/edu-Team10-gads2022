package app.krys.bookspaceapp.ui.account.settings


import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PhotoDataViewModel : ViewModel() {

    private val mutableImagePathSelected = MutableLiveData<Uri?>()
    val imagePathSelected: LiveData<Uri?> get() = mutableImagePathSelected

    fun getImagePath(path: Uri?) {
        mutableImagePathSelected.value = path
    }

    private val mutableImageBitmapSelected = MutableLiveData<Bitmap?>()
    val imageBitmapSelected: LiveData<Bitmap?> get() = mutableImageBitmapSelected

    fun getImageBitmap(bitMap: Bitmap?) {
        mutableImageBitmapSelected.value = bitMap
    }
}