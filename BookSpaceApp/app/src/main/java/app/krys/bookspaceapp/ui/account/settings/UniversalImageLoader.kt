package app.krys.bookspaceapp.ui.account.settings


import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import app.krys.bookspaceapp.R
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer



/** Config only initialize once for the whole App and it's done in HomeActivity and ImageLoader
 * can then be used anywhere in the App
 * */
open class UniversalImageLoader(private val mContext: Context) {

    private val TAG = this::class.simpleName
    private val defaultImage: Drawable? = ContextCompat.getDrawable(mContext, R.drawable.default_avatar)

    init {
        Log.d(TAG, "UniversalImageLoader: started")
    }

    fun getConfig(): ImageLoaderConfiguration? {
        Log.d(TAG, "getConfig: Returning image loader configuration")
        // UNIVERSAL IMAGE LOADER SETUP
        val defaultOptions: DisplayImageOptions = DisplayImageOptions.Builder()
            .showImageOnLoading(defaultImage) // resource or drawable
            .showImageForEmptyUri(defaultImage) // resource or drawable
            .showImageOnFail(defaultImage) // resource or drawable
            .cacheOnDisk(true)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .displayer(FadeInBitmapDisplayer(300))
            .build()

        return ImageLoaderConfiguration.Builder(mContext)
            .defaultDisplayImageOptions(defaultOptions)
            .memoryCache(WeakMemoryCache())
            .diskCacheSize(100 * 1024 * 1024)
            .build()
    }
}