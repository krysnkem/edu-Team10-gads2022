package app.krys.bookspaceapp

import android.app.Application
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration

class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Firebase.database.setPersistenceEnabled(true)

        // Create global configuration and initialize ImageLoader with this config
        val config = ImageLoaderConfiguration.Builder(this)
        .build();
        ImageLoader.getInstance().init(config);

    }

}