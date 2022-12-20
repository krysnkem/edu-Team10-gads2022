package app.krys.bookspaceapp.ui.account.settings

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import java.io.ByteArrayOutputStream

class ImageCompressor(private val context: Context) {

    private val TAG = this::class.simpleName

    private var mBitmap: Bitmap? = null




    fun doBackgroundWorkAsync(bm: Bitmap?, path: Uri?): ByteArray? {
        mBitmap = bm
        if (mBitmap == null) {
            mBitmap = getBitmapFromFileUri(context.contentResolver, path)
            val size = (mBitmap!!.byteCount / MB).toString()
            Log.d(TAG,"doInBackground: bitmap size: megabytes: ${size}MB")
        }

        var bytes: ByteArray? = null
        for (i in 1..10) {
            if (i == 10) {
                Toast.makeText(context, "That image is too large.", Toast.LENGTH_SHORT).show()
                break
            }
            bytes = getBytesFromBitmap(mBitmap!!, 100 / i)
            Log.d(TAG, "doInBackground: megabytes: (" + (11 - i) + "0%) " + bytes!!.size / MB + " MB")
            if (bytes.size / MB < MB_THRESH_HOLD) {
                return bytes
            }
        }

        return bytes
    }


    private fun getBitmapFromFileUri(contentResolver: ContentResolver, fileUri: Uri?): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, fileUri!!))
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
            }
    }


    // convert from bitmap to byte array
    private fun getBytesFromBitmap(bitmap: Bitmap, quality: Int): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }




    companion object {
        private const val MB_THRESH_HOLD = 5.0
        private const val MB = 1000000.0
    }

}